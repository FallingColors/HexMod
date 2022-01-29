package at.petrak.hexcasting.api

import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect

/**
 * A SimpleOperator that always costs the same amount of mana.
 */
interface ConstManaOperator : Operator {
    val argc: Int
    val manaCost: Int
        get() = 0

    fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>>

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, this.argc, stack.size)
        val args = stack.dropLast(this.argc)
        val newData = this.execute(args, ctx)
        stack.addAll(newData)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMana(this.manaCost))

        return OperationResult(stack, sideEffects)
    }
}