package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

object OpFishermanButItCopies : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val depth = stack.getPositiveIntUnderInclusive(stack.lastIndex, stack.size - 2)
        stack.removeLast()
        val fish = stack.get(stack.size - 1 - depth)
        stack.add(fish)

        return OperationResult(continuation, stack, ravenmind, listOf())
    }
}
