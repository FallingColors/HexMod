package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpReadable : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val (handStack) = ctx.getHeldItemToOperateOn {
            IXplatAbstractions.INSTANCE.findDataHolder(it) != null
        }

        val datumHolder = IXplatAbstractions.INSTANCE.findDataHolder(handStack)
            ?: return false.asSpellResult

        datumHolder.readDatum(ctx.world)
            ?: datumHolder.emptyDatum()
            ?: return false.asSpellResult

        return true.asSpellResult
    }
}
