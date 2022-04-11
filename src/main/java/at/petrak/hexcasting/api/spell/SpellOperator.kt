package at.petrak.hexcasting.api.spell

import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect
import at.petrak.hexcasting.common.casting.mishaps.MishapNotEnoughArgs

interface SpellOperator : Operator {
    val argc: Int

    val hasCastingSound: Boolean get() = true

    fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw MishapNotEnoughArgs(this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_i in 0 until this.argc) stack.removeLast()
        val (spell, mana, particles) = this.execute(args, ctx)

        val sideEffects = mutableListOf(
            OperatorSideEffect.ConsumeMana(mana),
            OperatorSideEffect.AttemptSpell(spell, this.isGreat, this.hasCastingSound)
        )
        for (spray in particles) {
            sideEffects.add(OperatorSideEffect.Particles(spray))
        }

        return OperationResult(stack, sideEffects)
    }

}
