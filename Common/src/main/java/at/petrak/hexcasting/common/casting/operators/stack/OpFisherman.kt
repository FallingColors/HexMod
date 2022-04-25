package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs
import net.minecraft.network.chat.TranslatableComponent
import kotlin.math.abs
import kotlin.math.roundToInt

object OpFisherman : Operator {
    val manaCost: Int
        get() = 0

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (stack.isEmpty())
            throw MishapNotEnoughArgs(1, 0)
        val arg = stack.getChecked<Double>(stack.lastIndex)
        val datum = stack[stack.lastIndex]
        val distance = stack.size - (arg + 1) // because getChecked<Int> just gives me a double for some reason
        stack.removeLast()
        if (distance < stack.size && abs(distance.roundToInt() - distance) < 0.05f) {
            val fish = stack[distance.roundToInt()]
            stack.removeAt(distance.roundToInt())
            stack.add(stack.size, fish)
        } else {
            throw MishapInvalidIota(
                datum,
                0,
                TranslatableComponent("hexcasting.mishap.invalid_value.int.between", 0, stack.size)
            )
        }

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMana(manaCost))

        return OperationResult(stack, sideEffects)
    }
}
