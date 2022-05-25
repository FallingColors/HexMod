package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpEvalDelay : Operator {
    override fun operate(continuation: SpellContinuation, stack: MutableList<LegacySpellDatum<*>>, local: LegacySpellDatum<*>, ctx: CastingContext): OperationResult {
        return OperationResult(continuation, stack, local, listOf())
    }
}
