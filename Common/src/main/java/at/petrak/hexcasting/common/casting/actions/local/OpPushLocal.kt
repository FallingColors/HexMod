package at.petrak.hexcasting.common.casting.actions.local

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.common.lib.hex.HexImageComponents
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes

object OpPushLocal : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack

        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)

        val newLocal = stack.last()
        val newImage = if (newLocal.type == HexIotaTypes.NULL.get())
            image.withoutComponent(HexImageComponents.RAVENMIND.get())
         else
            image.withComponent(HexImageComponents.RAVENMIND.get(), newLocal)

        return OperationResult(
            newImage.withUsedOp().copy(stack = stack.init()),
            listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE.get()
        )
    }
}
