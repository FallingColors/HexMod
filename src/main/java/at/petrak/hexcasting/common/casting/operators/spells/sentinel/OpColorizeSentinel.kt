package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.lib.LibCapabilities
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgSentinelStatusUpdateAck
import net.minecraft.world.item.DyeColor
import net.minecraftforge.network.PacketDistributor

object OpColorizeSentinel : SpellOperator {
    override val argc = 0
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        return Pair(
            Spell,
            10_000
        )
    }

    private object Spell : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val maybeCap = ctx.caster.getCapability(LibCapabilities.SENTINEL).resolve()
            if (!maybeCap.isPresent)
                return

            val cap = maybeCap.get()

            val otherHandItem = ctx.caster.getItemInHand(ctx.otherHand)
            val color = DyeColor.getColor(otherHandItem)
            if (color != null) {
                otherHandItem.shrink(1)
                cap.color = color.textColor or 0xff_000000u.toInt()

                HexMessages.getNetwork()
                    .send(PacketDistributor.PLAYER.with { ctx.caster }, MsgSentinelStatusUpdateAck(cap))
            }
        }
    }
}