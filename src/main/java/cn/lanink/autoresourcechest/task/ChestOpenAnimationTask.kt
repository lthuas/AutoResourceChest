package cn.lanink.autoresourcechest.task

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.lanink.autoresourcechest.chest.Chest
import cn.nukkit.Player
import cn.nukkit.blockentity.BlockEntityChest
import cn.nukkit.item.Item
import cn.nukkit.level.Sound
import cn.nukkit.level.particle.HappyVillagerParticle
import cn.nukkit.level.Position
import cn.nukkit.scheduler.PluginTask

class ChestOpenAnimationTask(
    owner: AutoResourceChest,
    private val chest: Chest,
    private val blockPos: Position,
    private val savedItems: Map<Int, Item>,
    private val totalSlots: Int,
    private val mode: String,
    private val gradient: Boolean,
    private val playSound: Boolean,
    private val playParticle: Boolean,
    val player: Player
) : PluginTask<AutoResourceChest>(owner) {

    private val slotOrder: IntArray = getSlotOrder(mode, totalSlots)
    private var currentIndex = 0
    private val revealStages = if (gradient) 2 else 0
    private var stage = 0
    private val blockEntity: BlockEntityChest? = blockPos.level?.getBlockEntity(blockPos) as? BlockEntityChest
    private var particleSoundCounter = 0

    override fun onRun(i: Int) {
        if (!chest.isAnimating || currentIndex >= slotOrder.size) {
            if (chest.isAnimating) {
                chest.finishAnimation()
            } else {
                restoreRemainingSlots()
            }
            this.cancel()
            return
        }

        val be = this.blockEntity
        if (be == null || be.level == null) {
            chest.finishAnimation()
            this.cancel()
            return
        }

        val level = blockPos.level ?: run {
            chest.finishAnimation()
            this.cancel()
            return
        }

        val inventory = be.inventory
        val slot = slotOrder[currentIndex]

        if (stage < revealStages) {
            val colorItem = when (stage) {
                0 -> GRADIENT_ITEM_GRAY
                1 -> GRADIENT_ITEM_WHITE
                else -> return
            }
            inventory.setItem(slot, colorItem.clone())
            stage++
        } else {
            val savedItem = savedItems[slot]
            if (savedItem != null) {
                inventory.setItem(slot, savedItem.clone())
            } else {
                inventory.setItem(slot, EMPTY_ITEM)
            }

            chest.revealedSlots.add(slot)

            particleSoundCounter++
            if (particleSoundCounter % 3 == 0) {
                if (playParticle) {
                    level.addParticle(HappyVillagerParticle(blockPos.add(0.5, 1.0, 0.5)))
                }
                if (playSound) {
                    level.addSound(blockPos, Sound.NOTE_PLING, 0.3f, 1.0f)
                }
            }

            stage = 0
            currentIndex++
        }
    }

    private fun restoreRemainingSlots() {
        val saved = chest.savedItems ?: return
        chest.clearSavedItems()
        val be = this.blockEntity
        if (be == null || be.level == null) {
            return
        }
        val inventory = be.inventory
        for (i in currentIndex until slotOrder.size) {
            val slot = slotOrder[i]
            val savedItem = saved[slot]
            if (savedItem != null) {
                inventory.setItem(slot, savedItem.clone())
            } else {
                inventory.setItem(slot, EMPTY_ITEM)
            }
        }
    }

    companion object {
        private val GRADIENT_ITEM_GRAY = Item.get(241, 8, 1)
        private val GRADIENT_ITEM_WHITE = Item.get(241, 0, 1)
        private val EMPTY_ITEM = Item.get(0)

        private val randomSlotCache = mutableMapOf<Int, Array<IntArray>>()

        fun getSlotOrder(mode: String, totalSlots: Int): IntArray {
            return when (mode.uppercase()) {
                "CENTER_OUT" -> {
                    val list = mutableListOf<Int>()
                    val center = totalSlots / 2
                    list.add(center)
                    var left = center - 1
                    var right = center + 1
                    while (left >= 0 || right < totalSlots) {
                        if (left >= 0) list.add(left--)
                        if (right < totalSlots) list.add(right++)
                    }
                    list.toIntArray()
                }
                "SNAKE" -> {
                    val list = mutableListOf<Int>()
                    for (row in 0 until 3) {
                        if (row % 2 == 0) {
                            for (col in 0 until 9) list.add(row * 9 + col)
                        } else {
                            for (col in 8 downTo 0) list.add(row * 9 + col)
                        }
                    }
                    list.toIntArray()
                }
                "RANDOM" -> {
                    val cache = randomSlotCache.getOrPut(totalSlots) {
                        Array(5) { (0 until totalSlots).shuffled().toIntArray() }
                    }
                    cache[(0 until cache.size).random()].copyOf()
                }
                "COLUMNS" -> {
                    val list = mutableListOf<Int>()
                    for (col in 0 until 9) {
                        for (row in 0 until 3) list.add(row * 9 + col)
                    }
                    list.toIntArray()
                }
                else -> (0 until totalSlots).toList().toIntArray()
            }
        }
    }

}
