package at.petrak.hexcasting.api.spell.math

import java.util.*

enum class HexDir {
    NORTH_EAST, EAST, SOUTH_EAST, SOUTH_WEST, WEST, NORTH_WEST;

    fun rotatedBy(a: HexAngle): HexDir =
        values()[(this.ordinal + a.ordinal).mod(values().size)]

    operator fun times(a: HexAngle) = this.rotatedBy(a)

    fun angleFrom(other: HexDir): HexAngle =
        HexAngle.values()[(this.ordinal - other.ordinal).mod(HexAngle.values().size)]

    operator fun minus(other: HexDir) = this.angleFrom(other)

    fun asDelta(): HexCoord =
        when (this) {
            NORTH_EAST -> HexCoord(1, -1)
            EAST -> HexCoord(1, 0)
            SOUTH_EAST -> HexCoord(0, 1)
            SOUTH_WEST -> HexCoord(-1, 1)
            WEST -> HexCoord(-1, 0)
            NORTH_WEST -> HexCoord(0, -1)
        }

    companion object {
        @JvmStatic
        fun fromString(key: String): HexDir {
            val lowercaseKey = key.lowercase(Locale.ROOT)
            return values().firstOrNull { it.name.lowercase(Locale.ROOT) == lowercaseKey } ?: WEST
        }
    }
}
