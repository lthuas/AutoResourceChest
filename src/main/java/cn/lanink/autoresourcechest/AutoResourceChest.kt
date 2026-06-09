package cn.lanink.autoresourcechest

import cn.lanink.autoresourcechest.chest.Chest
import cn.lanink.autoresourcechest.chest.ChestManager
import cn.lanink.autoresourcechest.command.AutoResourceChestCommand
import cn.lanink.autoresourcechest.player.PlayerConfigManager
import cn.lanink.autoresourcechest.task.ChestUpdateTask
import cn.lanink.autoresourcechest.task.WorldChestCheckTask
import cn.lanink.gamecore.utils.ConfigUtils
import cn.lanink.gamecore.utils.VersionUtils
import cn.nukkit.Player
import cn.nukkit.block.Block
import cn.nukkit.block.BlockID
import cn.nukkit.level.Position
import cn.nukkit.plugin.PluginBase
import cn.nukkit.utils.Config
import updata.AutoData
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/**
 * @author lt_name
 */
class AutoResourceChest : PluginBase() {

    val chestConfigMap: HashMap<String, ChestManager> = HashMap()
    val placeChestPlayer: HashMap<Player, ChestManager> = HashMap()

    val playerConfigManager = PlayerConfigManager(this)

    private var nbtConfig: Config? = null

    val autoWorlds: HashMap<String, String> = HashMap()

    companion object {
        @JvmStatic
        val RANDOM = Random()
        const val VERSION = "?"
        const val MINIMUM_GAME_CORE_VERSION = "1.6.12"
        var debug = false
        var instance: AutoResourceChest? = null
    }

    @Override
    override fun onLoad() {
        instance = this

        try {
            if (server.pluginManager.getPlugin("AutoUpData") != null) {
                if (AutoData.defaultUpDataByMaven(this, this.file, "cn.lanink", "AutoResourceChest", "")) {
                    return
                }
            }
        } catch (t: Throwable) {
            this.logger.warning("")
        }

        this.saveDefaultConfig()

        val file1 = File("$dataFolder/Chests")
        if (!file1.exists() && !file1.mkdirs()) {
            this.logger.error("Chests 文件夹初始化失败, 这可能导致插件无法正常运行！")
        }
        val file2 = File("$dataFolder/Players")
        if (!file2.exists() && !file2.mkdirs()) {
            this.logger.error("Players 文件夹初始化失败, 这可能导致插件无法正常运行！")
        }

        if (this.config.getBoolean("debug", false)) {
            debug = true
            this.logger.warning("§c=========================================")
            this.logger.warning("§c 警告：您开启了debug模式！")
            this.logger.warning("§c Warning: You have turned on debug mode!")
            this.logger.warning("§c=========================================")
            try {
                Thread.sleep(5000)
            } catch (ignored : InterruptedException) {

            }
        }

        if (!this.config.exists("autoWorld")) {
            this.config.set("autoWorld", HashMap<String, String>())
            this.config.save()
        }

        val description = Config()
        description.load(this.getResource("Description/config.yml"))
        ConfigUtils.addDescription(this.config, description)
    }

    @Override
    override fun onEnable() {
        val gameCore = this.server.pluginManager.plugins["MemoriesOfTime-GameCore"]
        if (gameCore == null) {
            this.logger.error("未找到 MemoriesOfTime-GameCore 前置插件，插件无法正常运行！")
            this.server.pluginManager.disablePlugin(this)
            return
        }
        if (!VersionUtils.checkMinimumVersion(gameCore, MINIMUM_GAME_CORE_VERSION)) {
            this.logger.error("MemoriesOfTime-GameCore 版本过低，插件无法正常运行！")
            this.server.pluginManager.disablePlugin(this)
            return
        }

        this.server.pluginManager.registerEvents(OnListener(this), this)

        this.server.commandMap.register("AutoResourceChest", AutoResourceChestCommand())

        this.server.scheduler.scheduleRepeatingTask(this, ChestUpdateTask(this), 20)
        this.server.scheduler.scheduleTask(this) { //所有插件加载完后再加载资源箱，防止自定义物品出问题
            this.loadAllChests()

            autoWorlds.putAll(this.config.get("autoWorld", HashMap<String, String>()))
            if (autoWorlds.isNotEmpty()) {
                this.server.scheduler.scheduleRepeatingTask(
                    this,
                    WorldChestCheckTask(this, autoWorlds),
                    20, true
                )
            }

            this.logger.warning("AutoResourceChest 是一款免费插件，开源链接: https://github.com/lt-name/AutoResourceChest")
        }

        this.logger.info("加载完成！版本:$VERSION")
    }

    @Override
    override fun onDisable() {
        var count = 0
        for (chestManager in this.chestConfigMap.values) {
            chestManager.saveConfig()
            chestManager.closeAllChest()
            count++
        }
        this.logger.info("成功保存 $count 个资源箱配置")
        this.playerConfigManager.saveAllPlayerConfig()
        this.logger.info("卸载完成！")
    }

    fun loadAllChests() {
        val files = File("$dataFolder/Chests").listFiles()
        var count = 0
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                if (!file.isFile) {
                    continue
                }
                val name = file.name.split(".")[0]
                this.chestConfigMap[name] = ChestManager(name, Config(file, Config.YAML))
                count++
            }
        }
        this.logger.info("§a已加载 §e$count §a个资源箱配置")
    }

    fun getNbtConfig(): Config {
        if (this.nbtConfig == null) {
            this.nbtConfig = Config("$dataFolder/nbtItem.yml", Config.YAML)
        }
        return this.nbtConfig!!
    }

    fun getChestByPos(position: Position): Chest? {
        for (chestManager: ChestManager in this.chestConfigMap.values) {
            val chest = chestManager.getChestByPos(position)
            if (chest != null) {
                return chest
            }
        }
        return null
    }

    fun isSupportChest(block: Block): Boolean {
        return block.id == BlockID.CHEST || block.id == BlockID.TRAPPED_CHEST
    }

}