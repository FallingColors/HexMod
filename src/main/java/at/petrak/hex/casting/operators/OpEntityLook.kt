package at.petrak.hex.casting.operators

import at.petrak.hex.casting.CastingContext
import at.petrak.hex.casting.SpellDatum
import at.petrak.hex.casting.operators.SpellOperator.Companion.getChecked
import at.petrak.hex.casting.operators.SpellOperator.Companion.spellListOf
import net.minecraft.world.entity.Entity

object OpEntityLook : SpellOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val e: Entity = args.getChecked(0)
        return spellListOf(e.lookAngle)
    }
}