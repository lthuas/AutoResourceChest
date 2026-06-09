package cn.lanink.autoresourcechest.chest

import cn.lanink.autoresourcechest.entity.EntityText
import cn.lanink.autoresourcechest.utils.Utils.Companion.formatTime
import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.inventory.BaseInventory
import cn.nukkit.item.Item
import cn.nukkit.level.Position
import lombok.EqualsAndHashCode

/**
 * @author lt_name
 */
@EqualsAndHashCode
class Chest(val chestManager: ChestManager, private var position: Position) {

    private var closed: Boolean = false
    var time = this.chestManager.refreshInterval

    private var enableFloatingText: Boolean = false
    private var text: EntityText? = null
    var isNeedRefresh = false
    var isAnimating = false
        private set

    init {
        if (chestManager.showName.isNotBlank()) {
            enableFloatingText = true
            this.text = EntityText(this.position)
        }
        this.refreshInventory()
    }

    fun onUpdate() {
        if (this.closed) {
            return
        }
        if (this.isNeedRefresh) {
            this.time--
            if (this.time <= 0) {
                this.time = this.chestManager.refreshInterval
                this.refreshInventory()
                this.isNeedRefresh = false
            }
        }
        if (this.enableFloatingText) {
            this.updateFloatingText()
        }
    }

    private fun updateFloatingText() {
        if (text == null) {
            return
        }

        if (this.text!!.isClosed) {
            this.text = EntityText(this.position)
        }
        //仅调整浮空字的位置
        this.text!!.setPosition(position.add(0.5, 1.0, 0.5))
        if (this.isNeedRefresh) {
            this.text!!.nameTag = this.chestManager.showName.replace("%time%", formatTime(time))
        }else {
            this.text!!.nameTag = this.chestManager.showName.replace("%time%", "已刷新")
        }
        for (player in this.text!!.getLevel().players.values) {
            if (player.getLevel() !== this.text!!.getLevel() || player.distance(this.position) > 5) {
                this.text!!.despawnFrom(player)
            } else {
                this.text!!.spawnTo(player)
            }
        }
    }

    private fun refreshInventory() {
        var blockEntity = this.position.getLevel().getBlockEntity(this.position)
        if (blockEntity !is BlockEntityChest) {
            this.position.getLevel().setBlock(this.position, Block.get(Block.CHEST))
            blockEntity = this.position.getLevel().getBlockEntity(this.position)
            if (blockEntity !is BlockEntityChest) {
                return
            }
        }
        val inventory: BaseInventory = blockEntity.inventory
        inventory.clearAll()
        val itemList = mutableListOf<Item>()
        itemList.addAll(this.chestManager.getFixedItems())
        itemList.addAll(this.chestManager.getRandomItems())
        for (item in itemList) {
            if (item.id == 0) {
                continue
            }
            var random: Int
            var errorCount = 0
            do {
                random = (0 until inventory.size).random()
                errorCount++
            } while (errorCount < 20 && inventory.getItem(random).id != 0)
            if (errorCount < 20) {
                inventory.setItem(random, item)
            } else {
                inventory.addItem(item)
            }
        }
    }

    fun startOpenAnimation(inventory: BaseInventory): Map<Int, Item> {
        this.isAnimating = true
        val saved = HashMap<Int, Item>()
        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i)
            if (item.id != 0) {
                saved[i] = item.clone()
            }
        }
        val blackGlass = Item.get(160, 15, 1)
        for (i in 0 until inventory.size) {
            inventory.setItem(i, blackGlass.clone())
        }
        return saved
    }

    fun finishAnimation() {
        this.isAnimating = false
    }

    fun close() {
        this.closed = true
        this.text?.close()
        val blockEntity = this.position.getLevel().getBlockEntity(this.position)
        if (blockEntity != null && blockEntity is BlockEntityChest) {
            blockEntity.inventory.clearAll()
        }
    }

}