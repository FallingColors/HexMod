package at.petrak.hexcasting.api.spell.casting

import at.petrak.hexcasting.api.spell.math.HexDir
import at.petrak.hexcasting.api.spell.math.HexPattern

object SpecialPatterns {
    val INTROSPECTION = HexPattern.fromAngles("qqq", HexDir.WEST)
    val RETROSPECTION = HexPattern.fromAngles("eee", HexDir.EAST)
    val CONSIDERATION = HexPattern.fromAngles("qqqaw", HexDir.EAST)
}