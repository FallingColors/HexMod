package at.petrak.hexcasting.api.spell.math

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Uses axial coordinates as per https://www.redblobgames.com/grids/hexagons/
 */
data class HexCoord(val q: Int, val r: Int) {
    fun s(): Int = this.q - this.r

    fun shiftedBy(x: HexCoord): HexCoord = HexCoord(this.q + x.q, this.r + x.r)

    fun shiftedBy(d: HexDir) = this.shiftedBy(d.asDelta())

    fun delta(x: HexCoord): HexCoord = HexCoord(this.q - x.q, this.r - x.r)

    operator fun plus(x: HexCoord) = this.shiftedBy(x)
    operator fun plus(d: HexDir) = this.shiftedBy(d)
    operator fun minus(x: HexCoord) = this.delta(x)

    fun distanceTo(x: HexCoord) =
        (abs(this.q - x.q) + abs(this.q + this.r - x.q - x.r) + abs(this.r - x.r)) / 2

    fun rangeAround(radius: Int): Iterator<HexCoord> = RingIter(this, radius)

    /** Get the direction that would bring you from this to its neighbor */
    fun immediateDelta(neighbor: HexCoord): HexDir? =
        when (neighbor - this) {
            HexCoord(1, 0) -> HexDir.EAST
            HexCoord(0, 1) -> HexDir.SOUTH_EAST
            HexCoord(-1, 1) -> HexDir.SOUTH_WEST
            HexCoord(-1, 0) -> HexDir.WEST
            HexCoord(0, -1) -> HexDir.NORTH_WEST
            HexCoord(1, -1) -> HexDir.NORTH_EAST
            else -> null
        }

    // https://docs.rs/hex2d/1.1.0/src/hex2d/lib.rs.html#785
    private class RingIter(val center: HexCoord, val radius: Int) : Iterator<HexCoord> {
        var q: Int = -radius
        var r: Int = max(-radius, 0)

        override fun hasNext(): Boolean = r <= radius + min(0, -q) || q < radius

        override fun next(): HexCoord {
            if (r > radius + min(0, -q)) {
                q++
                r = -radius + max(0, -q)
            }
            val out = HexCoord(center.q + q, center.r + r)
            r++
            return out
        }
    }

    companion object {
        @JvmStatic
        val Origin = HexCoord(0, 0)
    }
}
