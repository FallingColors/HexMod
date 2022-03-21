package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext

object OpEvalDelay : Operator {
    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        return OperationResult(stack, listOf())
    }
}