package at.petrak.hexcasting.api

import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect
import net.minecraft.world.phys.Vec3

interface SpellOperator : Operator {
    val argc: Int

    fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>>

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
        if (this.argc > stack.size)
            throw CastException(CastException.Reason.NOT_ENOUGH_ARGS, this.argc, stack.size)
        val args = stack.takeLast(this.argc)
        for (_i in 0 until this.argc) stack.removeLast()
        val (spell, mana, particlePoses) = this.execute(args, ctx)

        val sideEffects = mutableListOf(
            OperatorSideEffect.ConsumeMana(mana),
            OperatorSideEffect.AttemptSpell(spell, this.isGreat)
        )
        for (pos in particlePoses) {
            sideEffects.add(OperatorSideEffect.Particles(pos))
        }

        return OperationResult(stack, sideEffects)
    }
}