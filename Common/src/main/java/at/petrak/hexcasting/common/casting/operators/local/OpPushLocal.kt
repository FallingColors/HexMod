package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.nbt.CompoundTag

object OpPushLocal : Action {
    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)

        val newLocal = stack.removeLast()
        userData.put(HexAPI.RAVENMIND_USERDATA, newLocal.serialize())

        return OperationResult(stack, userData, listOf(), continuation)
    }
}
