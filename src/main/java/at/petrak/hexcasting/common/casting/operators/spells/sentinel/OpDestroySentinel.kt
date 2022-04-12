package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.api.player.HexPlayerDataHelper
import at.petrak.hexcasting.api.player.Sentinel

object OpDestroySentinel : SpellOperator {
    override val argc = 0
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val particles = mutableListOf<ParticleSpray>()
        val sentinel = HexPlayerDataHelper.getSentinel(ctx.caster)
        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())
        particles.add(ParticleSpray.Cloud(sentinel.position, 2.0))

        return Triple(
            Spell,
            1_000,
            particles
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            HexPlayerDataHelper.setSentinel(ctx.caster, Sentinel.none())
        }
    }
}
