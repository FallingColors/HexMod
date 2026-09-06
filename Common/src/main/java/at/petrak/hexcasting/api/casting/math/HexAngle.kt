package at.petrak.hexcasting.api.casting.math

enum class HexAngle {
    FORWARD, RIGHT, RIGHT_BACK, BACK, LEFT_BACK, LEFT;

    fun rotatedBy(a: HexAngle) = entries[(this.ordinal + a.ordinal) % entries.size]
    operator fun times(a: HexAngle) = this.rotatedBy(a)

    fun inverse() = entries[(entries.size - this.ordinal) % entries.size]

    fun toChar(): Char {
        return when (this) {
            FORWARD -> 'w'
            RIGHT -> 'e'
            RIGHT_BACK -> 'd'
            BACK -> 's'
            LEFT_BACK -> 'a'
            LEFT -> 'q'
        }
    }

    companion object {
        @JvmStatic
        fun fromChar(c: Char): HexAngle? {
            return when (c) {
                'w' -> FORWARD
                'e' -> RIGHT
                'd' -> RIGHT_BACK
                // for completeness ...
                's' -> BACK
                'a' -> LEFT_BACK
                'q' -> LEFT
                else -> null
            }
        }
    }
}
