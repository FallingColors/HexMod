package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext

class OpWeather(val rain: Boolean) : SpellOperator {
    override val argc = 0
    override val isGreat = true

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        return Triple(
            Spell(rain),
            ((if (this.rain) 2 else 1) * 50_000),
            listOf()
        )
    }

    private data class Spell(val rain: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val w = ctx.world
            if (w.isRaining != rain) {
                w.levelData.isRaining = rain // i hex the rains down in minecraftia
            }
        }
    }
}
