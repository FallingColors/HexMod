package at.petrak.hexcasting.common.casting.operators.spells.sentinel

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.Widget
import at.petrak.hexcasting.common.lib.LibCapabilities

object OpGetSentinelPos : ConstManaOperator {
    override val argc = 0
    override val manaCost = 1_000
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val maybeCap = ctx.caster.getCapability(LibCapabilities.SENTINEL).resolve()
        if (!maybeCap.isPresent)
            return spellListOf(Widget.NULL)

        val cap = maybeCap.get()
        return spellListOf(
            if (cap.hasSentinel)
                cap.position
            else
                Widget.NULL
        )
    }
}