package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.world.entity.Entity

object OpGetCaster : SimpleOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
        SpellOperator.spellListOf(ctx.caster as Entity)
}