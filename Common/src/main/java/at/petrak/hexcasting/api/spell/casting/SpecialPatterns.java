package at.petrak.hexcasting.api.spell.casting;

import at.petrak.hexcasting.api.spell.math.HexDir;
import at.petrak.hexcasting.api.spell.math.HexPattern;

public final class SpecialPatterns {
	public static final HexPattern INTROSPECTION = HexPattern.fromAngles("qqq", HexDir.WEST);
	public static final HexPattern RETROSPECTION = HexPattern.fromAngles("eee", HexDir.EAST);
	public static final HexPattern CONSIDERATION = HexPattern.fromAngles("qqqaw", HexDir.EAST);
}
