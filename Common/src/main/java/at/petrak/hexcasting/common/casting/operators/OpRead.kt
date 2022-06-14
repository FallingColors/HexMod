package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpRead : ConstManaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val dataHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)
            dataHolder != null && (dataHolder.readIota(ctx.world) != null || dataHolder.emptyIota() != null)
        }

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        val datum = datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        return listOf(datum)
    }
}
