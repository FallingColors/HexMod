package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.misc.MediaConstants

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpDestroySentinel : SpellAction {
    override val argc = 0
    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): Triple<RenderedSpell, Int, List<ParticleSpray>>? {
        val particles = mutableListOf<ParticleSpray>()
        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(ctx.caster) ?: return null
        // TODO why can't you remove things from other dimensions?
        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())
        particles.add(ParticleSpray.cloud(sentinel.position, 2.0))

        return Triple(
            Spell,
            MediaConstants.DUST_UNIT / 10,
            particles
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            IXplatAbstractions.INSTANCE.setSentinel(ctx.caster, null)
        }
    }
}
