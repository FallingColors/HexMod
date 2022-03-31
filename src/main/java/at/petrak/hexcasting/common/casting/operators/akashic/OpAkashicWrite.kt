package at.petrak.hexcasting.common.casting.operators.akashic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicRecord
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.casting.mishaps.MishapNoAkashicRecord
import at.petrak.hexcasting.hexmath.HexPattern
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

object OpAkashicWrite : ConstManaOperator {
    override val argc = 3
    override val manaCost = 10_000

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val pos = args.getChecked<Vec3>(0)
        val key = args.getChecked<HexPattern>(1)
        val datum = args[2]

        val bpos = BlockPos(pos)
        val tile = ctx.world.getBlockEntity(bpos)
        if (tile !is BlockEntityAkashicRecord) {
            throw MishapNoAkashicRecord(bpos)
        }

        val newPos = tile.addNewDatum(key, datum)
        return spellListOf(
            if (newPos == null)
                Widget.NULL
            else
                Vec3.atCenterOf(newPos)
        )
    }
}