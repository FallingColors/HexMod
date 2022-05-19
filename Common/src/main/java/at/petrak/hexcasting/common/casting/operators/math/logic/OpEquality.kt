package at.petrak.hexcasting.common.casting.operators.math.logic

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.SpellList
import at.petrak.hexcasting.api.spell.casting.CastingContext
import net.minecraft.world.phys.Vec3
import kotlin.math.abs

class OpEquality(val invert: Boolean) : ConstManaOperator {
    override val argc = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = args[0]
        val rhs = args[1]

        return spellListOf(if (checkEquals(lhs.payload, rhs.payload) != invert) 1.0 else 0.0)
    }

    private fun checkEquals(a: Any, b: Any, recursionsLeft: Int = 64): Boolean {
        return when {
            a is Double && b is Double -> abs(a - b) < 0.0001
            a is Vec3 && b is Vec3 -> a.subtract(b).lengthSqr() < 0.0000001
            a is SpellList && b is SpellList -> {
                val castA = a.toList()
                val castB = b.toList()
                if (castA.size != castB.size || recursionsLeft == 0)
                    return false
                for (i in castA.indices)
                    if (!checkEquals(castA[i].payload, castB[i].payload, recursionsLeft - 1))
                        return false
                true
            }
            else -> a == b
        }
    }
}
