package cn.lanink.autoresourcechest.chest

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.lanink.autoresourcechest.item.FixedItem
import cn.lanink.autoresourcechest.item.RandomItem
import cn.nukkit.Server
import cn.nukkit.item.Item
import cn.nukkit.level.Position
import cn.nukkit.utils.Config
import lombok.EqualsAndHashCode
import java.util.concurrent.ConcurrentHashMap

/**
 * @author lt_name
 */
@EqualsAndHashCode
class ChestManager(val name: String, private val config: Config) {

    var showName: String = config.getString("showName")
    var canBePutIn: Boolean = config.getBoolean("玩家可以放入物品")
    var refreshInterval: Int = config.getInt("刷新间隔(s)")
    var restrictOpenCount: Int = config.getInt("限制打开次数", -1)
    var maxRandomItemCount: Int = config.getInt("随机物品种类数量限制")
    var enableOpenAnimation: Boolean = config.getBoolean("开箱动画", true)
    var animationSpeed: Int = config.getInt("动画速度(tick)", 2)
    var fixedItems = ArrayList<FixedItem>()
    var randomItems = ArrayList<RandomItem>()
    val chests: MutableMap<Position, Chest> = ConcurrentHashMap()

    init {
        for (stringItem in this.config.getStringList("fixedItem")) {
            try {
                this.fixedItems.add(FixedItem(stringItem))
            }catch (e: Exception) {
                AutoResourceChest.instance?.logger?.error("读取固定刷新物品 $stringItem 时出现错误，请检查配置文件：Chests/${this.name}.yml！", e)
            }
        }
        for (stringItem in this.config.getStringList("randomItem")) {
            try {
                this.randomItems.add(RandomItem(stringItem!!))
            }catch (e: Exception) {
                AutoResourceChest.instance?.logger?.error("读取随机刷新物品 $stringItem 时出现错误，请检查配置文件：Chests/${this.name}.yml！", e)
            }

        }
        for (pos in this.config.getStringList("pos")) {
            try {
                val split = pos.split(":")
                if (!Server.getInstance().loadLevel(split[3])) {
                    AutoResourceChest.instance?.logger?.warning("世界：$split[3] 加载失败！")
                    continue
                }
                val position = Position(
                    split[0].toDouble(), split[1].toDouble(), split[2].toDouble(),
                    Server.getInstance().getLevelByName(split[3])
                )
                val supportChest = AutoResourceChest.instance?.isSupportChest(position.level.getBlock(position)) ?: false
                if (position.chunk == null || !supportChest) {
                    AutoResourceChest.instance?.logger?.error("读取资源箱坐标 $pos 时出现异常，请检查配置文件：Chests/${this.name}.yml！")
                    continue
                }
                position.chunk?.load()
                this.chests[position] = Chest(this, position)
            }catch (e: Exception) {
                AutoResourceChest.instance?.logger?.error("读取资源箱坐标 $pos 时出现异常，请检查配置文件：Chests/${this.name}.yml！", e)
            }
        }
    }

    fun saveConfig() {
        this.config.set("showName", this.showName)
        this.config.set("刷新间隔(s)", this.refreshInterval)
        this.config.set("限制打开次数", this.restrictOpenCount)
        this.config.set("随机物品种类数量限制", this.maxRandomItemCount)
        this.config.set("开箱动画", this.enableOpenAnimation)
        this.config.set("动画速度(tick)", this.animationSpeed)

        val fixedItemList = mutableListOf<String>()
        for (fixedItem: FixedItem in this.fixedItems) {
            fixedItemList.add(fixedItem.toString())
        }
        this.config.set("fixedItem", fixedItemList)

        val randomItemList = mutableListOf<String>()
        for (randomItem: RandomItem in this.randomItems) {
            randomItemList.add(randomItem.toString())
        }
        this.config.set("randomItem", randomItemList)

        val list = mutableListOf<String>()
        for (pos in this.chests.keys) {
            list.add("${pos.x}:${pos.y}:${pos.z}:${pos.level.name}")
        }
        this.config.set("pos", list)

        this.config.save()
    }

    fun addNewChest(position: Position): Boolean {
        return addNewChest(position, false)
    }

    fun addNewChest(position: Position, ignoreTips: Boolean): Boolean {
        for (pos in this.chests.keys) {
            if (pos.getLevel() === position.getLevel() && pos == position) {
                return false
            }
        }
        val newPos = position.clone().floor()
        if (newPos.chunk == null || !newPos.getLevel().loadChunk(newPos.chunkX, newPos.chunkZ) || newPos.chunk.provider == null) {
            if (!ignoreTips) {
                AutoResourceChest.instance?.logger?.error("创建资源箱失败 $position")
            }
            return false
        }
        this.chests[newPos] = Chest(this, newPos)
        return true
    }

    fun removeChest(position: Position): Boolean {
        val chest = this.chests.remove(position.floor())
        if (chest != null) {
            chest.close()
            return true
        }
        return false
    }

    fun removeChest(chest: Chest): Boolean {
        for ((key, value) in HashMap(this.chests).entries) {
            if (value == chest) {
                this.chests.remove(key)
                value.close()
                return true
            }
        }
        return false
    }

    fun closeAllChest() {
        this.chests.values.forEach {
            chest -> chest.close()
        }
        this.chests.clear()
    }

    fun getFixedItems(): List<Item> {
        val list = ArrayList<Item>()
        for (item in this.fixedItems) {
            list.add(item.item.clone())
        }
        return list
    }

    fun getRandomItems(): List<Item> {
        val list = ArrayList<Item>()
        val randomItems = ArrayList<RandomItem>(this.randomItems)
        randomItems.shuffle()
        for (randomItem: RandomItem in randomItems) {
            val item = randomItem.getRandomItem()
            if (item.id != 0) {
                list.add(item.clone())
                if (list.size >= this.maxRandomItemCount) {
                    break
                }
            }
        }
        return list
    }

    fun getChestByPos(position: Position): Chest? {
        //需要判断level
        for ((key, value) in this.chests.entries) {
            if (key.getLevel() === position.getLevel() && key == position.floor()) {
                return value
            }
        }
        return null
    }

}