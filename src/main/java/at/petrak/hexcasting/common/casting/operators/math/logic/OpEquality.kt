package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

class OpEquality(val invert: Boolean) : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = args[0]
        val rhs = args[1]

        return spellListOf(if (when {
            lhs.payload is Double && rhs.payload is Double ->
                abs(lhs.payload - rhs.payload) < 0.0001
            lhs.payload is Vec3 && rhs.payload is Vec3 ->
                abs(lhs.payload.x - rhs.payload.x) < 0.0001 && abs(lhs.payload.y - rhs.payload.y) < 0.0001 && abs(lhs.payload.z - rhs.payload.z) < 0.0001
            else -> lhs == rhs
        } != invert) 1.0 else 0.0)
    }
}
