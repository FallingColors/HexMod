package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getPositiveIntUnderInclusive
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpDuplicateMany : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)
        val count = stack.takeLast(1).getPositiveIntUnderInclusive(0, stack.size - 1)
        stack.removeAt(stack.lastIndex)
        for (iota in stack.takeLast(count))
            stack.add(iota)
        return OperationResult(image.withUsedOp().copy(stack = stack), listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}
