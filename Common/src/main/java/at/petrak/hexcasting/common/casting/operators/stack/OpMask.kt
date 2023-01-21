package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota
import it.unimi.dsi.fastutil.booleans.BooleanList
import net.minecraft.resources.ResourceLocation

class OpMask(val mask: BooleanList, val key: ResourceLocation) : ConstMediaAction {
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
