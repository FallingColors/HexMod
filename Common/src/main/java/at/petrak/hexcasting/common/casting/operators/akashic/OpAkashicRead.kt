package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getBlockPos
import at.petrak.hexcasting.api.spell.getPattern
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.iota.NullIota
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord

object OpAkashicRead : ConstManaAction {
    override val argc = 2
    override val manaCost = ManaConstants.DUST_UNIT

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
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
