package cn.lanink.autoresourcechest.item

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.lanink.autoresourcechest.AutoResourceChest.Companion.RANDOM
import cn.lanink.autoresourcechest.utils.Utils
import cn.lanink.gamecore.utils.NukkitTypeUtils
import cn.nukkit.item.Item
import cn.nukkit.item.StringItem
import lombok.AllArgsConstructor
import lombok.EqualsAndHashCode

/**
 * @author lt_name
 */
@AllArgsConstructor
@EqualsAndHashCode
class RandomItem: BaseItem {

    val probability: Int

    constructor(string: String) {
        val split = string.split("&")
        val split1 = split[0].split(":")
        val split2 = split[1].split("@")
        this.probability = split2[1].toInt()
        if (split1.size >= 2 && split1[1] == "nbt") {
            this.nbtItemName = split1[0]
            val nbtItemString = AutoResourceChest.instance?.getNbtConfig()?.getString(this.nbtItemName)
            if (nbtItemString == null || nbtItemString == "") {
                AutoResourceChest.instance?.logger?.error("NBT物品：${this.nbtItemName} 配置不存在，无法加载！")
                this.item = Item.get(0)
                return
            }
            val split3 = nbtItemString.split(":")
            this.item = Item.fromString("${split3[0]}:${split3[1]}")
            this.item.compoundTag = Utils.base64ToBytes(split3[2])
        } else {
            this.item = Item.fromString(split[0])
        }
        this.item.setCount(split2[0].toInt())
    }

    constructor(item: Item, probability: Int) {
        this.item = item
        this.probability = probability
    }

    fun getRandomItem(): Item {
        return if (RANDOM.nextInt(100) < probability) item.clone() else Item.get(0)
    }

    override fun toString(): String {
        return if (this.isNbtItem()) {
            "${this.nbtItemName}:nbt&${this.item.count}@${this.probability}"
        } else {
            if (NukkitTypeUtils.getNukkitType() == NukkitTypeUtils.NukkitType.MOT) {
                if (this.item is StringItem) {
                    return "${this.item.namespaceId}&${this.item.count}"
                }
            }
            "${this.item.id}:${this.item.damage}&${this.item.count}@${this.probability}"
        }
    }

}