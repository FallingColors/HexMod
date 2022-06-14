package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.asActionResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpWritable : ConstManaAction {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val datum = args[0]

        val (handStack) = ctx.getHeldItemToOperateOn {
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            datumHolder != null && datumHolder.writeIota(datum, true)
        }

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack) ?: return false.asActionResult

        if (!datumHolder.writeIota(datum, true))
            return false.asActionResult

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            return false.asActionResult

        return true.asActionResult
    }
}
