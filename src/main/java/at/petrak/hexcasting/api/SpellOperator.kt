package at.petrak.hexcasting.api

import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect

interface SpellOperator : Operator {
    val argc: Int

    fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_i in 0 until this.argc) stack.removeLast()
        val (spell, mana, particles) = this.execute(args, ctx)

        val sideEffects = mutableListOf(
            OperatorSideEffect.ConsumeMana(mana),
            OperatorSideEffect.AttemptSpell(spell, this.isGreat)
        )
        for (spray in particles) {
            sideEffects.add(OperatorSideEffect.Particles(spray))
        }

        return OperationResult(stack, sideEffects)
    }

}