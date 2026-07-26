package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerLevel

/**
 * A frame representing all the state for a Thoth evaluation.
 * Pushed by SpecialHandlerForEach.
 * @property data list of *remaining* datums to ForEach over
 * @property code code to run per datum
 * @property contextStack the stack state used for each iteration
 * @property stashedStack the stack state to restore after all iterations finish
 * @property acc concatenated list of final stack states after each iteration
 */
data class FrameForEach(
    val data: TreeList<Iota>,
    val code: TreeList<Iota>,
    val contextStack: TreeList<Iota>,
    val stashedStack: TreeList<Iota>,
    val acc: TreeList<Iota>
) : ContinuationFrame {

    /** When halting, we add the stack state at halt to the stack accumulator, then return the stashed stack, plus the accumulator. */
    override fun breakDownwards(stack: TreeList<Iota>): Pair<Boolean, TreeList<Iota>> {
        return true to stashedStack.appended(ListIota(acc.appendedAll(stack)))
    }

    /** Step the Thoth computation, enqueueing one code evaluation. */
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingVM
    ): CastResult {
        // Save the stack to the accumulator. On the first iteration, the stack will be empty.
        val newAcc = acc.appendedAll(harness.image.stack)

        // If we still have data to process...
        val (newStack, newImage, newCont) = if (!data.isEmpty()) {
            // Create a stack composed of the context stack plus the next datum
            val stack = contextStack.appended(data.head())
            val cont2 = continuation
                // put the next Thoth object back on the stack for the next Thoth cycle,
                .pushFrame(FrameForEach(data.tail(), code, contextStack, stashedStack, newAcc))
                // and prep the Thoth'd code block for evaluation.
                .pushFrame(FrameEvaluate(code, true))
            Triple(stack, harness.image.withUsedOp(), cont2)
        } else {
            // Else, restore the stashed stack, and dump our final list on top.
            val stack = stashedStack.appended(ListIota(newAcc))
            Triple(stack, harness.image, continuation)
        }
        return CastResult(
            ListIota(code),
            newCont,
            // reset escapes so they don't carry over to other iterations or out of thoth
            newImage.withResetEscape().copy(stack = newStack),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH,
        )
    }

    override fun size() = data.size + code.size + acc.size + contextStack.size + stashedStack.size

    override val type: ContinuationFrame.Type<*> = TYPE

    companion object {
        @JvmField
        val TYPE: ContinuationFrame.Type<FrameForEach> = object : ContinuationFrame.Type<FrameForEach> {
            val CODEC = RecordCodecBuilder.mapCodec<FrameForEach> { inst ->
                inst.group(
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("data").forGetter { it.data },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("code").forGetter { it.code },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("context").forGetter { it.contextStack },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("stashed").forGetter { it.stashedStack },
                    TreeList.codecOf(IotaType.TYPED_CODEC).fieldOf("accumulator").forGetter { it.acc }
                ).apply(inst) { a, b, c, d, e ->
                    FrameForEach(a, b, c, d, e)
                }
            }
            val STREAM_CODEC = StreamCodec.composite(
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameForEach::data,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameForEach::code,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameForEach::contextStack,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameForEach::stashedStack,
                IotaType.TYPED_STREAM_CODEC.apply(TreeList.streamCodecOp()), FrameForEach::acc
            ) { a, b, c, d, e ->
                FrameForEach(a, b, c, d, e)
            }


            override fun codec(): MapCodec<FrameForEach> =
                CODEC

            override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, FrameForEach> =
                STREAM_CODEC
        }
    }
}
