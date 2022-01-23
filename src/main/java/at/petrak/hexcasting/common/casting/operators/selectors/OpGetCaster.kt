package at.petrak.hexcasting.common.casting.operators.selectors

import at.petrak.hexcasting.api.ConstManaOperator
import at.petrak.hexcasting.api.Operator
import at.petrak.hexcasting.api.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import net.minecraft.world.entity.Entity

object OpGetCaster : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
        Operator.spellListOf(ctx.caster as Entity)
}