package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel

/**
 * A list of patterns to be evaluated in sequence.
 * @property list the *remaining* list of patterns to be evaluated
 * @property isMetacasting only for sound effects, if this is being cast from a hermes / iris
 */
data class FrameEvaluate(val list: SpellList, val isMetacasting: Boolean) : ContinuationFrame {
    // Discard this frame and keep discarding frames.
    override fun breakDownwards(stack: List<Iota>) = false to stack

    // Step the list of patterns, evaluating a single one.
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingVM
    ): CastResult {
        // If there are patterns left...
        return if (list.nonEmpty) {
            val newCont = if (list.cdr.nonEmpty) { // yay TCO
                // ...enqueue the evaluation of the rest of the patterns...
                continuation.pushFrame(FrameEvaluate(list.cdr, this.isMetacasting))
            } else continuation
            // ...before evaluating the first one in the list.
            val update = harness.executeInner(list.car, level, newCont)
            if (this.isMetacasting && update.sound != HexEvalSounds.MISHAP) {
                update.copy(sound = HexEvalSounds.HERMES)
            } else {
                update
            }
        } else {
            // If there are no patterns (e.g. empty Hermes), just return OK.
            CastResult(ListIota(list), continuation, null, listOf(), ResolvedPatternType.EVALUATED, HexEvalSounds.HERMES)
        }
    }

    override fun size() = list.size()

    override val type: ContinuationFrame.Type<*> = TYPE

    companion object {

        @JvmField
        val TYPE: ContinuationFrame.Type<FrameEvaluate> = object : ContinuationFrame.Type<FrameEvaluate> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameEvaluate> { inst ->
                inst.group(
                    SpellList.CODEC.fieldOf("patterns").forGetter { it.list },
                    Codec.BOOL.fieldOf("isMetacasting").forGetter { it.isMetacasting }
                ).apply(inst, ::FrameEvaluate)
            }
            val STREAM_CODEC = StreamCodec.composite(
                SpellList.STREAM_CODEC, FrameEvaluate::list,
                ByteBufCodecs.BOOL, FrameEvaluate::isMetacasting,
                ::FrameEvaluate
            )


            override fun codec(): MapCodec<FrameEvaluate> =
                CODEC

            override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, FrameEvaluate> =
                STREAM_CODEC
        }
    }
}