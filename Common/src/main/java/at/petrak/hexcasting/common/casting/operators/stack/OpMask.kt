package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.LegacySpellDatum
import at.petrak.hexcasting.api.spell.casting.CastingContext
import it.unimi.dsi.fastutil.booleans.BooleanList

class OpMask(val mask: BooleanList) : ConstManaOperator {
    override val argc: Int
        get() = mask.size

    override fun execute(args: List<LegacySpellDatum<*>>, ctx: CastingContext): List<LegacySpellDatum<*>> {
        val out = ArrayList<LegacySpellDatum<*>>(this.mask.size)
        for ((i, include) in this.mask.withIndex()) {
            if (include)
                out.add(args[i])
        }
        return out
    }
}
