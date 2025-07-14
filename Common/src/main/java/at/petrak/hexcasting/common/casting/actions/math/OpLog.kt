package at.petrak.hexcasting.common.casting.actions.math

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero
import at.petrak.hexcasting.api.utils.Vector
import kotlin.math.log

object OpLog : ConstMediaAction {
    override val argc: Int
        get() = 2

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val value = args.getDouble(0, argc)
        val base = args.getDouble(1, argc)
        if (value <= 0.0 || base <= 0.0 || base == 1.0)
            throw MishapDivideByZero.of(args[0], args[1], "logarithm")
        return log(value, base).asActionResult
    }
}
