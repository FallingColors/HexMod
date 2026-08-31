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

	HexPattern ADD = HexPattern.fromDrawableAngles("waaw", HexDir.NORTH_EAST);
	HexPattern SUB = HexPattern.fromDrawableAngles("wddw", HexDir.NORTH_WEST);
	HexPattern MUL = HexPattern.fromDrawableAngles("waqaw", HexDir.SOUTH_EAST);
	HexPattern DIV = HexPattern.fromDrawableAngles("wdedw", HexDir.NORTH_EAST);
	HexPattern ABS = HexPattern.fromDrawableAngles("wqaqw", HexDir.NORTH_EAST);
	HexPattern POW = HexPattern.fromDrawableAngles("wedew", HexDir.NORTH_WEST);
	HexPattern FLOOR = HexPattern.fromDrawableAngles("ewq", HexDir.EAST);
	HexPattern CEIL = HexPattern.fromDrawableAngles("qwe", HexDir.EAST);
	HexPattern SIN = HexPattern.fromDrawableAngles("qqqqqaa", HexDir.SOUTH_EAST);
	HexPattern COS = HexPattern.fromDrawableAngles("qqqqqad", HexDir.SOUTH_EAST);
	HexPattern TAN = HexPattern.fromDrawableAngles("wqqqqqadq", HexDir.SOUTH_WEST);
	HexPattern ARCSIN = HexPattern.fromDrawableAngles("ddeeeee", HexDir.SOUTH_EAST);
	HexPattern ARCCOS = HexPattern.fromDrawableAngles("adeeeee", HexDir.NORTH_EAST);
	HexPattern ARCTAN = HexPattern.fromDrawableAngles("eadeeeeew", HexDir.NORTH_EAST);
	HexPattern ARCTAN2 = HexPattern.fromDrawableAngles("deadeeeeewd", HexDir.WEST);
	HexPattern LOG = HexPattern.fromDrawableAngles("eqaqe", HexDir.NORTH_WEST);
	HexPattern MOD = HexPattern.fromDrawableAngles("addwaad", HexDir.NORTH_EAST);


	// Vecs
	HexPattern PACK = HexPattern.fromDrawableAngles("eqqqqq", HexDir.EAST);
	HexPattern UNPACK = HexPattern.fromDrawableAngles("qeeeee", HexDir.EAST);

	// Lists
	HexPattern INDEX = HexPattern.fromDrawableAngles("deeed", HexDir.NORTH_WEST);
	HexPattern SLICE = HexPattern.fromDrawableAngles("qaeaqwded", HexDir.NORTH_WEST);
	HexPattern APPEND = HexPattern.fromDrawableAngles("edqde", HexDir.SOUTH_WEST);
	HexPattern UNAPPEND = HexPattern.fromDrawableAngles("qaeaq", HexDir.NORTH_WEST);
	HexPattern REV = HexPattern.fromDrawableAngles("qqqaede", HexDir.EAST);
	HexPattern INDEX_OF = HexPattern.fromDrawableAngles("dedqde", HexDir.EAST);
	HexPattern REMOVE = HexPattern.fromDrawableAngles("edqdewaqa", HexDir.SOUTH_WEST);
	HexPattern REPLACE = HexPattern.fromDrawableAngles("wqaeaqw", HexDir.NORTH_WEST);
	HexPattern CONS = HexPattern.fromDrawableAngles("ddewedd", HexDir.SOUTH_EAST);
	HexPattern UNCONS = HexPattern.fromDrawableAngles("aaqwqaa", HexDir.SOUTH_WEST);

	// Boolean Logic, Comparisons, & Sets
	HexPattern AND = HexPattern.fromDrawableAngles("wdw", HexDir.NORTH_EAST);
	HexPattern OR = HexPattern.fromDrawableAngles("waw", HexDir.SOUTH_EAST);
	HexPattern XOR = HexPattern.fromDrawableAngles("dwa", HexDir.NORTH_WEST);
	HexPattern GREATER = HexPattern.fromDrawableAngles("e", HexDir.SOUTH_EAST);
	HexPattern LESS = HexPattern.fromDrawableAngles("q", HexDir.SOUTH_WEST);
	HexPattern GREATER_EQ = HexPattern.fromDrawableAngles("ee", HexDir.SOUTH_EAST);
	HexPattern LESS_EQ = HexPattern.fromDrawableAngles("qq", HexDir.SOUTH_WEST);
	HexPattern NOT = HexPattern.fromDrawableAngles("dw", HexDir.NORTH_WEST);
	HexPattern UNIQUE = HexPattern.fromDrawableAngles("aweaqa", HexDir.NORTH_EAST);
}
