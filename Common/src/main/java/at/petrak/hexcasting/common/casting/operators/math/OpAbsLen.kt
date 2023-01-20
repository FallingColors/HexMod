package at.petrak.hexcasting.common.casting.operators.math

import at.petrak.hexcasting.api.casting.ConstMediaAction
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingContext
import at.petrak.hexcasting.api.casting.getNumOrVec
import at.petrak.hexcasting.api.casting.iota.Iota
import kotlin.math.absoluteValue

object OpAbsLen : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: List<Iota>, ctx: CastingContext): List<Iota> {
        val x = args.getNumOrVec(0, argc)

        return x.map({ num -> num.absoluteValue }, { vec -> vec.length() }).asActionResult
    }
}
