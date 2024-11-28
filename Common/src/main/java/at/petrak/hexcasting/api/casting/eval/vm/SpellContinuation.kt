package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.getList
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel
import kotlin.math.max

/**
 * A continuation during the execution of a spell.
 */
sealed interface SpellContinuation {
    object Done : SpellContinuation {
        override fun size(): Int = 0
        override fun depth(): Int = 0
    }

    data class NotDone(val frame: ContinuationFrame, val next: SpellContinuation) : SpellContinuation {
        override fun size(): Int = frame.size() + next.size()
        override fun depth(): Int = max(frame.depth(), next.depth())
    }

    fun pushFrame(frame: ContinuationFrame): SpellContinuation = NotDone(frame, this)

    fun size(): Int
    fun depth(): Int

    fun serializeToNBT() = NBTBuilder {
        TAG_FRAME %= list(getNBTFrames())
    }
    fun getNBTFrames(): List<CompoundTag> {
        var self = this
        val frames = mutableListOf<CompoundTag>()
        while (self is NotDone) {
            frames.add(ContinuationFrame.toNBT(self.frame))
            self = self.next
        }
        return frames
    }
    companion object {
        const val TAG_FRAME = "frame"

        @JvmStatic
        fun fromNBT(nbt: CompoundTag, world: ServerLevel): SpellContinuation {
            val frames = nbt.getList(TAG_FRAME, Tag.TAG_COMPOUND)
            var result: SpellContinuation = Done
            for (frame in frames.asReversed()) {
                if (frame is CompoundTag) {
                    result = result.pushFrame(ContinuationFrame.fromNBT(frame, world))
                }
            }
            return result
        }
    }
}
