package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.numOrVec
import at.petrak.hexcasting.api.spell.spellListOf
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext

object OpMulDot : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = numOrVec(args[0], 1)
        val rhs = numOrVec(args[1], 0)

        return spellListOf(
            lhs.map({ lnum ->
                rhs.map(
                    { rnum -> lnum * rnum }, { rvec -> rvec.scale(lnum) }
                )
            }, { lvec ->
                rhs.map(
                    { rnum -> lvec.multiply(rnum, rnum, rnum) }, { rvec -> lvec.dot(rvec) }
                )
            })
        )
    }
}
