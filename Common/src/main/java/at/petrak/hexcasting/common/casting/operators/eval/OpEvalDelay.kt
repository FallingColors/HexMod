package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpEvalDelay : Operator {
    override fun operate(continuation: SpellContinuation, stack: MutableList<Iota>, local: Iota, ctx: CastingContext): OperationResult {
        return OperationResult(continuation, stack, local, listOf())
    }
}
