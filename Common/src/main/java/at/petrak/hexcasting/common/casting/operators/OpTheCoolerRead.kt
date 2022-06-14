package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.getItemEntity
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapBadItem
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpTheCoolerRead : ConstManaAction {
    override val argc = 1

    override fun execute(
        args: List<Iota>,
        ctx: CastingContext
    ): List<Iota> {
        val target = args.getItemEntity(0, argc)

        ctx.assertEntityInRange(target)

        val stack = target.item
        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(stack)
            ?: throw MishapBadItem.of(target, "iota.read")

        val datum = datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBadItem.of(target, "iota.read")
        return listOf(datum)
    }
}
