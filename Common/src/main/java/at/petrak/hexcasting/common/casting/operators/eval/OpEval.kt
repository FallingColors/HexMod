package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.CastingHarness
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpEval : Operator {
    override fun operate(continuation: SpellContinuation, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        val instrs: SpellList = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()

        ctx.incDepth()

        // if not installed already...
        val newCont = if (continuation is SpellContinuation.NotDone && continuation.frame is ContinuationFrame.FinishEval) {
            continuation
        } else {
            continuation.pushFrame(ContinuationFrame.FinishEval()) // install a break-boundary after eval
        }

        val frame = ContinuationFrame.Evaluate(instrs)
        return OperationResult(newCont.pushFrame(frame), stack, local, listOf())
    }
}
