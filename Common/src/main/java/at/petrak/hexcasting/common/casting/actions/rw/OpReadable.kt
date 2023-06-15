package at.petrak.hexcasting.common.casting.actions.rw

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpReadable : ConstMediaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val (handStack) = env.getHeldItemToOperateOn {
            IXplatAbstractions.INSTANCE.findDataHolder(it) != null
        } ?: return false.asActionResult

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: return false.asActionResult

        datumHolder.readIota(env.world)
            ?: datumHolder.emptyIota()
            ?: return false.asActionResult

        return true.asActionResult
    }
}
