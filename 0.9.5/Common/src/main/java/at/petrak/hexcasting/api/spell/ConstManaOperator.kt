package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.casting.SpellContinuation
import at.petrak.hexcasting.api.spell.casting.OperatorSideEffect
import at.petrak.hexcasting.api.spell.mishaps.MishapNotEnoughArgs

/**
 * A SimpleOperator that always costs the same amount of mana.
 */
interface ConstManaOperator : Operator {
    val argc: Int
    val manaCost: Int
        get() = 0

    fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>>

    override fun operate(continuation: SpellContinuation, stack: MutableList<SpellDatum<*>>, local: SpellDatum<*>, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_i in 0 until this.argc) stack.removeLast()
        val newData = this.execute(args, ctx)
        stack.addAll(newData)

        val sideEffects = mutableListOf<OperatorSideEffect>(OperatorSideEffect.ConsumeMana(this.manaCost))

        return OperationResult(continuation, stack, local, sideEffects)
    }
}
