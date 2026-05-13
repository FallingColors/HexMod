package at.petrak.hexcasting.api.casting.eval;

import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public final class SpecialPatterns {
    public static final HexPattern INTROSPECTION = HexPattern.fromAngles("qqq", HexDir.WEST);
    public static final HexPattern RETROSPECTION = HexPattern.fromAngles("eee", HexDir.EAST);
    public static final HexPattern MEDITATION = HexPattern.fromAngles("eqqqe", HexDir.SOUTH_WEST);
    public static final HexPattern RECOLLECTION = HexPattern.fromAngles("qeeeq", HexDir.SOUTH_EAST);
    public static final HexPattern CONSIDERATION = HexPattern.fromAngles("qqqaw", HexDir.WEST);
    public static final HexPattern EVANITION = HexPattern.fromAngles("eeedw", HexDir.EAST);
}
