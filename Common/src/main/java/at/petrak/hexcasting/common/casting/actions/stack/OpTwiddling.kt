package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.utils.Vector

class OpTwiddling(val argumentCount: Int, val lookup: IntArray) : ConstMediaAction {
    override val argc: Int
        get() = this.argumentCount

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> =
        Vector.from(this.lookup.map(args::get))
}