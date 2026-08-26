package at.petrak.hexcasting.common.casting.actions.escaping

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getPositiveInt
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpOpenNParens : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val newStack = image.stack.toMutableList()
        val layers = newStack.getPositiveInt(newStack.lastIndex)
        newStack.removeLast()
        val image2 = image.withUsedOp().copy(
            stack = newStack,
            parenCount = layers
        )
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }

    // Since there's no nice way to determine how many new layers it should open when drawn inside parens, we don't
    // override operateInParens() at all. This pattern is just treated as any other pattern when parenthesized.
}