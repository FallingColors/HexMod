package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame

object OpEvalDelay : Operator {
    override fun operate(continuation: MutableList<ContinuationFrame>, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        return OperationResult(stack, local, listOf())
    }
}
