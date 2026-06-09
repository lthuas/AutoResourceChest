package cn.lanink.autoresourcechest.item

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.lanink.autoresourcechest.utils.Utils
import cn.lanink.gamecore.utils.NukkitTypeUtils
import cn.nukkit.item.Item
import cn.nukkit.item.StringItem

/**
 * @author lt_name
 */
class FixedItem: BaseItem {

    constructor(string: String) {
        val split = string.split("&")
        val split1 = split[0].split(":")
        if (split1.size >= 2 && split1[1] == "nbt") {
            this.nbtItemName = split1[0]
            val nbtItemString = AutoResourceChest.instance?.getNbtConfig()?.getString(this.nbtItemName)
            if (nbtItemString == null || nbtItemString == "") {
                AutoResourceChest.instance?.logger?.error("NBT物品：${this.nbtItemName} 配置不存在，无法加载！")
                return
            }
            val split2 = nbtItemString.split(":")
            this.item = Item.fromString("${split2[0]}:${split2[1]}")
            this.item.compoundTag = Utils.base64ToBytes(split2[2])
        } else {
            this.item = Item.fromString(split[0])
        }
        this.item.setCount(split[1].toInt())
    }

    constructor(item: Item) {
        this.item = item
    }

    override fun toString(): String {
        return if (this.isNbtItem()) {
            "${this.nbtItemName}:nbt&${this.item.count}"
        } else {
            if (NukkitTypeUtils.getNukkitType() == NukkitTypeUtils.NukkitType.MOT) {
                if (this.item is StringItem) {
                    return "${this.item.namespaceId}&${this.item.count}"
                }
            }
            "${this.item.id}:${this.item.damage}&${this.item.count}"
        }
    }

}