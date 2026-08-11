package at.petrak.hexcasting.api.casting.math

enum class HexAngle {
    FORWARD, RIGHT, RIGHT_BACK, BACK, LEFT_BACK, LEFT;

    fun rotatedBy(a: HexAngle) = values()[(this.ordinal + a.ordinal) % values().size]
    operator fun times(a: HexAngle) = this.rotatedBy(a)

    companion object {
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
