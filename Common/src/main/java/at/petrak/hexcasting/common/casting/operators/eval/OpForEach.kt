package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

object OpForEach : Operator {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<LegacySpellDatum<*>>,
        local: LegacySpellDatum<*>,
        ctx: CastingContext
    ): OperationResult {
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
