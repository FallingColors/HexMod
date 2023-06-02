package at.petrak.hexcasting.common.casting.actions.eval

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.FrameEvaluate
import at.petrak.hexcasting.api.casting.eval.vm.FrameFinishEval
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.evaluatable
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpEval : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()
        val iota = stack.removeLastOrNull() ?: throw MishapNotEnoughArgs(1, 0)
        return exec(env, image, continuation, stack, iota)
    }

    fun exec(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation, newStack: MutableList<Iota>, iota: Iota): OperationResult {
        // also, never make a break boundary when evaluating just one pattern
        val instrs = evaluatable(iota, 0)
        val newCont =
                if (instrs.left().isPresent || (continuation is SpellContinuation.NotDone && continuation.frame is FrameFinishEval)) {
                    continuation
                } else {
                    continuation.pushFrame(FrameFinishEval) // install a break-boundary after eval
                }

        val instrsList = instrs.map({ SpellList.LList(0, listOf(it)) }, { it })
        val frame = FrameEvaluate(instrsList, true)

        val image2 = image.withUsedOp().copy(stack = newStack)
        return OperationResult(image2, listOf(), newCont.pushFrame(frame), HexEvalSounds.HERMES)
    }
}
