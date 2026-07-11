package at.petrak.hexcasting.common.casting.arithmetic.operator

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun Iterator<IndexedValue<Iota>>.nextList(argc: Int = 0): SpellList {
    val (idx, x) = this.next()
    if (x is ListIota) {
        return x.list
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "list")
    }
}

fun Iterator<IndexedValue<Iota>>.nextDouble(argc: Int = 0): Double {
    val (idx, x) = this.next()
    if (x is DoubleIota) return x.double
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "double")
}

fun Iterator<IndexedValue<Iota>>.nextInt(argc: Int = 0): Int {
    val (idx, x) = this.next()
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int")
}

fun Iterator<IndexedValue<Iota>>.nextPositiveIntUnder(max: Int, argc: Int = 0): Int {
    val (idx, x) = this.next()
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in 0 until max) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.positive.less.equal", max)
}

fun Iterator<IndexedValue<Iota>>.nextPositiveIntUnderInclusive(max: Int, argc: Int = 0): Int {
    val (idx, x) = this.next()
    if (x is DoubleIota) {
        val double = x.double
        val rounded = double.roundToInt()
        if (abs(double - rounded) <= DoubleIota.TOLERANCE && rounded in 0..max) {
            return rounded
        }
    }
    throw MishapInvalidIota.of(x, if (argc == 0) idx else argc - (idx + 1), "int.positive.less.equal", max)
}

/**
 * Returns the double if it is between [min] and [max] (inclusive), and throws a mishap otherwise. [idx] should be
 * the double's index from the top of the stack (i.e. top iota has [idx]=0, second from the top has [idx]=1, etc.).
 */
fun Double.asDoubleBetween(min: Double, max: Double, idx: Int) = if (this in min .. max) this
    else throw MishapInvalidIota.of(DoubleIota(this), idx, "double.between", min, max)

/**
 * Takes the standard factorial if `arg` is a positive integer, otherwise uses the gamma function.
 */
fun generalFactorial(arg: Double): Double {
    val argInt = arg.roundToInt()
    if (arg >= 0 && DoubleIota.tolerates(arg, argInt.toDouble())) {
        // normal integer factorial
        var result = 1.0
        for (factor in 2..argInt) {
            result *= factor
        }
        return result
    }
    // gamma function of arg+1 for non-integer factorial
    // https://introcs.cs.princeton.edu/java/91float/Gamma.java.html
    val x = arg + 1
    val ser = ( 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
                    + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
                    +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5) )
    val logGamma = (x - 0.5) * ln(x + 4.5) - (x + 4.5) + ln(ser * sqrt(2*PI))
    return exp(logGamma)
}