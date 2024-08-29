package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.deserializeWithCodec
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.serializeWithCodec
import com.mojang.serialization.Codec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

/**
 * A continuation during the execution of a spell.
 */
sealed interface SpellContinuation {
    object Done : SpellContinuation

    data class NotDone(val frame: ContinuationFrame, val next: SpellContinuation) : SpellContinuation

    fun pushFrame(frame: ContinuationFrame): SpellContinuation = NotDone(frame, this)

    @Deprecated(
        "Use the codec instead.",
        replaceWith = ReplaceWith("serializeWithCodec(SpellContinuation.getCodec())")
    )
    fun serializeToNBT(): CompoundTag = this.serializeWithCodec(getCodec()) as CompoundTag

    private fun toList(): List<ContinuationFrame> {
        var self = this
        val frames = mutableListOf<ContinuationFrame>()
        while (self is NotDone) {
            frames.add(self.frame)
            self = self.next
        }
        return frames
    }

    companion object {
        @JvmStatic
        fun getCodec(): Codec<SpellContinuation> = ContinuationFrame.getCodec().listOf()
            .xmap(SpellContinuation::fromList, SpellContinuation::toList)

        @JvmStatic
        fun getCodec(world: ServerLevel): Codec<SpellContinuation> = ContinuationFrame.getCodec(world).listOf()
            .xmap(SpellContinuation::fromList, SpellContinuation::toList)

        @JvmField
        val TAG_FRAME = "frame"

        /**
         * Takes a tag containing the ContinuationFrame.Type resourcelocation and the serialized continuation frame, and returns
         * the deserialized continuation frame.
         */
        @Deprecated(
            "Use the codec instead.",
            replaceWith = ReplaceWith("tag.deserializeWithCodec(SpellContinuation.getCodec(world))")
        )
        @JvmStatic
        fun fromNBT(nbt: CompoundTag, world: ServerLevel): SpellContinuation =
            nbt.deserializeWithCodec(getCodec(world))!!

        private fun fromList(frames: List<ContinuationFrame>): SpellContinuation {
            var result: SpellContinuation = Done
            for (frame in frames.asReversed()) {
                if (frame is CompoundTag) {
                    result = result.pushFrame(frame)
                }
            }
            return result
        }
    }
}
