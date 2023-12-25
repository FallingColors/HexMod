package at.petrak.hexcasting.common.casting.actions.spells.great

import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants

class OpWeather(val rain: Boolean) : SpellAction {
    override val argc = 0

    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        return SpellAction.Result(
            Spell(rain),
            if (this.rain) MediaConstants.CRYSTAL_UNIT else MediaConstants.SHARD_UNIT,
            listOf()
        )
    }

    private data class Spell(val rain: Boolean) : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            val w = env.world
            if (w.isRaining != rain) {
                w.levelData.isRaining = rain // i hex the rains down in minecraftia

                val (minTime, maxTime) = if (rain) (30 to 90) else (60 to 180)
                val time = (w.random.nextInt(minTime, maxTime)) * 20 * 60
                if (rain) {
                    w.setWeatherParameters(0, time, true, w.random.nextDouble() < 0.05)
                } else {
                    w.setWeatherParameters(time, 0, false, false)
                }
            }
        }
    }
}
