package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgSentinelStatusUpdateAck
import net.minecraftforge.network.PacketDistributor

object OpDestroySentinel : SpellOperator {
    override val argc = 0
    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val particles = mutableListOf<ParticleSpray>()
        val maybeCap = ctx.caster.getCapability(HexCapabilities.SENTINEL).resolve()
        maybeCap.ifPresent {
            if (it.dimension != ctx.world.dimension())
                throw MishapLocationInWrongDimension(it.dimension.location())
            particles.add(ParticleSpray.Cloud(it.position, 2.0))
        }

        return Triple(
            Spell,
            1_000,
            particles
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val maybeCap = ctx.caster.getCapability(HexCapabilities.SENTINEL).resolve()
            if (!maybeCap.isPresent)
                return

            val cap = maybeCap.get()
            cap.hasSentinel = false

            HexMessages.getNetwork().send(PacketDistributor.PLAYER.with { ctx.caster }, MsgSentinelStatusUpdateAck(cap))
        }
    }
}
