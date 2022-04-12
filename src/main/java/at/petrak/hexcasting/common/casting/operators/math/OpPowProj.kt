package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import net.minecraft.world.phys.Vec3
import kotlin.math.pow

object OpPowProj : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = MathOpUtils.GetNumOrVec(args[0], 1)
        val rhs = MathOpUtils.GetNumOrVec(args[1], 0)

        return spellListOf(
            lhs.map({ lnum ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0 && lnum == 0.0)
                            throw MishapDivideByZero.of(lnum, rnum, "exponent")
                        lnum.pow(rnum)
                    }, { rvec ->
                        if (lnum == 0.0 && (rvec.x == 0.0 || rvec.y == 0.0 || rvec.z == 0.0))
                            throw MishapDivideByZero.of(lnum, rvec, "exponent")
                        Vec3(lnum.pow(rvec.x), lnum.pow(rvec.y), lnum.pow(rvec.z))
                    }
                )
            }, { lvec ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0 && (lvec.x == 0.0 || lvec.y == 0.0 || lvec.z == 0.0))
                            throw MishapDivideByZero.of(lvec, rnum, "exponent")
                        Vec3(lvec.x.pow(rnum), lvec.y.pow(rnum), lvec.z.pow(rnum))
                    },
                    { rvec ->
                        if (rvec == Vec3.ZERO)
                            throw MishapDivideByZero.of(lvec, rvec, "project")
                        rvec.scale(lvec.dot(rvec) / rvec.dot(rvec))
                    }
                )
            })
        )
    }
}
