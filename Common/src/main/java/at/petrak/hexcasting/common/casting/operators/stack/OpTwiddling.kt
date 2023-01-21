package at.petrak.hexcasting.common.casting.operators.stack

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.iota.Iota

class OpTwiddling(val argumentCount: Int, val lookup: IntArray) : ConstMediaAction {
    override val argc: Int
        get() = this.argumentCount

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> =
        this.lookup.map(args::get)
}