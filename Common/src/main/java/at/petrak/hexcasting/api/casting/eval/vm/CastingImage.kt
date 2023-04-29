package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.utils.*
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity

/**
 * The state of a casting VM, containing the stack and all
 */
data class CastingImage private constructor(
    val stack: List<Iota>,

    val parenCount: Int,
    val parenthesized: List<Iota>,
    val escapeNext: Boolean,
    val opsConsumed: Long,

    val userData: CompoundTag
) {
    constructor() : this(listOf(), 0, listOf(), false, 0, CompoundTag())

    /**
     * Return a copy of this with the given number of ops additionally exhausted
     */
    fun withUsedOps(count: Long) = this.copy(opsConsumed = this.opsConsumed + count)

    /**
     * Return a copy of this with 1 op used
     */
    fun withUsedOp() = this.withUsedOps(1)

    fun serializeToNbt() = NBTBuilder {
        TAG_STACK %= stack.serializeToNBT()

        TAG_PAREN_COUNT %= parenCount
        TAG_ESCAPE_NEXT %= escapeNext
        TAG_PARENTHESIZED %= parenthesized.serializeToNBT()
        TAG_OPS_CONSUMED %= opsConsumed

        TAG_USERDATA %= userData
    }

    companion object {
        const val TAG_STACK = "stack"
        const val TAG_PAREN_COUNT = "open_parens"
        const val TAG_PARENTHESIZED = "parenthesized"
        const val TAG_ESCAPE_NEXT = "escape_next"
        const val TAG_OPS_CONSUMED = "ops_consumed"
        const val TAG_USERDATA = "userdata"

        @JvmStatic
        public fun loadFromNbt(tag: CompoundTag, world: ServerLevel): CastingImage {
            return try {
                val stack = mutableListOf<Iota>()
                val stackTag = tag.getList(TAG_STACK, Tag.TAG_COMPOUND)
                for (subtag in stackTag) {
                    val datum = IotaType.deserialize(subtag.asCompound, world)
                    stack.add(datum)
                }

                val userData = if (tag.contains(TAG_USERDATA)) {
                    tag.getCompound(TAG_USERDATA)
                } else {
                    CompoundTag()
                }

                val parenthesized = mutableListOf<Iota>()
                val parenTag = tag.getList(TAG_PARENTHESIZED, Tag.TAG_COMPOUND)
                for (subtag in parenTag) {
                    parenthesized.add(IotaType.deserialize(subtag.downcast(CompoundTag.TYPE), world))
                }

                val parenCount = tag.getInt(TAG_PAREN_COUNT)
                val escapeNext = tag.getBoolean(TAG_ESCAPE_NEXT)
                val opsUsed = tag.getLong(TAG_OPS_CONSUMED)

                CastingImage(stack, parenCount, parenthesized, escapeNext, opsUsed, userData)
            } catch (exn: Exception) {
                HexAPI.LOGGER.warn("error while loading a CastingImage", exn)
                CastingImage()
            }
        }

        @JvmStatic
        fun checkAndMarkGivenMotion(userData: CompoundTag, entity: Entity): Boolean {
            val marked = userData.getOrCreateCompound(HexAPI.MARKED_MOVED_USERDATA)
            return if (marked.contains(entity.stringUUID)) {
                true
            } else {
                marked.putBoolean(entity.stringUUID, true)
                false
            }
        }
    }
}
