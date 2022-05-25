package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.numOrVec
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.mishaps.MishapDivideByZero
import at.petrak.hexcasting.api.spell.spellListOf
import net.minecraft.world.phys.Vec3

object OpDivCross : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = numOrVec(args[0], 1)
        val rhs = numOrVec(args[1], 0)

        return spellListOf(
            lhs.map({ lnum ->
                rhs.map(
                    { rnum ->
                        if (rnum == 0.0)
                            throw MishapDivideByZero.of(lnum, rnum)
                        lnum / rnum
                    },
                    { rvec ->
                        if (rvec.x == 0.0 || rvec.y == 0.0 || rvec.z == 0.0)
                            throw MishapDivideByZero.of(lnum, rvec)
                        Vec3(lnum / rvec.x, lnum / rvec.y, lnum / rvec.z)
                    }
                )
            }, { lvec ->
                rhs.map(
                    { rnum ->
                        if (lvec == Vec3.ZERO)
                            throw MishapDivideByZero.of(lvec, rnum)
                        lvec.scale(1.0 / rnum)
                    },
                    { rvec -> lvec.cross(rvec) }
                )
            })
        )
    }
}
