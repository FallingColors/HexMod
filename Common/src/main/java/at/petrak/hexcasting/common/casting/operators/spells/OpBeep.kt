package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.ParticleSpray
import at.petrak.hexcasting.api.spell.RenderedSpell
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellOperator
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.common.network.MsgBeepAck
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.phys.Vec3

object OpBeep : SpellOperator {
    override val argc = 3

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getChecked<Vec3>(0, argc)
        val instrument = args.getChecked<Double>(1, argc).toInt().coerceIn(0, NoteBlockInstrument.values().size - 1)
        val note = args.getChecked<Double>(2, argc).toInt().coerceIn(0, 24)
        ctx.assertVecInRange(target)

        return Triple(
            Spell(target, note, NoteBlockInstrument.values()[instrument]),
            ManaConstants.DUST_UNIT / 10,
            listOf(ParticleSpray.cloud(target, 1.0))
        )
    }

    override fun hasCastingSound(ctx: CastingContext) = false

    private data class Spell(val target: Vec3, val note: Int, val instrument: NoteBlockInstrument) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            IXplatAbstractions.INSTANCE.sendPacketNear(target, 128.0, ctx.world, MsgBeepAck(target, note, instrument))
        }
    }
}
