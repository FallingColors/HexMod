package at.petrak.hex.common.casting.operators.math

import at.petrak.hex.api.ConstManaOperator
import at.petrak.hex.api.Operator.Companion.getChecked
import at.petrak.hex.api.Operator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.world.phys.Vec3

object OpDeconstructVec : ConstManaOperator {
    override val argc = 1
    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val v = args.getChecked<Vec3>(0)
        return spellListOf(v.x, v.y, v.z)
    }
}