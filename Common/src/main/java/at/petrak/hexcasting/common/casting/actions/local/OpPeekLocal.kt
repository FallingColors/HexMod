package at.petrak.hexcasting.common.casting.actions.local

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexImageComponents

object OpPeekLocal : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack
        val newStack = stack.appended(image.getComponent(HexImageComponents.RAVENMIND.get()) ?: NullIota())
        return OperationResult(
            image.withUsedOp().copy(stack = newStack),
            listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE.get()
        )
    }
}
