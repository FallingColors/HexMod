package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getIntBetween
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.nbt.CompoundTag

object OpFishermanButItCopies : Action {
    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val depth = stack.getIntBetween(stack.lastIndex, -(stack.size - 2), stack.size - 2)
        stack.removeLast()

        if (depth >= 0) {
            val fish = stack[stack.size - 1 - depth]
            stack.add(fish)
        } else {
            val lure = stack.last()
            stack.add(stack.size - 1 + depth, lure)
        }

        return OperationResult(stack, userData, listOf(), continuation, 1)
    }
}
