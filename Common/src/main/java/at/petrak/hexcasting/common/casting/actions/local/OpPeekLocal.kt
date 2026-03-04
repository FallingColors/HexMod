package at.petrak.hexcasting.common.casting.actions.local

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.NbtOps
import kotlin.jvm.optionals.getOrElse

object OpPeekLocal : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        val stack = image.stack.toMutableList()
        val ravenmind = image.ravenmind()

        val rm = if (ravenmind.isPresent) {
            IotaType.TYPED_CODEC.parse(NbtOps.INSTANCE, ravenmind.get()).orThrow
        } else {
            NullIota()
        }

        stack.add(rm)

        // does not mutate userdata
        val image2 = image.withUsedOp().copy(stack = stack)
        return OperationResult(image2, listOf(), continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}
