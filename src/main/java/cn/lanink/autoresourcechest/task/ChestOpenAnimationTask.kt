package cn.lanink.autoresourcechest.task

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.lanink.autoresourcechest.chest.Chest
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.item.Item
import cn.nukkit.level.Position
import cn.nukkit.scheduler.PluginTask

class ChestOpenAnimationTask(
    owner: AutoResourceChest,
    private val chest: Chest,
    private val blockPos: Position,
    private val savedItems: Map<Int, Item>
) : PluginTask<AutoResourceChest>(owner) {

    private var currentSlot = 0
    private val inventorySize = 27

    override fun onRun(i: Int) {
        if (currentSlot >= inventorySize) {
            chest.finishAnimation()
            this.cancel()
            return
        }

        val level = blockPos.level
        if (level == null) {
            chest.finishAnimation()
            this.cancel()
            return
        }

        val blockEntity = level.getBlockEntity(blockPos) as? BlockEntityChest
        if (blockEntity == null) {
            chest.finishAnimation()
            this.cancel()
            return
        }

        val inventory = blockEntity.inventory
        val savedItem = savedItems[currentSlot]
        if (savedItem != null) {
            inventory.setItem(currentSlot, savedItem.clone())
        } else {
            inventory.setItem(currentSlot, Item.get(0))
        }

        currentSlot++
    }

}
