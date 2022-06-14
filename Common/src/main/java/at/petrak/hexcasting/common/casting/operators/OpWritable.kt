package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapOthersName
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpWritable : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val datum = args[0]

        val (handStack) = ctx.getHeldItemToOperateOn {
            val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(it)

            datumHolder != null && datumHolder.writeIota(datum, true)
        }

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack) ?: return false.asSpellResult

        if (!datumHolder.writeIota(datum, true))
            return false.asSpellResult

        val trueName = MishapOthersName.getTrueNameFromDatum(datum, ctx.caster)
        if (trueName != null)
            return false.asSpellResult

        return true.asSpellResult
    }
}
