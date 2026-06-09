package cn.lanink.autoresourcechest.player

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.nukkit.Player
import cn.nukkit.utils.Config
import java.io.File

/**
 * @author lt_name
 */
class PlayerConfigManager(val autoResourceChest: AutoResourceChest) {

    val playerConfigMap = HashMap<String, PlayerConfig>()

    fun loadAllPlayerConfig() {
        val files = File("${this.autoResourceChest.dataFolder}/Players").listFiles()
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                if (!file.isFile) {
                    continue
                }
                val name = file.name.split(".")[0]
                if (this.playerConfigMap[name] == null) {
                    this.playerConfigMap[name] = PlayerConfig(name, Config(file, Config.YAML))
                }
            }
        }
    }

    private fun loadPlayerConfig(name: String): PlayerConfig {
        val get = this.playerConfigMap[name]
        if (get != null) {
            return get
        }
        val config = Config("${this.autoResourceChest.dataFolder}/Players/$name.yml", Config.YAML)
        val playerConfig = PlayerConfig(name, config)
        this.playerConfigMap[name] = playerConfig
        return playerConfig;
    }

    fun saveAllPlayerConfig() {
        for (playerConfig in this.playerConfigMap.values) {
            playerConfig.save()
        }
    }

    fun getPlayerConfig(player: Player): PlayerConfig {
        return this.getPlayerConfig(player.name)
    }

    fun getPlayerConfig(name: String): PlayerConfig {
        return this.playerConfigMap.getOrDefault(name, this.loadPlayerConfig(name))
    }

}