package at.petrak.hexcasting.api.casting.arithmetic;

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public interface Arithmetic {
	public String arithName();

	public abstract Iterable<HexPattern> opTypes();

	public abstract Operator getOperator(HexPattern pattern);

	public static HexPattern ADD = HexPattern.fromAngles("waaw", HexDir.NORTH_EAST);
	public static HexPattern SUB = HexPattern.fromAngles("wddw", HexDir.NORTH_WEST);
	public static HexPattern MUL = HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST);
	public static HexPattern DIV = HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST);
	public static HexPattern ABS = HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST);
	public static HexPattern POW = HexPattern.fromAngles("wedew", HexDir.NORTH_WEST);
	public static HexPattern FLOOR = HexPattern.fromAngles("ewq", HexDir.EAST);
	public static HexPattern CEIL = HexPattern.fromAngles("qwe", HexDir.EAST);
	public static HexPattern MOD = HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST);
	public static HexPattern PACK = HexPattern.fromAngles("eqqqqq", HexDir.EAST);
	public static HexPattern UNPACK = HexPattern.fromAngles("qeeeee", HexDir.EAST);
}
