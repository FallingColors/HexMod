package at.petrak.hex.casting.operators

import at.petrak.hex.casting.CastingContext
import at.petrak.hex.casting.SpellDatum
import net.minecraft.world.entity.Entity

object OpGetCaster : SpellOperator {
    override val argc = 0

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> =
        SpellOperator.spellListOf(ctx.caster as Entity)
}