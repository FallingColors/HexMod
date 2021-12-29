package at.petrak.hex.common.casting.operators.math

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

object OpPowProj : SimpleOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = MathOpUtils.GetNumOrVec(args[0])
        val rhs = MathOpUtils.GetNumOrVec(args[1])

        return spellListOf(
            lhs.map({ lnum ->
                rhs.map(
                    { rnum -> lnum.pow(rnum) }, { rvec -> Vec3(lnum.pow(rvec.x), lnum.pow(rvec.y), lnum.pow(rvec.z)) }
                )
            }, { lvec ->
                rhs.map(
                    { rnum -> Vec3(lvec.x.pow(rnum), lvec.y.pow(rnum), lvec.z.pow(rnum)) },
                    { rvec -> rvec.scale(lvec.dot(rvec) / rvec.dot(rvec)) }
                )
            })
        )
    }
}