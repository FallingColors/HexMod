package at.petrak.hexcasting.api.spell.casting

import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.phys.AABB

/**
 * Optional field on a [CastingContext] for the spell circle
 */
data class SpellCircleContext(val impetusPos: BlockPos, val aabb: AABB, val activatorAlwaysInRange: Boolean) {
    fun serializeToNBT(): CompoundTag {
        val out = CompoundTag()

        out.putInt(TAG_IMPETUS_X, impetusPos.x)
        out.putInt(TAG_IMPETUS_Y, impetusPos.y)
        out.putInt(TAG_IMPETUS_Z, impetusPos.z)

        out.putDouble(TAG_MIN_X, aabb.minX)
        out.putDouble(TAG_MIN_Y, aabb.minY)
        out.putDouble(TAG_MIN_Z, aabb.minZ)
        out.putDouble(TAG_MAX_X, aabb.maxX)
        out.putDouble(TAG_MAX_Y, aabb.maxY)
        out.putDouble(TAG_MAX_Z, aabb.maxZ)

        out.putBoolean(TAG_PLAYER_ALWAYS_IN_RANGE, activatorAlwaysInRange)

        return out
    }

    companion object {
        const val TAG_IMPETUS_X = "impetus_x"
        const val TAG_IMPETUS_Y = "impetus_y"
        const val TAG_IMPETUS_Z = "impetus_z"
        const val TAG_MIN_X = "min_x"
        const val TAG_MIN_Y = "min_y"
        const val TAG_MIN_Z = "min_z"
        const val TAG_MAX_X = "max_x"
        const val TAG_MAX_Y = "max_y"
        const val TAG_MAX_Z = "max_z"
        const val TAG_PLAYER_ALWAYS_IN_RANGE = "player_always_in_range"

        fun DeserializeFromNBT(tag: CompoundTag): SpellCircleContext {
            val impX = tag.getInt(TAG_IMPETUS_X)
            val impY = tag.getInt(TAG_IMPETUS_Y)
            val impZ = tag.getInt(TAG_IMPETUS_Z)

            val minX = tag.getDouble(TAG_MIN_X)
            val minY = tag.getDouble(TAG_MIN_Y)
            val minZ = tag.getDouble(TAG_MIN_Z)
            val maxX = tag.getDouble(TAG_MAX_X)
            val maxY = tag.getDouble(TAG_MAX_Y)
            val maxZ = tag.getDouble(TAG_MAX_Z)

            val playerAIR = tag.getBoolean(TAG_PLAYER_ALWAYS_IN_RANGE)

            return SpellCircleContext(BlockPos(impX, impY, impZ), AABB(minX, minY, minZ, maxX, maxY, maxZ), playerAIR)
        }
    }
}
