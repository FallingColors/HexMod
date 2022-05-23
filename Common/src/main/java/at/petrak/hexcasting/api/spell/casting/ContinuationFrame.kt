package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingHarness.CastResult

/**
 * A single frame of evaluation during the 
 */
sealed interface ContinuationFrame {
    fun evaluate(stack: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness) -> CastResult
    /** @return whether the break should stop here */
    fun breakDownwards(): Boolean

    class Evaluate(var list: SpellList): ContinuationFrame {
        override fun breakDownwards() = false

        fun evaluate(stack: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness) -> CastResult {
            if (list.nonEmpty) {
                val toEval = list.car
                list = list.cdr
                if (list.nonEmpty) { // yay TCO
                    stack.push(this)
                }
                return harness.getUpdate(toEval, level, stack)
            } else {
                return CastResult(harness.getFunctionalData(), listOf())
            }
        }

    }
    class FinishEval(): ContinuationFrame {
        override fun breakDownwards() = true // TODO: properly reset state

        fun evaluate(stack: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness) -> CastResult {
            return CastResult(FunctionalData(harness.stack, 0, listOf(), false), listOf())
        }
    }
    class ForEach(var first: Boolean, var data: SpellList, val code: SpellList, val baseStack: List<SpellDatum<*>>, var acc: MutableList<SpellDatum<*>>): ContinuationFrame {
        override fun breakDownwards() = true // TODO: properly reset state

        fun evaluate(stack: MutableList<ContinuationFrame>, level: ServerLevel, harness: CastingHarness) -> CastResult {
            if (data.nonEmpty) {
                val toEval = data.car
                data = data.cdr
                if (!first) {
                    acc.addAll(stack)
                }
                first = false
                stack.push(this)
                return harness.getUpdate(toEval, level, baseStack)
            } else {
                val newStack = baseStack.toMutableList()
                acc.addAll(stack)
                newStack.add(SpellDatum.make(acc))
                return CastResult(FunctionalData(newStack, 0, listOf(), false), listOf())
            }
        }
    }
}
