package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

object OpPushLocal : Operator {
    override fun operate(
        stack: MutableList<SpellDatum<*>>,
        local: SpellDatum<*>,
        ctx: CastingContext
    ): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)
        val newLocal = stack.removeLast()
        return OperationResult(stack, newLocal, listOf())
    }
}
