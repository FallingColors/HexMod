package at.petrak.hexcasting.common.casting.operators.rw

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpRead : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingEnvironment): List<Iota> {
        val (handStack, hand) = ctx.getHeldItemToOperateOn {
            val dataHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)
            dataHolder != null && (dataHolder.readIota(ctx.world) != null || dataHolder.emptyIota() != null)
        } ?: return listOf(NullIota())

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        val datum = datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        return listOf(datum)
    }
}
