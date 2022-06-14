package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation

object OpEvalDelay : Action {
    override fun operate(continuation: SpellContinuation, stack: MutableList<Iota>, local: Iota, ctx: CastingContext): OperationResult {
        return OperationResult(continuation, stack, local, listOf())
    }
}
