package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.casting.Action
import at.petrak.hexcasting.api.casting.OperationResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota

object OpEvalDelay : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        return OperationResult(continuation, stack, ravenmind, listOf())
    }
}
