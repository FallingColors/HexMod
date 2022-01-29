package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.colors.CapPreferredColorizer
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgColorizerUpdateAck
import net.minecraft.world.item.ItemStack
import net.minecraftforge.network.PacketDistributor

object OpColorize : SpellOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        return Pair(
            Spell,
            10_000
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val maybeCap = ctx.caster.getCapability(HexCapabilities.PREFERRED_COLORIZER).resolve()
            if (!maybeCap.isPresent)
                return
            val cap = maybeCap.get()

            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            if (CapPreferredColorizer.isColorizer(otherHandItem.item)) {
                val copied = ItemStack(otherHandItem.item, 1)
                ctx.withdrawItem(otherHandItem.item, 1, true)
                cap.colorizer = copied

                HexMessages.getNetwork().send(PacketDistributor.PLAYER.with { ctx.caster }, MsgColorizerUpdateAck(cap))
            }
        }
    }
}