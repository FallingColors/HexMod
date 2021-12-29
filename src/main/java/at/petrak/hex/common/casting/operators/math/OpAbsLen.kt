package at.petrak.hex.common.casting.operators.math

import at.petrak.hex.api.SimpleOperator
import at.petrak.hex.api.SpellOperator.Companion.spellListOf
import at.petrak.hex.common.casting.CastingContext
import at.petrak.hex.common.casting.SpellDatum
import kotlin.math.absoluteValue

object OpAbsLen : SimpleOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val x = MathOpUtils.GetNumOrVec(args[0])

        return spellListOf(
            x.map({ num -> num.absoluteValue }, { vec -> vec.length() })
        )
    }
}