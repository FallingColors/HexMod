package at.petrak.hex.common.casting.operators

import at.petrak.hex.common.casting.CastException
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import at.petrak.hex.common.casting.SpellOperator

/**
 * An operator that acts in the expected method of popping some arguments
 * and pushing some more arguments.
 */
interface SimpleOperator : SpellOperator {
    val argc: Int
    fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>>

    override fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext) {
        if (this.argc > stack.size)
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        // there's gotta be a better way to do this
        for (_idx in 0 until this.argc)
            stack.removeLast()
        val newData = this.execute(args, ctx)
        stack.addAll(newData)
    }
}