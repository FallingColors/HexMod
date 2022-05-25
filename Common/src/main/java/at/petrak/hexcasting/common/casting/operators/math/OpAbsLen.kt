package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.numOrVec
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.asSpellResult
import at.petrak.hexcasting.api.spell.casting.CastingContext
import kotlin.math.absoluteValue

object OpAbsLen : ConstManaOperator {
    override val argc: Int
        get() = 1

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val x = numOrVec(args[0], 0)

        return x.map({ num -> num.absoluteValue }, { vec -> vec.length() }).asSpellResult
    }
}
