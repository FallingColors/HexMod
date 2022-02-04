package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.FrozenColorizer
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgColorizerUpdateAck
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor

object OpColorize : SpellOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Triple<RenderedSpell, Int, List<Vec3>> {
        return Triple(
            Spell,
            10_000,
            listOf()
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val maybeCap = ctx.caster.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve()
            if (!maybeCap.isPresent)
                return
            val cap = maybeCap.get()

            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            if (FrozenColorizer.isColorizer(otherHandItem.item)) {
                if (ctx.withdrawItem(otherHandItem.item, 1, true)) {
                    cap.colorizer = FrozenColorizer(otherHandItem.item, ctx.caster.uuid)
                    HexMessages.getNetwork()
                        .send(PacketDistributor.PLAYER.with { ctx.caster }, MsgColorizerUpdateAck(cap))
                }
            }
        }
    }
}