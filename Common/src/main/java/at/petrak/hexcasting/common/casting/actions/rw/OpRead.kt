package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.casting.mishaps.MishapBadOffhandItem
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpRead : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val (handStack, hand) = env.getHeldItemToOperateOn {
            val dataHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)
            dataHolder != null && (dataHolder.readIota(env.world) != null || dataHolder.emptyIota() != null)
        } ?: return listOf(NullIota())

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        val datum = datumHolder.readIota(env.world)
            ?: datumHolder.emptyIota()
            ?: throw MishapBadOffhandItem.of(handStack, hand, "iota.read")

        return listOf(datum)
    }
}
