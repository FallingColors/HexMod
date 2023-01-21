package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants

class OpWeather(val rain: Boolean) : SpellAction {
    override val argc = 0

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        if (ctx.world.isRaining == rain)
            return null

        return Triple(
            Spell(rain),
            if (this.rain) MediaConstants.CRYSTAL_UNIT else MediaConstants.SHARD_UNIT,
            listOf()
        )
    }

    private data class Spell(val rain: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val w = ctx.world
            if (w.isRaining != rain) {
                w.levelData.isRaining = rain // i hex the rains down in minecraftia

                if (rain) {
                    w.setWeatherParameters(0, 6000, true, w.random.nextDouble() < 0.05)
                } else {
                    w.setWeatherParameters(6000, 0, false, false)
                }
            }
        }
    }
}
