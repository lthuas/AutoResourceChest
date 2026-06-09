package cn.lanink.autoresourcechest.player

import cn.nukkit.level.Position
import cn.nukkit.utils.Config
import lombok.EqualsAndHashCode
import lombok.Getter

/**
 * @author lt_name
 */
@EqualsAndHashCode
class PlayerConfig(val name: String, val config: Config) {

    @Getter
    private val openChestLog = HashMap<String, Int>()

    init {
        this.openChestLog.putAll(this.config.get("openChestLog", HashMap()))
    }

    /**
     * 获取玩家在某个位置打开资源箱的次数
     *
     * @param position 位置
     */
    fun getOpenCount(position: Position): Int {
        return this.getOpenCount("${position.x}:${position.y}:${position.z}:${position.level.name}")
    }

    fun getOpenCount(string: String): Int {
        return this.openChestLog.getOrDefault(string, 0)
    }

    fun addOpenCount(position: Position) {
        this.addOpenCount("${position.x}:${position.y}:${position.z}:${position.level.name}")
    }

    fun addOpenCount(string: String) {
        this.openChestLog[string] = this.getOpenCount(string) + 1
    }

    fun save() {
        this.config.set("openChestLog", this.openChestLog)
        this.config.save()
    }

}