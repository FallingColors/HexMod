package at.petrak.hexcasting.common.casting.operators.selectors

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpGetCaster : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        ctx.assertEntityInRange(ctx.caster)
        return ctx.caster.asSpellResult
    }
}
