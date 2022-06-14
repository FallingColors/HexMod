package at.petrak.hexcasting.common.casting.operators.spells.great

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext

class OpWeather(val rain: Boolean) : SpellAction {
    override val argc = 0
    override val isGreat = true

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        if (ctx.world.isRaining == rain)
            return null

        return Triple(
            Spell(rain),
            if (this.rain) ManaConstants.CRYSTAL_UNIT else ManaConstants.SHARD_UNIT,
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
