package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.cap.HexCapabilities
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import net.minecraft.world.entity.item.ItemEntity

object OpTheCoolerRead : ConstManaOperator {
    override val argc = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): List<SpellDatum<*>> {
        val target = args.getChecked<ItemEntity>(0)
        val stack = target.item
        val datumHolder = stack.getCapability(HexCapabilities.DATUM).resolve()
        if (!datumHolder.isPresent)
            throw MishapBadItem.of(stack, "iota.read")

        ctx.assertEntityInRange(target)


        val datum = datumHolder.get().readDatum(ctx.world)
            ?: datumHolder.get().emptyDatum()
            ?: throw MishapBadItem.of(stack, "iota.read")
        return listOf(datum)
    }
}
