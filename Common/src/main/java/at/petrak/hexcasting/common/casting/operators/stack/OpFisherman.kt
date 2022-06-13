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
        val distance = stack.size - (arg + 1) // because getChecked<Int> just gives me a double for some reason
        stack.removeLast()
        if (distance >= 0 && distance < stack.size && abs(distance.roundToInt() - distance) < 0.05f) {
            val fish = stack[distance.roundToInt()]
            stack.removeAt(distance.roundToInt())
            stack.add(stack.size, fish)
        } else {
            throw MishapInvalidIota(
                datum,
                0,
                "hexcasting.mishap.invalid_value.int.between".asTranslatedComponent(1, stack.size)
            )
        }

        return OperationResult(continuation, stack, local, listOf())
    }
}
