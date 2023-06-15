package at.petrak.hexcasting.common.casting.actions.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota

class OpTwiddling(val argumentCount: Int, val lookup: IntArray) : ConstMediaAction {
    override val argc: Int
        get() = this.argumentCount

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> =
        this.lookup.map(args::get)
}