package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingHarness.CastResult
import net.minecraft.server.level.ServerLevel

/**
 * A single frame of evaluation during the 
 */
sealed interface ContinuationFrame {
    fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult
    /** @return whether the break should stop here */
    fun breakDownwards(stack: List<SpellDatum<*>>): Pair<Boolean, List<SpellDatum<*>>>

    data class Evaluate(var list: SpellList): ContinuationFrame {
        override fun breakDownwards(stack: List<SpellDatum<*>>) = Pair(false, stack)

        override fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult {
            if (list.nonEmpty) {
                val toEval = list.car
                list = list.cdr
                if (list.nonEmpty) { // yay TCO
                    continuation.add(this)
                }
                return harness.getUpdate(toEval, level, continuation)
            } else {
                return CastResult(null, listOf())
            }
        }

    }

    // I'd put this in the else-branch for Evaluate, but we want to properly set the ctn boundary for break.
    class FinishEval(): ContinuationFrame {
        override fun breakDownwards(stack: List<SpellDatum<*>>) = Pair(true, stack)

        override fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult {
            return CastResult(FunctionalData(harness.stack.toList(), 0, listOf(), false), listOf())
        }
    }

    data class ForEach(var first: Boolean, var data: SpellList, val code: SpellList, val baseStack: List<SpellDatum<*>>, var acc: MutableList<SpellDatum<*>>): ContinuationFrame {
        fun appendBase(iota: SpellDatum<*>): List<SpellDatum<*>> {
            val mutStack = baseStack.toMutableList()
            mutStack.add(iota)
            return mutStack
        }

        override fun breakDownwards(stack: List<SpellDatum<*>>): Pair<Boolean, List<SpellDatum<*>>> {
            acc.addAll(stack)
            return Pair(true, appendBase(SpellDatum.make(acc)))
        }

        override fun evaluate(continuation: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness): CastResult {
            if (!first) {
                acc.addAll(harness.stack)
            }
            first = false
            val stackTop = if (data.nonEmpty) {
                // queue next datum for Thoth eval
                val toEval = data.car
                data = data.cdr
                continuation.add(this)
                continuation.add(Evaluate(code))
                toEval
            } else {
                // dump our final list onto the stack
                SpellDatum.make(acc)
            }
            return CastResult(FunctionalData(appendBase(stackTop), 0, listOf(), false), listOf())
        }
    }
}
