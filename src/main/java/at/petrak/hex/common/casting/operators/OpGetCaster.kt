package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.world.entity.Entity

object OpGetCaster : ConstManaOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
        Operator.spellListOf(ctx.caster as Entity)
}