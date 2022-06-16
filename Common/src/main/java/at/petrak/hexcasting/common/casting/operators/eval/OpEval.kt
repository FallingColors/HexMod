package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.iota.Iota

object OpEval : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        val instrs = stack.getList(stack.lastIndex)
        stack.removeLastOrNull()

        ctx.incDepth()

        // if not installed already...
        val newCont =
            if (continuation is SpellContinuation.NotDone && continuation.frame is ContinuationFrame.FinishEval) {
                continuation
            } else {
                continuation.pushFrame(ContinuationFrame.FinishEval) // install a break-boundary after eval
            }

        val frame = ContinuationFrame.Evaluate(instrs)
        return OperationResult(newCont.pushFrame(frame), stack, ravenmind, listOf())
    }
}
