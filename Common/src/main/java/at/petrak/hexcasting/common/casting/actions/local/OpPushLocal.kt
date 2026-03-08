package at.petrak.hexcasting.common.casting.actions.local

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.eval.vm.components.CastingImageComponents
import at.petrak.hexcasting.api.casting.eval.vm.components.GenericIotaComponent
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes
import net.minecraft.nbt.NbtOps
import kotlin.jvm.optionals.getOrElse

object OpPushLocal : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack

        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)

        val newLocal = stack.last()
        val newImage = if (newLocal.type == HexIotaTypes.NULL.get())
            image.withoutComponent(CastingImageComponents.RAVENMIND)
         else
            image.withComponent(CastingImageComponents.RAVENMIND, GenericIotaComponent(newLocal))

        return OperationResult(
            newImage.withUsedOp().copy(stack = stack.init()),
            listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE.get()
        )
    }
}
