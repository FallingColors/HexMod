package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.RenderedSpell
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.api.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.lib.HexCapabilities
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgSentinelStatusUpdateAck
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor

class OpCreateSentinel(val extendsRange: Boolean) : SpellOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): Pair<RenderedSpell, Int> {
        val target = args.getChecked<Vec3>(0)
        ctx.assertVecInRange(target)

        return Pair(
            Spell(target, this.extendsRange),
            10_000
        )
    }

    private data class Spell(val target: Vec3, val extendsRange: Boolean) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val maybeCap = ctx.caster.getCapability(HexCapabilities.SENTINEL).resolve()
            if (!maybeCap.isPresent)
                return

            val cap = maybeCap.get()
            cap.hasSentinel = true
            cap.extendsRange = extendsRange
            cap.position = target

            HexMessages.getNetwork().send(PacketDistributor.PLAYER.with { ctx.caster }, MsgSentinelStatusUpdateAck(cap))
        }
    }
}