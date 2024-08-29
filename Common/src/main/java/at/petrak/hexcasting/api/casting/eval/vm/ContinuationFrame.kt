package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.deserializeWithCodec
import at.petrak.hexcasting.common.lib.hex.HexContinuationTypes
import com.google.common.base.Suppliers
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.server.level.ServerLevel
import java.util.function.Supplier

/**
 * A single frame of evaluation during the execution of a spell.
 *
 * Specifically, an evaluation will keep a stack of these frames.
 * An evaluation with no meta-eval will consist of a single [Evaluate(rest of the pats)] at all times.
 * When an Eval is invoked, we push Evaluate(pats) to the top of the stack.
 *
 * Evaluation is performed by repeatedly popping the top-most (i.e. innermost) frame from the stack,
 * then evaluating that frame (and possibly allowing it to push other frames (e.g. if it's a Hermes)).
 *
 * Once the stack of frames is empty, there are no more computations to run, so we're done.
 *
 */
interface ContinuationFrame {
    /**
     * Step the evaluation forward once.
     * For Evaluate, this consumes one pattern; for ForEach this queues the next iteration of the outer loop.
     * @return the result of this pattern step
     */
    fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingVM): CastResult

    /**
     * The OpHalt instruction wants us to "jump to" the END of the nearest meta-eval.
     * In other words, we should consume Evaluate frames until we hit a FinishEval or Thoth frame.
     * @return whether the break should stop here, alongside the new stack state (e.g. for finalizing a Thoth)
     */
    fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>>

    /**
     * Serializes this frame. Used for things like delays, where we pause execution.
     */
    fun serializeToNBT(): CompoundTag

    /**
     * Return the number of iotas contained inside this frame, used for determining whether it is valid to serialise.
     */
    fun size(): Int

    val type: Type<*>

    interface Type<U : ContinuationFrame> {
        fun getCodec(): Codec<U>

        fun getCodec(world: ServerLevel): Codec<U>

        @Deprecated(
            "Use the CODEC instead.",
            replaceWith = ReplaceWith("tag.deserializeWithCodec(getCodec())")
        )
        fun deserializeFromNBT(tag: CompoundTag, world: ServerLevel): U? =
            tag.deserializeWithCodec(getCodec())
    }

    companion object {
        @JvmStatic
        fun getCodec(): Codec<ContinuationFrame> =
            HexContinuationTypes.REGISTRY.byNameCodec().dispatchMap(
                HexContinuationTypes.KEY_TYPE,
                ContinuationFrame::type
            ) { continuationType ->
                continuationType.getCodec().fieldOf(HexContinuationTypes.KEY_DATA).codec()
            }.codec()

        @JvmStatic
        fun getCodec(world: ServerLevel): Codec<ContinuationFrame> =
                HexContinuationTypes.REGISTRY.byNameCodec().dispatchMap(
                    HexContinuationTypes.KEY_TYPE,
                    ContinuationFrame::type
                ) { continuationType ->
                    continuationType.getCodec(world).fieldOf(HexContinuationTypes.KEY_DATA).codec()
            }.codec()

        /**
         * Takes a tag containing the ContinuationFrame.Type resourcelocation and the serialized continuation frame, and returns
         * the deserialized continuation frame.
         */
        @Deprecated(
            "Use the codec instead.",
            replaceWith = ReplaceWith("tag.deserializeWithCodec(ContinuationFrame.getCodec(world))")
        )
        @JvmStatic
        fun fromNBT(tag: CompoundTag, world: ServerLevel): ContinuationFrame =
            getCodec(world).parse(NbtOps.INSTANCE, tag).resultOrPartial(HexAPI.LOGGER::error).orElseThrow()

        /**
         * Takes a continuation frame and serializes it along with its type.
         */
        @Deprecated(
            "Use the codec instead.",
            replaceWith = ReplaceWith("serializeWithCodec(ContinuationFrame.getCodec())")
        )
        @JvmStatic
        fun toNBT(frame: ContinuationFrame): CompoundTag =
            getCodec().encodeStart(NbtOps.INSTANCE, frame).resultOrPartial(HexAPI.LOGGER::error)
                .orElseThrow() as CompoundTag
    }
}
