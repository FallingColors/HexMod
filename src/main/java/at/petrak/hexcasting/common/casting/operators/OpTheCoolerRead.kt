package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.getChecked
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastException
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.items.ItemDataHolder
import net.minecraft.world.entity.item.ItemEntity

object OpTheCoolerRead : ConstManaOperator {
    override val argc = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): List<SpellDatum<*>> {
        val target = args.getChecked<ItemEntity>(0)
        if (target.item.item !is ItemDataHolder) {
            throw CastException(CastException.Reason.BAD_OFFHAND_ITEM, ItemDataHolder::class.java, target.item)
        }

        val stack = target.item
        val item = stack.item as ItemDataHolder
        val datum = item.readDatum(stack, ctx) ?: SpellDatum.make(Widget.NULL)
        return listOf(datum)
    }
}