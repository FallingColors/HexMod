package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector
import it.unimi.dsi.fastutil.booleans.BooleanList
import net.minecraft.resources.ResourceLocation

class OpMask(val mask: BooleanList, val key: ResourceLocation) : ConstMediaAction {
    override val argc: Int
        get() = mask.size

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val out = Vector.VectorBuilder<Iota>()
        for ((i, include) in this.mask.withIndex()) {
            if (include)
                out.addOne(args[i])
        }
        return out.result()
    }
}
