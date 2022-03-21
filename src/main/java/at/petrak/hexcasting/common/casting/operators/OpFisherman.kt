package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.OperationResult
import at.petrak.hexcasting.api.spell.Operator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect

object OpFisherman : Operator {
    val manaCost: Int
        get() = 0

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (stack.isEmpty())
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, 1, stack.size)
        val arg  = stack.takeLast(1).getChecked<Double>(0)
        val distance = stack.size - (arg + 1) // because getChecked<Int> just gives me a double for some reason
        stack.removeLast()
        if (distance < stack.size && Math.abs(distance.toInt() - distance) < 0.05f) {
            val fish = stack[distance.toInt()]
            stack.removeAt(distance.toInt())
            stack.add(stack.size, fish)
        } else {
            throw CastException(CastException.Reason.INVALID_VALUE, "integer less than " + stack.size + " but greater than 0", arg)
        }

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMana(this.manaCost))

        return OperationResult(stack, sideEffects)
    }
}