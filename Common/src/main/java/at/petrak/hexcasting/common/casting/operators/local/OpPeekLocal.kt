package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.casting.Action
import at.petrak.hexcasting.api.casting.OperationResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.orNull

object OpPeekLocal : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        stack.add(ravenmind.orNull())
        return OperationResult(continuation, stack, ravenmind, listOf())
    }
}
