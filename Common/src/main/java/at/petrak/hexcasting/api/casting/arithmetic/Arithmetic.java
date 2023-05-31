package at.petrak.hexcasting.api.casting.arithmetic;

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;

public interface Arithmetic {
	String arithName();

	Iterable<HexPattern> opTypes();

	Operator getOperator(HexPattern pattern);

	HexPattern ADD = HexPattern.fromAngles("waaw", HexDir.NORTH_EAST);
	HexPattern SUB = HexPattern.fromAngles("wddw", HexDir.NORTH_WEST);
	HexPattern MUL = HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST);
	HexPattern DIV = HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST);
	HexPattern ABS = HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST);
	HexPattern POW = HexPattern.fromAngles("wedew", HexDir.NORTH_WEST);
	HexPattern FLOOR = HexPattern.fromAngles("ewq", HexDir.EAST);
	HexPattern CEIL = HexPattern.fromAngles("qwe", HexDir.EAST);
	HexPattern MOD = HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST);
	HexPattern PACK = HexPattern.fromAngles("eqqqqq", HexDir.EAST);
	HexPattern UNPACK = HexPattern.fromAngles("qeeeee", HexDir.EAST);
}
