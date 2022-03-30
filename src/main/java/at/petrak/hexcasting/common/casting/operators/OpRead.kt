package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.common.items.ItemDataHolder

object OpRead : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val handStack =
            ctx.caster.getItemInHand(ctx.otherHand)
        val handItem = handStack.item
        if (handItem !is ItemDataHolder) {
            throw MishapBadOffhandItem.of(handStack, "iota.read")
        }
        val datum = handItem.readDatum(handStack, ctx) ?: throw MishapBadOffhandItem.of(handStack, "iota.read")
        return listOf(datum)
    }
}