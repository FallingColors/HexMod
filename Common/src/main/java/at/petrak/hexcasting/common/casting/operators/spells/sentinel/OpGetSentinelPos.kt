package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.misc.ManaConstants
import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.Widget
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapLocationInWrongDimension
import at.petrak.hexcasting.xplat.IXplatAbstractions

object OpGetSentinelPos : ConstManaOperator {
    override val argc = 0
    override val manaCost = ManaConstants.DUST_UNIT / 10
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val sentinel = IXplatAbstractions.INSTANCE.getSentinel(ctx.caster)
        if (sentinel.dimension != ctx.world.dimension())
            throw MishapLocationInWrongDimension(sentinel.dimension.location())
        return spellListOf(
            if (sentinel.hasSentinel)
                sentinel.position
            else
                Widget.NULL
        )
    }
}
