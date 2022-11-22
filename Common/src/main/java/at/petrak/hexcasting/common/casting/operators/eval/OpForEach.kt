package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.FrameForEach
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.getList
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

object OpForEach : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
    ): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val instrs = stack.getList(stack.lastIndex - 1)
        val datums = stack.getList(stack.lastIndex)
        stack.removeLastOrNull()
        stack.removeLastOrNull()

        val frame = FrameForEach(datums, instrs, null, mutableListOf())

        return OperationResult(
            continuation.pushFrame(frame),
            stack,
            ravenmind,
            listOf()
        )
    }
}
