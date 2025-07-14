package at.petrak.hexcasting.common.casting.actions.math.trig

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getDouble
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero
import at.petrak.hexcasting.api.utils.Vector
import kotlin.math.cos
import kotlin.math.tan

object OpTan : ConstMediaAction {
    override val argc: Int
        get() = 1

    override fun execute(args: Vector<Iota>, env: CastingEnvironment): Vector<Iota> {
        val angle = args.getDouble(0, argc)
        if (cos(angle) == 0.0)
            throw MishapDivideByZero.tan(args[0] as DoubleIota)
        return tan(angle).asActionResult
    }
}
