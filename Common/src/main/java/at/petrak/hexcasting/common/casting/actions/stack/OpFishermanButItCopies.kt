package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpFishermanButItCopies : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        if (image.stack.size < 2)
            throw MishapNotEnoughArgs(2, image.stack.size)

        val depth = image.stack.getIntBetween(image.stack.lastIndex, -(image.stack.size - 2), image.stack.size - 2)
        var stack = image.stack.init()

        if (depth >= 0) {
            val fish = stack[stack.size - 1 - depth]
            stack = stack.appended(fish)
        } else {
            val lure = stack.last()
            stack = stack.dropRight(1 - depth).appended(lure).appendedAll(stack.takeRight(1 - depth))
        }

        val image2 = image.withUsedOp().copy(stack = stack)
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}
