package at.petrak.hexcasting.common.casting.actions.eval

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getEvaluatable
import at.petrak.hexcasting.api.casting.iota.ContinuationIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs

object OpEvalBreakable : Action {
    override fun operate(env: CastingEnvironment,
                         image: CastingImage,
                         continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()

        if (stack.size < 1)
            throw MishapNotEnoughArgs(1, 0)

        val instrs = stack.getEvaluatable(stack.lastIndex, stack.size)
        stack.removeLastOrNull()

        stack.add(ContinuationIota(continuation))
        return OpEval.exec(env, image, continuation, stack, instrs)
    }
}
