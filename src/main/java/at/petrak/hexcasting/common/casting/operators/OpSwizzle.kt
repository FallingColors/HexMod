package at.petrak.hexcasting.common.casting.operators

import at.petrak.hexcasting.api.spell.ConstManaOperator
import at.petrak.hexcasting.api.spell.SpellDatum
import at.petrak.hexcasting.common.casting.CastingContext
import it.unimi.dsi.fastutil.booleans.BooleanList

class OpSwizzle(val mask: BooleanList) : ConstManaOperator {
    override val argc: Int
        get() = mask.size

    override fun execute(args: List<SpellDatum<*>>, ctx: CastingContext): List<SpellDatum<*>> {
        val out = ArrayList<SpellDatum<*>>(this.mask.size)
        for ((i, include) in this.mask.withIndex()) {
            if (include)
                out.add(args[i])
        }
        return out
    }
}