package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.getChecked
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity

object OpTheCoolerReadable : ConstManaOperator {
    override val argc = 1

    override fun execute(
        args: List<SpellDatum<*>>,
        ctx: CastingContext
    ): List<SpellDatum<*>> {
        val target = args.getChecked<ItemEntity>(0)
        ctx.assertEntityInRange(target)

        val stack = target.item
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(stack)
            ?: return spellListOf(0.0)

        if (datumHolder.readDatum(ctx.world) == null && datumHolder.emptyDatum() == null)
            return spellListOf(0.0)

        return spellListOf(1.0)
    }
}
