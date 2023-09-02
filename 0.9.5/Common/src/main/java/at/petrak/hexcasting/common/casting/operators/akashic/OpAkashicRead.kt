package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.math.HexPattern
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

object OpAkashicRead : ConstManaOperator {
    override val argc = 2
    override val manaCost = ManaConstants.DUST_UNIT

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val pos = args.getChecked<Vec3>(0, argc)
        val key = args.getChecked<HexPattern>(1, argc)

        val bpos = BlockPos(pos)
        val tile = ctx.world.getBlockEntity(bpos)
        if (tile !is BlockEntityAkashicRecord) {
            throw MishapNoAkashicRecord(bpos)
        }

        val datum = tile.lookupPattern(key, ctx.world)
        return listOf(datum ?: SpellDatum.make(Widget.NULL))
    }
}
