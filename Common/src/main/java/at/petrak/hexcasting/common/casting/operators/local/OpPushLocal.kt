package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.casting.Action
import at.petrak.hexcasting.api.casting.OperationResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs

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
