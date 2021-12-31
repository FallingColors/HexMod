package at.petrak.hex.common.casting.operators

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.world.entity.Entity

object OpEntityLook : ConstManaOperator {
    override val argc = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val e: Entity = args.getChecked(0)
        return spellListOf(e.lookAngle)
    }
}