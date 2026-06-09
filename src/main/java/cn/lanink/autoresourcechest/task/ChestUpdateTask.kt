package cn.lanink.autoresourcechest.task

import cn.lanink.autoresourcechest.AutoResourceChest
import cn.lanink.autoresourcechest.chest.Chest
import cn.lanink.autoresourcechest.chest.ChestManager
import cn.nukkit.scheduler.PluginTask

/**
 * @author lt_name
 */
class ChestUpdateTask(owner: AutoResourceChest) : PluginTask<AutoResourceChest>(owner) {

    @Override
    override fun onRun(i: Int) {
        for (chestManager: ChestManager in owner.chestConfigMap.values) {
            for (chest: Chest in chestManager.chests.values) {
                chest.onUpdate()
            }
        }
    }


}