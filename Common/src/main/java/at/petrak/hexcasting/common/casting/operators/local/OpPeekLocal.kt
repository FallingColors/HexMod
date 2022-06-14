package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpPeekLocal : Operator {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        local: Iota,
        ctx: CastingContext
    ): OperationResult {
        stack.add(local)
        return OperationResult(continuation, stack, local, listOf())
    }
}
