package at.petrak.hexcasting.common.casting.actions.spells.sentinel

import at.petrak.hexcasting.api.casting.ParticleSpray
import at.petrak.hexcasting.api.casting.RenderedSpell
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadCaster
import at.petrak.hexcasting.api.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.server.level.ServerPlayer

object OpDestroySentinel : SpellAction {
    override val argc = 0
    override fun execute(
            args: List<Iota>,
            env: CastingEnvironment
    ): SpellAction.Result {
        if (env.castingEntity !is ServerPlayer)
            throw MishapBadCaster()

        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(env.castingEntity as? ServerPlayer)

        // TODO why can't you remove things from other dimensions?
        val dim = sentinel?.dimension
        if (dim != null && dim != env.world.dimension())
            throw MishapLocationInWrongDimension(dim.location())

        val particles = sentinel?.position?.let { listOf(ParticleSpray.cloud(it, 2.0)) }
            ?: listOf()
        return SpellAction.Result(
            Spell,
            MediaConstants.DUST_UNIT / 10,
            particles
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(env: CastingEnvironment) {
            IXplatAbstractions.INSTANCE.setSentinel(env.castingEntity as? ServerPlayer, null)
        }
    }
}
