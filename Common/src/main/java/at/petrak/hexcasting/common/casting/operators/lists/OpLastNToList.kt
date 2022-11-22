package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

object OpLastNToList : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)
        val yoinkCount = stack.takeLast(1).getPositiveIntUnderInclusive(0, stack.size - 1)
        stack.removeLast()
        val output = mutableListOf<Iota>()
        output.addAll(stack.takeLast(yoinkCount))
        for (i in 0 until yoinkCount) {
            stack.removeLast()
        }
        stack.addAll(output.asActionResult)

        return OperationResult(continuation, stack, ravenmind, listOf())
    }
}
