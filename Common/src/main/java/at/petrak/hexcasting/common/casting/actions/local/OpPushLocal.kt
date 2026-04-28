package at.petrak.hexcasting.common.casting.actions.local

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes

object OpPushLocal : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack

        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)

        val newLocal = stack.last()
        if (newLocal.type == HexIotaTypes.NULL)
            image.userData.remove(HexAPI.RAVENMIND_USERDATA)
         else
            image.userData.put(HexAPI.RAVENMIND_USERDATA, IotaType.serialize(newLocal))

        val image2 = image.withUsedOp().copy(stack = stack.init())
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}
