package at.petrak.hexcasting.api

import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext

interface SpellOperator : Operator {
    val argc: Int
    fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int>

    override fun modifyStack(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_idx in 0 until this.argc)
            stack.removeLast()
        val (spell, mana) = this.execute(args, ctx)
        return OperationResult(mana, listOf(spell))
    }
}