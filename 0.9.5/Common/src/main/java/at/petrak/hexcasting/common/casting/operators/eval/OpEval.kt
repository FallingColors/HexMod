package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpEval : Operator {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<SpellDatum<*>>,
        local: SpellDatum<*>,
        ctx: CastingContext
    ): OperationResult {
        stack.getChecked<Any>(stack.lastIndex) // existence check
        val datum = stack.removeLast()
        val instrs = evaluatable(datum, 0)

        instrs.ifRight {
            ctx.incDepth()
        }

        // if not installed already...
        // also, never make a break boundary when evaluating just one pattern
        val newCont =
            if (instrs.left().isPresent || (continuation is SpellContinuation.NotDone && continuation.frame is ContinuationFrame.FinishEval)) {
                continuation
            } else {
                continuation.pushFrame(ContinuationFrame.FinishEval) // install a break-boundary after eval
            }

        val instrsList = instrs.map({ SpellList.LList(0, spellListOf(it)) }, { it!! })
        val frame = ContinuationFrame.Evaluate(instrsList)
        return OperationResult(newCont.pushFrame(frame), stack, local, listOf())
    }
}
