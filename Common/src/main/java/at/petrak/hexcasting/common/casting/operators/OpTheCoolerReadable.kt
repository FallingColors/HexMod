package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getChecked
import at.petrak.hexcasting.xplat.IXplatAbstractions
import net.minecraft.world.entity.item.ItemEntity

object OpTheCoolerReadable : ConstManaOperator {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): List<Iota> {
        val target = args.getChecked<ItemEntity>(0, argc)
        ctx.assertEntityInRange(target)

        val stack = target.item
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(stack)
            ?: return false.asSpellResult

        if (datumHolder.readIota(ctx.world) == null && datumHolder.emptyIota() == null)
            return false.asSpellResult

        return true.asSpellResult
    }
}
