package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import net.minecraft.nbt.CompoundTag

object OpPeekLocal : Action {
    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        val rm = if (userData.contains(HexAPI.RAVENMIND_USERDATA)) {
            IotaType.deserialize(userData.getCompound(HexAPI.RAVENMIND_USERDATA), env.world)
        } else {
            NullIota()
        }
        stack.add(rm)
        return OperationResult(stack, userData, listOf(), continuation)
    }
}
