package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpReadable : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val (handStack) = ctx.getHeldItemToOperateOn {
            IXplatAbstractions.INSTANCE.findDataHolder(it) != null
        }

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: return false.asSpellResult

        datumHolder.readIota(ctx.world)
            ?: datumHolder.emptyIota()
            ?: return false.asSpellResult

        return true.asSpellResult
    }
}
