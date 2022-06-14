package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.misc.ManaConstants

import at.petrak.hexcasting.api.player.Sentinel
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.SpellAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpDestroySentinel : SpellAction {
    override val argc = 0
    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val particles = mutableListOf<ParticleSpray>()
        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(ctx.caster)
        // TODO why can't you remove things from other dimensions?
        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())
        particles.add(ParticleSpray.cloud(sentinel.position, 2.0))

        return Triple(
            Spell,
            ManaConstants.DUST_UNIT / 10,
            particles
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            IXplatAbstractions.INSTANCE.setSentinel(ctx.caster, Sentinel.none())
        }
    }
}
