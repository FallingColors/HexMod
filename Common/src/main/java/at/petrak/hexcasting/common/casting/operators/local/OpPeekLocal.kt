package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.ContinuationFrame

object OpPeekLocal : Operator {
    override fun operate(
        continuation: MutableList<ContinuationFrame>,
        stack: MutableList<SpellDatum<*>>,
        local: SpellDatum<*>,
        ctx: CastingContext
    ): OperationResult {
        stack.add(local)
        return OperationResult(stack, local, listOf())
    }
}
