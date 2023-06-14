package at.petrak.hexcasting.api.casting.arithmetic;

import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.math.HexDir;
import at.petrak.hexcasting.api.casting.math.HexPattern;

/**
 * This is the interface to implement if you want to override the behaviour of an Operator pattern like ADD, SUB, etc. for some type/s of
 * iotas for which that Operator pattern is not yet defined.
 */
public interface Arithmetic {
	String arithName();

	/**
	 * @return All the HexPatterns for which this Arithmetic has defined Operators.
	 */
	Iterable<HexPattern> opTypes();

	/**
	 * @param pattern The HexPattern that would be drawn by the caster.
	 * @return The Operator that this Arithmetic has defined for that pattern.
	 */
	Operator getOperator(HexPattern pattern);

	// Below are some common Operator patterns that you can make use of in your Arithmetic:

	HexPattern ADD = HexPattern.fromAngles("waaw", HexDir.NORTH_EAST);
	HexPattern SUB = HexPattern.fromAngles("wddw", HexDir.NORTH_WEST);
	HexPattern MUL = HexPattern.fromAngles("waqaw", HexDir.SOUTH_EAST);
	HexPattern DIV = HexPattern.fromAngles("wdedw", HexDir.NORTH_EAST);
	HexPattern ABS = HexPattern.fromAngles("wqaqw", HexDir.NORTH_EAST);
	HexPattern POW = HexPattern.fromAngles("wedew", HexDir.NORTH_WEST);
	HexPattern FLOOR = HexPattern.fromAngles("ewq", HexDir.EAST);
	HexPattern CEIL = HexPattern.fromAngles("qwe", HexDir.EAST);
	HexPattern SIN = HexPattern.fromAngles("qqqqqaa", HexDir.SOUTH_EAST);
	HexPattern COS = HexPattern.fromAngles("qqqqqad", HexDir.SOUTH_EAST);
	HexPattern TAN = HexPattern.fromAngles("wqqqqqadq", HexDir.SOUTH_WEST);
	HexPattern ARCSIN = HexPattern.fromAngles("ddeeeee", HexDir.SOUTH_EAST);
	HexPattern ARCCOS = HexPattern.fromAngles("adeeeee", HexDir.NORTH_EAST);
	HexPattern ARCTAN = HexPattern.fromAngles("eadeeeeew", HexDir.NORTH_EAST);
	HexPattern ARCTAN2 = HexPattern.fromAngles("deadeeeeewd", HexDir.WEST);
	HexPattern LOG = HexPattern.fromAngles("eqaqe", HexDir.NORTH_WEST);
	HexPattern MOD = HexPattern.fromAngles("addwaad", HexDir.NORTH_EAST);


	// Vecs
	HexPattern PACK = HexPattern.fromAngles("eqqqqq", HexDir.EAST);
	HexPattern UNPACK = HexPattern.fromAngles("qeeeee", HexDir.EAST);

	// Lists
	HexPattern INDEX = HexPattern.fromAngles("deeed", HexDir.NORTH_WEST);
	HexPattern SLICE = HexPattern.fromAngles("qaeaqwded", HexDir.NORTH_WEST);
	HexPattern APPEND = HexPattern.fromAngles("edqde", HexDir.SOUTH_WEST);
	HexPattern UNAPPEND = HexPattern.fromAngles("qaeaq", HexDir.NORTH_WEST);
	HexPattern REV = HexPattern.fromAngles("qqqaede", HexDir.EAST);
	HexPattern INDEX_OF = HexPattern.fromAngles("dedqde", HexDir.EAST);
	HexPattern REMOVE = HexPattern.fromAngles("edqdewaqa", HexDir.SOUTH_WEST);
	HexPattern REPLACE = HexPattern.fromAngles("wqaeaqw", HexDir.NORTH_WEST);
	HexPattern CONS = HexPattern.fromAngles("ddewedd", HexDir.SOUTH_EAST);
	HexPattern UNCONS = HexPattern.fromAngles("aaqwqaa", HexDir.SOUTH_WEST);

	// Boolean Logic, Comparisons, & Sets
	HexPattern AND = HexPattern.fromAngles("wdw", HexDir.NORTH_EAST);
	HexPattern OR = HexPattern.fromAngles("waw", HexDir.SOUTH_EAST);
	HexPattern XOR = HexPattern.fromAngles("dwa", HexDir.NORTH_WEST);
	HexPattern GREATER = HexPattern.fromAngles("e", HexDir.SOUTH_EAST);
	HexPattern LESS = HexPattern.fromAngles("q", HexDir.SOUTH_WEST);
	HexPattern GREATER_EQ = HexPattern.fromAngles("ee", HexDir.SOUTH_EAST);
	HexPattern LESS_EQ = HexPattern.fromAngles("qq", HexDir.SOUTH_WEST);
	HexPattern NOT = HexPattern.fromAngles("dw", HexDir.NORTH_WEST);
	HexPattern UNIQUE = HexPattern.fromAngles("aweaqa", HexDir.NORTH_EAST);
}
