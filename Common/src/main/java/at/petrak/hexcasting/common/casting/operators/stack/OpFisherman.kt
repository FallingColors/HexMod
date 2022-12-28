package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.Action
import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.eval.SpellContinuation
import at.petrak.hexcasting.api.spell.iota.DoubleIota
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import kotlin.math.abs
import kotlin.math.roundToInt

object OpFisherman : Action {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<Iota>,
        ravenmind: Iota?,
        ctx: CastingContext
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

        return OperationResult(continuation, stack, ravenmind, listOf())
    }
}
