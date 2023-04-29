package at.petrak.hexcasting.common.casting.operators.spells

import at.petrak.hexcasting.api.casting.*
import at.petrak.hexcasting.api.casting.castables.SpellAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.common.msgs.MsgBeepS2C
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.phys.Vec3

object OpBeep : SpellAction {
    override val argc = 3

    override fun execute(
        args: List<Iota>,
        ctx: CastingEnvironment
    ): SpellAction.Result {
        val target = args.getVec3(0, argc)
        val instrument = args.getPositiveIntUnder(1, NoteBlockInstrument.values().size, argc)
        val note = args.getPositiveIntUnderInclusive(2, 24, argc) // mojang don't have magic numbers challenge
        ctx.assertVecInRange(target)

        return SpellAction.Result(
            Spell(target, note, NoteBlockInstrument.values()[instrument]),
            MediaConstants.DUST_UNIT / 10,
            listOf(ParticleSpray.cloud(target, 1.0))
        )
    }

    override fun hasCastingSound(ctx: CastingEnvironment) = false

    private data class Spell(val target: Vec3, val note: Int, val instrument: NoteBlockInstrument) : RenderedSpell {
        override fun cast(ctx: CastingEnvironment) {
            IXplatAbstractions.INSTANCE.sendPacketNear(target, 128.0, ctx.world, MsgBeepS2C(target, note, instrument))
            ctx.world.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, target)
        }
    }
}
