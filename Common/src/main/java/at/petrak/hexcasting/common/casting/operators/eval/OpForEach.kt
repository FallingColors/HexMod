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
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpForEach : Operator {
    override fun operate(continuation: SpellContinuation, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val instrs: SpellList = stack.getChecked(stack.lastIndex - 1)
        val datums: SpellList = stack.getChecked(stack.lastIndex)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        val frame = ContinuationFrame.ForEach(datums, instrs, null, mutableListOf())

        return OperationResult(
            continuation.pushFrame(frame),
            stack,
            local,
            listOf()
        )
    }
}
