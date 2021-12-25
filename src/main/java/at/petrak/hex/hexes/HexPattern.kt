package at.petrak.hex.hexes

/**
 * Sequence of angles to define a pattern traced.
 */
@JvmRecord
data class HexPattern(val startDir: HexDir, val angles: MutableList<HexAngle> = arrayListOf()) {
    /**
     * @return True if it successfully appended, false if not.
     */
    fun tryAppendDir(newDir: HexDir): Boolean {
        // Two restrictions:
        // - No adding a pos/dir pair we previously added
        // - No backtracking
        val linesSeen = mutableSetOf<Pair<HexCoord, HexDir>>()

        var compass = this.startDir
        var cursor = HexCoord.Origin
        for (a in this.angles) {
            linesSeen.add(Pair(cursor, compass))
            cursor += compass
            compass *= a
        }

        val potentialNewLine = Pair(cursor, newDir)
        if (potentialNewLine in linesSeen) return false
        val nextAngle = newDir - compass
        if (nextAngle == HexAngle.BACK) return false

        this.angles.add(nextAngle)
        return true
    }

    @JvmOverloads
    fun positions(start: HexCoord = HexCoord.Origin): List<HexCoord> {
        val out: ArrayList<HexCoord> = ArrayList(this.angles.size + 2)
        out.add(start)
        var compass: HexDir = this.startDir
        var cursor = start
        for (a in this.angles) {
            cursor += compass
            out.add(cursor)
            compass *= a
        }
        out.add(cursor.shiftedBy(compass))
        return out
    }

    // Terrible shorthand method for easy matching
    fun anglesSignature(): String {
        return buildString {
            for (a in this@HexPattern.angles) {
                append(
                    when (a) {
                        HexAngle.FORWARD -> "w"
                        HexAngle.RIGHT -> "e"
                        HexAngle.RIGHT_BACK -> "d"
                        HexAngle.BACK -> "s"
                        HexAngle.LEFT_BACK -> "a"
                        HexAngle.LEFT -> "q"
                    }
                )
            }
        }
    }

    override fun toString(): String = buildString {
        append("HexPattern[")
        append(this@HexPattern.startDir)
        append(", ")
        append(this@HexPattern.anglesSignature())
        append("]")
    }
}