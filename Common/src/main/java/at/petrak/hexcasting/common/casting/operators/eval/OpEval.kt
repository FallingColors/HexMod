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

object OpEval : Operator {
    override fun operate(continuation: MutableList<ContinuationFrame>, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        val instrs: SpellList = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()

        ctx.incDepth()

        continuation.add(ContinuationFrame.FinishEval()) // boundary for break
        continuation.add(ContinuationFrame.Evaluate(instrs))

        return OperationResult(stack, local, listOf())
    }
}
