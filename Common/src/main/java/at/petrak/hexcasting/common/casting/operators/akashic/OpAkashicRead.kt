package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord

object OpAkashicRead : ConstMediaAction {
    override val argc = 2
    override val mediaCost = MediaConstants.DUST_UNIT

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val pos = args.getBlockPos(0, argc)
        val key = args.getPattern(1, argc)

        val record = ctx.world.getBlockState(pos).block
        if (record !is BlockAkashicRecord) {
            throw MishapNoAkashicRecord(pos)
        }

        val datum = record.lookupPattern(pos, key, ctx.world)
        return listOf(datum ?: NullIota())
    }
}
