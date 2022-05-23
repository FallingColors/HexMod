package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingHarness.CastResult
import net.minecraft.server.level.ServerLevel

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
    fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult
    /**
     * The OpHalt instruction wants us to "jump to" the END of the nearest meta-eval.
     * In other words, we should consume Evaluate frames until we hit a FinishEval or Thoth frame.
     * @return whether the break should stop here, alongside the new stack state (e.g. for finalizing a Thoth)
     */
    fun breakDownwards(stack: List<SpellDatum<*>>): Pair<Boolean, List<SpellDatum<*>>>

    /**
     * A list of patterns to be evaluated in sequence.
     * @property list the *remaining* list of patterns to be evaluated
     */
    data class Evaluate(var list: SpellList): ContinuationFrame {
        // Discard this frame and keep discarding frames.
        override fun breakDownwards(stack: List<SpellDatum<*>>) = Pair(false, stack)

        // Step the list of patterns, evaluating a single one.
        override fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult {
            // If there are patterns left...
            if (list.nonEmpty) {
                val toEval = list.car
                list = list.cdr
                if (list.nonEmpty) { // yay TCO
                    // ...enqueue the evaluation of the rest of the patterns...
                    continuation.add(this)
                }
                // ...before evaluating the first one in the list.
                return harness.getUpdate(toEval, level, continuation)
            } else {
                // If there are no patterns (e.g. empty Hermes), just return OK.
                return CastResult(null, ResolvedPatternType.OK, listOf())
            }
        }

    }

    /**
     * A stack marker representing the end of a Hermean evaluation,
     * so that we know when to stop removing frames during a Halt.
     */
    class FinishEval(): ContinuationFrame {
        // Don't do anything else to the stack, just finish the halt statement.
        override fun breakDownwards(stack: List<SpellDatum<*>>) = Pair(true, stack)

        // Evaluating it does nothing; it's only a boundary condition.
        override fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult {
            return CastResult(FunctionalData(harness.stack.toList(), 0, listOf(), false), ResolvedPatternType.OK, listOf())
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
    data class ForEach(var first: Boolean, var data: SpellList, val code: SpellList, val baseStack: List<SpellDatum<*>>, var acc: MutableList<SpellDatum<*>>): ContinuationFrame {
        /** Helper to append something to the original stack. */
        fun appendBase(iota: SpellDatum<*>): List<SpellDatum<*>> {
            val mutStack = baseStack.toMutableList()
            mutStack.add(iota)
            return mutStack
        }

        /** When halting, we add the stack state at halt to the stack accumulator, then return the original pre-Thoth stack, plus the accumulator. */
        override fun breakDownwards(stack: List<SpellDatum<*>>): Pair<Boolean, List<SpellDatum<*>>> {
            acc.addAll(stack)
            return Pair(true, appendBase(SpellDatum.make(acc)))
        }

        /** Step the Thoth computation, enqueueing one code evaluation. */
        override fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult {
            // If this isn't the very first Thoth step (i.e. no Thoth computations run yet)...
            if (!first) {
                // add everything we've added 
                acc.addAll(harness.stack)
            }
            first = false
            // If we still have data to process...
            val stackTop = if (data.nonEmpty) {
                // queue next datum for Thoth eval.
                val toEval = data.car
                data = data.cdr
                // Put this Thoth object: back on the stack for the next Thoth cycle,
                continuation.add(this)
                // and prep the Thoth'd code block for evaluation.
                continuation.add(Evaluate(code))
                // Push the next datum to the top of the stack.
                toEval
            } else {
                // Else, dump our final list onto the stack.
                SpellDatum.make(acc)
            }
            return CastResult(FunctionalData(appendBase(stackTop), 0, listOf(), false), ResolvedPatternType.OK, listOf())
        }
    }
}
