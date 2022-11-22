package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

object OpPushLocal : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)
        val newLocal = stack.removeLast()
        return OperationResult(continuation, stack, newLocal, listOf())
    }
}
