package cn.lanink.autoresourcechest.entity

import cn.lanink.gamecore.utils.NukkitTypeUtils
import cn.nukkit.entity.Entity
import cn.nukkit.level.Position
import cn.nukkit.level.format.FullChunk
import cn.nukkit.nbt.tag.CompoundTag

/**
 * @author lt_name
 */
class EntityText : Entity {

    override fun getNetworkId(): Int {
        return 64
    }

    @Deprecated("只是为了兼容PN核心")
    constructor(chunk: FullChunk?, nbt: CompoundTag?) : super(chunk, nbt) {
        this.close()
    }

    constructor(position: Position) : super(position.chunk, getDefaultNBT(position)) {
        if (NukkitTypeUtils.getNukkitType() == NukkitTypeUtils.NukkitType.MOT
            || NukkitTypeUtils.getNukkitType() == NukkitTypeUtils.NukkitType.POWER_NUKKIT_X) {
            this.setCanBeSavedWithChunk(false)
        }
    }

    override fun initEntity() {
        super.initEntity()
        this.nameTag = ""
        this.isNameTagVisible = true
        this.isNameTagAlwaysVisible = true
        this.setImmobile()
    }

}