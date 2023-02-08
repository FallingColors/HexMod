package at.petrak.hexcasting.common.casting.operators.local

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.orNull
import net.minecraft.nbt.CompoundTag

object OpPeekLocal : Action {
    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        // TODO winfy: figure out ravenmind semantics
        stack.add(userData.getCompound(CastingEnvironment.TAG_RAVENMIND))
        //IotaType.deserialize(subtag.downcast(CompoundTag.TYPE), world)
        return OperationResult(stack, userData, listOf(), continuation)
    }
}
