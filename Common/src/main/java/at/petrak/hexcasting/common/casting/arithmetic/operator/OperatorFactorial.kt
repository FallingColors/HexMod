package at.petrak.hexcasting.common.casting.arithmetic.operator

import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBasic
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate
import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.DOUBLE
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes.VEC3
import net.minecraft.world.phys.Vec3
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

object OperatorFactorial : OperatorBasic(1, IotaMultiPredicate.all(IotaPredicate.or(
    IotaPredicate.ofType(DOUBLE), IotaPredicate.ofType(VEC3)
))) {
    override fun apply(iotas: Iterable<Iota>, env : CastingEnvironment): Iterable<Iota> {
        val iota = iotas.first()
        return when (iota) {
            is DoubleIota -> compute(iota.double).asActionResult
            is Vec3Iota -> Vec3(compute(iota.vec3.x), compute(iota.vec3.y), compute(iota.vec3.z)).asActionResult
            else -> throw MishapInvalidIota.of(iota, 0, "numvec")
        }
    }

    // Take the standard factorial if it's an integer, otherwise use the gamma function.
    private fun compute(arg: Double): Double {
        val argInt = arg.roundToInt()
        if (arg >= 0 && DoubleIota.tolerates(arg, argInt.toDouble())) {
            return factorial(argInt)
        }
        return exp(logGamma(arg + 1))
    }

    private fun factorial(number: Int): Double {
        var result: Long = 1

        for (factor in 2..number) {
            result *= factor
        }

        return result.toDouble()
    }

    // https://introcs.cs.princeton.edu/java/91float/Gamma.java.html
    private fun logGamma(x: Double): Double {
        val ser =  ( 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5) )

        return (x - 0.5) * ln(x + 4.5) - (x + 4.5) + ln(ser * sqrt(2*PI))
    }
}