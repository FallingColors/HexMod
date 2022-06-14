package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getItemEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerReadable : ConstManaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): List<Iota> {
        val target = args.getItemEntity(0, argc)
        ctx.assertEntityInRange(target)

        val stack = target.item
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(stack)
            ?: return false.asActionResult

        if (datumHolder.readIota(ctx.world) == null && datumHolder.emptyIota() == null)
            return false.asActionResult

        return true.asActionResult
    }
}
