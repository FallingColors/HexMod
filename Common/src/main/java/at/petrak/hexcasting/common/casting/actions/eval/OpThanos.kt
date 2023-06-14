package at.petrak.hexcasting.common.casting.actions.eval

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.mod.HexConfig
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds

object OpThanos : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val opsLeft = HexConfig.server().maxOpCount() - image.opsConsumed
        val stack = image.stack.toMutableList()
        stack.add(DoubleIota(opsLeft.toDouble()))

        val image2 = image.withUsedOp().copy(stack = stack)
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}