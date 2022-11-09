package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpReadable : ConstManaAction {
    override val argc = 0

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val (handStack) = ctx.getHeldItemToOperateOn {
            IXplatAbstractions.INSTANCE.findDataHolder(it) != null
        }

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: return false.asActionResult

        datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: return false.asActionResult

        return true.asActionResult
    }
}
