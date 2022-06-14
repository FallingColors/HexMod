package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.spell.ConstManaAction
import at.petrak.hexcasting.api.spell.iota.Iota
import at.petrak.hexcasting.api.spell.casting.CastingContext
import it.unimi.dsi.fastutil.booleans.BooleanList

class OpMask(val mask: BooleanList) : ConstManaAction {
    override val argc: Int
        get() = mask.size

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val out = ArrayList<Iota>(this.mask.size)
        for ((i, include) in this.mask.withIndex()) {
            if (include)
                out.add(args[i])
        }
        return out
    }
}
