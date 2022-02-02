package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.OperationResult
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.OperatorSideEffect
import net.minecraft.world.phys.Vec3

class OpWeather(val rain: Boolean) : SpellOperator {
    override val argc = 0
    override val isGreat = true

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>> {
        return Triple(Spell(rain), ((if (this.rain) 2 else 1) * 50_000), listOf()) // return an empty list for shits and gigs ig
    }

    private data class Spell(val rain: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val w = ctx.world
            if (w.isRaining != rain) {
                w.levelData.isRaining = rain // i hex the rains down in minecraftia
            }
        }
    }

    override fun operate(stack: MutableList<SpellDatum<*>>, ctx: CastingContext): OperationResult {
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