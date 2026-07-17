package at.petrak.hexcasting.common.casting.actions.eval

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.FrameForEach
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.TreeList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpForEach : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack

        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val instrs = stack.getList(stack.lastIndex - 1, stack.size)
        val datums = stack.getList(stack.lastIndex, stack.size)
        val newStack = stack.dropRight(2)

        val frame = FrameForEach(datums, instrs, null, TreeList.empty())
        val image2 = image.withUsedOp().copy(stack = newStack)

        return OperationResult(image2, listOf(), continuation.pushFrame(frame), HexEvalSounds.THOTH)
    }
}
