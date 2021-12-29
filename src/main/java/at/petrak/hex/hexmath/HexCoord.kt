package at.petrak.hex.hexmath

import java.lang.Integer.max
import kotlin.math.abs
import kotlin.math.min

/**
 * Uses axial coordinates as per https://www.redblobgames.com/grids/hexagons/
 */
@JvmRecord
data class HexCoord(val q: Int, val r: Int) {
    fun s(): Int = this.q - this.r

    fun shiftedBy(x: HexCoord): HexCoord = HexCoord(this.q + x.q, this.r + x.r)

    fun shiftedBy(d: HexDir) = this.shiftedBy(d.asDelta())

    operator fun plus(x: HexCoord) = this.shiftedBy(x)
    operator fun plus(d: HexDir) = this.shiftedBy(d)

    fun distanceTo(x: HexCoord) =
        (abs(this.q - x.q) + abs(this.q + this.r - x.q - x.r) + abs(this.r - x.r)) / 2

    fun rangeAround(radius: Int): Iterator<HexCoord> = RingIter(this, radius)

    // https://docs.rs/hex2d/1.1.0/src/hex2d/lib.rs.html#785
    private class RingIter(val center: HexCoord, val radius: Int) : Iterator<HexCoord> {
        var q: Int = -center.r
        var r: Int = max(-center.r, -(-center.r) - center.r) // i could not tell you what the hell this line does

        // de bruin's rule on the original code? i have no idea what any of this does?
        override fun hasNext(): Boolean = r <= min(radius, -q + radius) || this.q < radius

        override fun next(): HexCoord {
            if (r > min(radius, -q + radius)) {
                q++
                r = max(-center.r, -q - center.r)
            }
            val out = HexCoord(center.q + q, center.r + r)
            r++
            return out
        }
    }

    companion object {
        val Origin = HexCoord(0, 0)
    }
}
