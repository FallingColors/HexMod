package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import kotlin.math.abs
import kotlin.math.roundToInt

object OpFisherman : Operator {
    override fun operate(
        continuation: SpellContinuation,
        stack: MutableList<SpellDatum<*>>,
        local: SpellDatum<*>,
        ctx: CastingContext
    ): OperationResult {
        if (stack.size < 2)
            throw MishapNotEnoughArgs(2, stack.size)
        val arg = stack.getChecked<Double>(stack.lastIndex)
        val datum = stack[stack.lastIndex]
        stack.removeLast()

        if (arg.roundToInt() == 0 || abs(arg) > stack.size || abs(arg.roundToInt() - arg) > 0.05f) {
            throw MishapInvalidIota(
                datum,
                0,
                "hexcasting.mishap.invalid_value.int.nonzero.between".asTranslatedComponent(-stack.size, stack.size)
            )
        }
        else if (arg > 0) {
            val distance = (stack.size - arg).roundToInt()
            val fish = stack.removeAt(distance)
            stack.add(stack.size, fish)
        }
        else /* if (arg < 0) */ {
            val depth = (stack.size + arg).roundToInt()
            val lure = stack.removeLast()
            stack.add(depth, lure)
        }

        return OperationResult(continuation, stack, local, listOf())
    }
}
