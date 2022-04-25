package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.api.spell.math.HexPattern
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

object OpAkashicRead : ConstManaOperator {
    override val argc = 2
    override val manaCost = 10_000

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val pos = args.getChecked<Vec3>(0)
        val key = args.getChecked<HexPattern>(1)

        val bpos = BlockPos(pos)
        val tile = ctx.world.getBlockEntity(bpos)
        if (tile !is BlockEntityAkashicRecord) {
            throw MishapNoAkashicRecord(bpos)
        }

        val datum = tile.lookupPattern(key, ctx.world)
        return listOf(datum ?: SpellDatum.make(Widget.NULL))
    }
}
