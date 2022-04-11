package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.network.HexMessages
import at.petrak.hexcasting.common.network.MsgBeepAck
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.phys.Vec3
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.network.PacketDistributor.TargetPoint

object OpBeep : SpellOperator {
    override val argc = 3

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0)
        val note = args.getChecked<Double>(1).toInt().coerceIn(0, 24)
        val instrument = args.getChecked<Double>(2).toInt().coerceIn(0, NoteBlockInstrument.values().size - 1)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target, note, NoteBlockInstrument.values()[instrument]),
            1_000,
            listOf(ParticleSpray.Cloud(Vec3.atCenterOf(BlockPos(target)), 1.0))
        )
    }

    private data class Spell(val target: Vec3, val note: Int, val instrument: NoteBlockInstrument) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            HexMessages.getNetwork().send(PacketDistributor.NEAR.with {
                TargetPoint(
                    target.x, target.y, target.z,
                    128.0 * 128.0, ctx.world.dimension()
                )
            }, MsgBeepAck(target, note, instrument))
        }
    }
}
