package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingHarness.CastResult
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.ListIota
import at.petrak.hexcasting.api.utils.NBTBuilder
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.hasList
import at.petrak.hexcasting.api.utils.serializeToNBT
import at.petrak.hexcasting.common.lib.HexIotaTypes
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerLevel

// TODO this should probably be a registry too
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
 */
sealed interface ContinuationFrame {
    /**
     * Step the evaluation forward once.
     * For Evaluate, this consumes one pattern; for ForEach this queues the next iteration of the outer loop.
     * @return the result of this pattern step
     */
    fun evaluate(continuation: SpellContinuation, level: ServerLevel, harness: CastingHarness): CastResult

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
     * A list of patterns to be evaluated in sequence.
     * @property list the *remaining* list of patterns to be evaluated
     */
    data class Evaluate(val list: SpellList) : ContinuationFrame {
        // Discard this frame and keep discarding frames.
        override fun breakDownwards(stack: List<Iota>) = false to stack

        // Step the list of patterns, evaluating a single one.
        override fun evaluate(
            continuation: SpellContinuation,
            level: ServerLevel,
            harness: CastingHarness
        ): CastResult {
            // If there are patterns left...
            if (list.nonEmpty) {
                val newCont = if (list.cdr.nonEmpty) { // yay TCO
                    // ...enqueue the evaluation of the rest of the patterns...
                    continuation.pushFrame(Evaluate(list.cdr))
                } else continuation
                // ...before evaluating the first one in the list.
                return harness.getUpdate(list.car, level, newCont)
            } else {
                // If there are no patterns (e.g. empty Hermes), just return OK.
                return CastResult(continuation, null, ResolvedPatternType.EVALUATED, listOf())
            }
        }

        override fun serializeToNBT() = NBTBuilder {
            "type" %= "evaluate"
            "patterns" %= list.serializeToNBT()
        }
    }

    /**
     * A stack marker representing the end of a Hermes evaluation,
     * so that we know when to stop removing frames during a Halt.
     */
    object FinishEval : ContinuationFrame {
        // Don't do anything else to the stack, just finish the halt statement.
        override fun breakDownwards(stack: List<Iota>) = true to stack

        // Evaluating it does nothing; it's only a boundary condition.
        override fun evaluate(
            continuation: SpellContinuation,
            level: ServerLevel,
            harness: CastingHarness
        ): CastResult {
            return CastResult(
                continuation,
                FunctionalData(harness.stack.toList(), 0, listOf(), false, harness.ravenmind),
                ResolvedPatternType.EVALUATED,
                listOf()
            )
        }

        override fun serializeToNBT() = NBTBuilder {
            "type" %= "end"
        }
    }

    /**
     * A frame representing all the state for a Thoth evaluation.
     * Pushed by an OpForEach.
     * @property first whether the input stack state is the first one (since we don't want to save the base-stack before any changes are made)
     * @property data list of *remaining* datums to ForEach over
     * @property code code to run per datum
     * @property baseStack the stack state at Thoth entry
     * @property acc concatenated list of final stack states after Thoth exit
     */
    data class ForEach(
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
            harness: CastingHarness
        ): CastResult {
            // If this isn't the very first Thoth step (i.e. no Thoth computations run yet)...
            val stack = if (baseStack == null) {
                // init stack to the harness stack...
                harness.stack.toList()
            } else {
                // else save the stack to the accumulator and reuse the saved base stack.
                acc.addAll(harness.stack)
                baseStack
            }

            // If we still have data to process...
            val (stackTop, newCont) = if (data.nonEmpty) {
                // Push the next datum to the top of the stack,
                data.car to continuation
                    // put the next Thoth object back on the stack for the next Thoth cycle,
                    .pushFrame(ForEach(data.cdr, code, stack, acc))
                    // and prep the Thoth'd code block for evaluation.
                    .pushFrame(Evaluate(code))
            } else {
                // Else, dump our final list onto the stack.
                ListIota(acc) to continuation
            }
            val tStack = stack.toMutableList()
            tStack.add(stackTop)
            return CastResult(
                newCont,
                FunctionalData(tStack, 0, listOf(), false, harness.ravenmind),
                ResolvedPatternType.EVALUATED,
                listOf()
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

    companion object {
        @JvmStatic
        fun fromNBT(tag: CompoundTag, world: ServerLevel): ContinuationFrame {
            return when (tag.getString("type")) {
                "eval" -> Evaluate(
                    HexIotaTypes.LIST.deserialize(
                        tag.getList("patterns", Tag.TAG_COMPOUND),
                        world
                    )!!.list
                )
                "end" -> FinishEval
                "foreach" -> ForEach(
                    HexIotaTypes.LIST.deserialize(tag.getList("data", Tag.TAG_COMPOUND), world)!!.list,
                    HexIotaTypes.LIST.deserialize(tag.getList("code", Tag.TAG_COMPOUND), world)!!.list,
                    if (tag.hasList("base", Tag.TAG_COMPOUND))
                        HexIotaTypes.LIST.deserialize(tag.getList("base", Tag.TAG_COMPOUND), world)!!.list.toList()
                    else
                        null,
                    HexIotaTypes.LIST.deserialize(
                        tag.getList("accumulator", Tag.TAG_COMPOUND),
                        world
                    )!!.list.toMutableList()
                )
                else -> Evaluate(SpellList.LList(0, listOf()))
            }
        }
    }
}
