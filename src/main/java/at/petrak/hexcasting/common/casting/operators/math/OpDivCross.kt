package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.Operator.Companion.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import at.petrak.hexcasting.common.casting.mishaps.MishapDivideByZero
import net.minecraft.world.phys.Vec3

object OpDivCross : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = MathOpUtils.GetNumOrVec(args[0], 1)
        val rhs = MathOpUtils.GetNumOrVec(args[1], 0)

        return spellListOf(
            lhs.map({ lnum ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0)
                            throw MishapDivideByZero(lnum)
                        lnum / rnum
                    },
                    { rvec ->
                        if (rvec.x == 0.0 && rvec.y == 0.0 && rvec.z == 0.0)
                            throw MishapDivideByZero(lnum)
                        Vec3(lnum / rvec.x, lnum / rvec.y, lnum / rvec.z)
                    }
                )
            }, { lvec ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0)
                            throw MishapDivideByZero(lvec)
                        lvec.scale(1.0 / rnum)
                    },
                    { rvec ->
                        if (rvec.x == 0.0 && rvec.y == 0.0 && rvec.z == 0.0)
                            throw MishapDivideByZero(lvec)
                        lvec.cross(rvec)
                    }
                )
            })
        )
    }
}
