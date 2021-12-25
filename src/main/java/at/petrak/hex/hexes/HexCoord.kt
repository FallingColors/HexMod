package at.petrak.hex.hexes

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

    companion object {
        val Origin = HexCoord(0, 0)
    }
}
