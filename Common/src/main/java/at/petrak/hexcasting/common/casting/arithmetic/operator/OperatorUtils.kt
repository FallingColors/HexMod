package at.petrak.hexcasting.common.casting.arithmetic.operator

import at.petrak.hexcasting.api.casting.SpellList
import at.petrak.hexcasting.api.casting.iota.DoubleIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import kotlin.math.abs
import kotlin.math.roundToInt

fun Iterator<IndexedValue<Iota>>.nextList(argc: Int = 0): SpellList {
    val (idx, x) = this.next()
    if (x is ListIota) {
        return x.list
    } else {
        throw MishapInvalidIota.ofType(x, if (argc == 0) idx else argc - (idx + 1), "list")
    }
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