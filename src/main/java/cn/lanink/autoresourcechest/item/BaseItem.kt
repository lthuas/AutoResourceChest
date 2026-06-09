package cn.lanink.autoresourcechest.item

import cn.nukkit.item.Item

/**
 * @author lt_name
 */
open class BaseItem {

    var nbtItemName: String? = null
    var item: Item = Item.get(0)

    fun isNbtItem(): Boolean {
        return this.nbtItemName != null
    }

}