package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.common.network.MsgBeepAck
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.phys.Vec3

object OpBeep : SpellAction {
    override val argc = 3

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val target = args.getVec3(0, argc)
        val instrument = args.getPositiveIntUnder(1, NoteBlockInstrument.values().size, argc)
        val note = args.getPositiveIntUnder(2, 24, argc) // mojang don't have magic numbers challenge
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
