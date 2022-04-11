package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.item.DataHolder
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapBadItem
import at.petrak.hexcasting.common.casting.mishaps.MishapBadOffhandItem
import net.minecraft.world.entity.item.ItemEntity

object OpTheCoolerRead : ConstManaOperator {
    override val argc = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): List<SpellDatum<*>> {
        val target = args.getChecked<ItemEntity>(0)
        val stack = target.item
        val item = stack.item
        if (item !is DataHolder) {
            throw MishapBadItem.of(stack, "iota.read")
        }
        ctx.assertEntityInRange(target)


        val datum = item.readDatum(stack, ctx.world) ?: throw MishapBadItem.of(stack, "iota.read")
        return listOf(datum)
    }
}
