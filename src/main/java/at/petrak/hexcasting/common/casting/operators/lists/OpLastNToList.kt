package at.petrak.hexcasting.common.casting.operators.lists

import at.petrak.hexcasting.api.OperationResult
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect

object OpLastNToList : Operator {
    val manaCost: Int
        get() = 0

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (stack.isEmpty())
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, 1, stack.size)
        val arg = stack.takeLast(1).getChecked<Double>(0)
        stack.removeLast()
        if (arg < 0) {
            throw CastException(CastException.Reason.INVALID_VALUE, "integer greater than 0", arg)
        }
        val output = emptyList<SpellDatum<*>>().toMutableList()
        output.addAll(stack.takeLast(arg.toInt()))
        val endSize = stack.size - output.toList().size
        while (stack.size != endSize) {
            stack.removeLast()
        }
        stack.addAll(spellListOf(output))

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMana(this.manaCost))

        return OperationResult(stack, sideEffects)
    }
}