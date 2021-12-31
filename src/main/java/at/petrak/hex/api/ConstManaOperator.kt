package at.petrak.hex.api

import at.petrak.hex.common.casting.CastException
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum

/**
 * A SimpleOperator that always costs the same amount of mana.
 */
interface ConstManaOperator : Operator {
    val argc: Int
    val manaCost: Int
        get() = 0

    fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>>

    override fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        // there's gotta be a better way to do this
        for (_idx in 0 until this.argc)
            stack.removeLast()
        val newData = this.execute(args, ctx)
        stack.addAll(newData)
        return OperationResult(this.manaCost, emptyList())
    }
}