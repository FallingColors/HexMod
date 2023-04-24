package at.petrak.hexcasting.common.casting.operators.eval

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.nbt.CompoundTag

object OpThanos : Action {
    override fun operate(env: CastingEnvironment, stack: MutableList<Iota>, userData: CompoundTag, continuation: SpellContinuation): OperationResult {
        throw NotImplementedError()
    }
}