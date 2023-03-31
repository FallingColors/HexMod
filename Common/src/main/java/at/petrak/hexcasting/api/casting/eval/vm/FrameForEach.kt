package at.petrak.hexcasting.api.casting.eval.vm

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.eval.CastResult
import at.petrak.hexcasting.api.casting.eval.ResolvedPatternType
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.server.level.ServerLevel

/**
 * A frame representing all the state for a Thoth evaluation.
 * Pushed by an OpForEach.
 * @property first whether the input stack state is the first one (since we don't want to save the base-stack before any changes are made)
 * @property data list of *remaining* datums to ForEach over
 * @property code code to run per datum
 * @property baseStack the stack state at Thoth entry
 * @property acc concatenated list of final stack states after Thoth exit
 */
data class FrameForEach(
    val data: SpellList,
    val code: SpellList,
    val baseStack: List<Iota>?,
    val acc: MutableList<Iota>
) : ContinuationFrame {

    /** When halting, we add the stack state at halt to the stack accumulator, then return the original pre-Thoth stack, plus the accumulator. */
    override fun breakDownwards(stack: List<Iota>): Pair<Boolean, List<Iota>> {
        val newStack = baseStack?.toMutableList() ?: mutableListOf()
        acc.addAll(stack)
        newStack.add(ListIota(acc))
        return true to newStack
    }

    /** Step the Thoth computation, enqueueing one code evaluation. */
    override fun evaluate(
        continuation: SpellContinuation,
        level: ServerLevel,
        harness: CastingVM
    ): CastResult {
        // If this isn't the very first Thoth step (i.e. no Thoth computations run yet)...
        val stack = if (baseStack == null) {
            // init stack to the harness stack...
            harness.image.stack.toList()
        } else {
            // else save the stack to the accumulator and reuse the saved base stack.
            acc.addAll(harness.image.stack)
            baseStack
        }

        // If we still have data to process...
        val (stackTop, newImage, newCont) = if (data.nonEmpty) {
            // Increment the evaluation depth,
            // push the next datum to the top of the stack,
            Triple(data.car, harness.image.copy(userData = CastingImage.incDepth(harness.image.userData)), continuation
                // put the next Thoth object back on the stack for the next Thoth cycle,
                .pushFrame(FrameForEach(data.cdr, code, stack, acc))
                // and prep the Thoth'd code block for evaluation.
                .pushFrame(FrameEvaluate(code, true)))
        } else {
            // Else, dump our final list onto the stack.
            Triple(ListIota(acc), harness.image, continuation)
        }
        val tStack = stack.toMutableList()
        tStack.add(stackTop)
        // TODO: this means we could have Thoth casting do a different sound
        return CastResult(
            newCont,
            newImage.copy(stack = tStack),
            listOf(),
            ResolvedPatternType.EVALUATED,
            HexEvalSounds.THOTH,
        )
    }

    override fun serializeToNBT() = NBTBuilder {
        "type" %= "foreach"
        "data" %= data.serializeToNBT()
        "code" %= code.serializeToNBT()
        if (baseStack != null)
            "base" %= baseStack.serializeToNBT()
        "accumulator" %= acc.serializeToNBT()
    }
}
