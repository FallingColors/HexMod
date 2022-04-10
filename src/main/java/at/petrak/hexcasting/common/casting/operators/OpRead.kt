package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.item.DataHolder
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapBadOffhandItem

object OpRead : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val handStack = ctx.getHeldItemToOperateOn {
            val item = it.item
            item is DataHolder && item.readDatum(it, ctx.world) != null
        }

        val handItem = handStack.item
        val datum = (handItem as? DataHolder)?.readDatum(handStack, ctx.world) ?: throw MishapBadOffhandItem.of(handStack, "iota.read")

        return listOf(datum)
    }
}
