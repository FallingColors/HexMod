package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.GetNumOrVec
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.spellListOf

object OpAdd : ConstManaOperator {
    override val argc: Int
        get() = 2

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val lhs = GetNumOrVec(args[0], 1)
        val rhs = GetNumOrVec(args[1], 0)

        return spellListOf(
            lhs.map({ lnum ->
                rhs.map(
                    { rnum -> lnum + rnum }, { rvec -> rvec.add(lnum, lnum, lnum) }
                )
            }, { lvec ->
                rhs.map(
                    { rnum -> lvec.add(rnum, rnum, rnum) }, { rvec -> lvec.add(rvec) }
                )
            })
        )
    }
}
