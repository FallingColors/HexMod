package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpPushLocal : Operator {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<LegacySpellDatum<*>>,
        local: LegacySpellDatum<*>,
        ctx: CastingContext
    ): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)
        val newLocal = stack.removeLast()
        return OperationResult(continuation, stack, newLocal, listOf())
    }
}
