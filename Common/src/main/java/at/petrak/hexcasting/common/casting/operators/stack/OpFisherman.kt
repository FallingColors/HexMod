package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs
import net.minecraft.nbt.CompoundTag
import kotlin.math.abs
import kotlin.math.roundToInt

object OpFisherman : Action {
    override fun operate(
        env: CastingEnvironment,
        stack: MutableList<Iota>,
        userData: CompoundTag,
        continuation: SpellContinuation
    ): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)

        val depth = let {
            val x = stack.last()
            val maxIdx = stack.size - 1
            if (x is DoubleIota) {
                val double = x.double
                val rounded = double.roundToInt()
                if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in 1..maxIdx) {
                    return@let rounded
                }
            }
            throw MishapInvalidIota.of(x, 0, "double.between", 1, maxIdx)
        }

        stack.removeLast()
        val fish = stack.removeAt(stack.size - depth)
        stack.add(fish)

        return OperationResult(stack, userData, listOf(), continuation)
    }
}
