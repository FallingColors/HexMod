package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.evaluatable
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.PatternIota

object OpEval : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
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

        val instrsList = instrs.map({ SpellList.LList(0, listOf(PatternIota(it))) }, { it })
        val frame = ContinuationFrame.Evaluate(instrsList)
        return OperationResult(newCont.pushFrame(frame), stack, ravenmind, listOf())
    }
}
