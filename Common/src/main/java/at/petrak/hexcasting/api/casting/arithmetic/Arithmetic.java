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

	HexPattern ADD = HexPattern.fromAngleString("waaw", HexDir.NORTH_EAST);
	HexPattern SUB = HexPattern.fromAngleString("wddw", HexDir.NORTH_WEST);
	HexPattern MUL = HexPattern.fromAngleString("waqaw", HexDir.SOUTH_EAST);
	HexPattern DIV = HexPattern.fromAngleString("wdedw", HexDir.NORTH_EAST);
	HexPattern ABS = HexPattern.fromAngleString("wqaqw", HexDir.NORTH_EAST);
	HexPattern POW = HexPattern.fromAngleString("wedew", HexDir.NORTH_WEST);
	HexPattern FLOOR = HexPattern.fromAngleString("ewq", HexDir.EAST);
	HexPattern CEIL = HexPattern.fromAngleString("qwe", HexDir.EAST);
	HexPattern SIN = HexPattern.fromAngleString("qqqqqaa", HexDir.SOUTH_EAST);
	HexPattern COS = HexPattern.fromAngleString("qqqqqad", HexDir.SOUTH_EAST);
	HexPattern TAN = HexPattern.fromAngleString("wqqqqqadq", HexDir.SOUTH_WEST);
	HexPattern ARCSIN = HexPattern.fromAngleString("ddeeeee", HexDir.SOUTH_EAST);
	HexPattern ARCCOS = HexPattern.fromAngleString("adeeeee", HexDir.NORTH_EAST);
	HexPattern ARCTAN = HexPattern.fromAngleString("eadeeeeew", HexDir.NORTH_EAST);
	HexPattern ARCTAN2 = HexPattern.fromAngleString("deadeeeeewd", HexDir.WEST);
	HexPattern LOG = HexPattern.fromAngleString("eqaqe", HexDir.NORTH_WEST);
	HexPattern MOD = HexPattern.fromAngleString("addwaad", HexDir.NORTH_EAST);


	// Vecs
	HexPattern PACK = HexPattern.fromAngleString("eqqqqq", HexDir.EAST);
	HexPattern UNPACK = HexPattern.fromAngleString("qeeeee", HexDir.EAST);

	// Lists
	HexPattern INDEX = HexPattern.fromAngleString("deeed", HexDir.NORTH_WEST);
	HexPattern SLICE = HexPattern.fromAngleString("qaeaqwded", HexDir.NORTH_WEST);
	HexPattern APPEND = HexPattern.fromAngleString("edqde", HexDir.SOUTH_WEST);
	HexPattern UNAPPEND = HexPattern.fromAngleString("qaeaq", HexDir.NORTH_WEST);
	HexPattern REV = HexPattern.fromAngleString("qqqaede", HexDir.EAST);
	HexPattern INDEX_OF = HexPattern.fromAngleString("dedqde", HexDir.EAST);
	HexPattern REMOVE = HexPattern.fromAngleString("edqdewaqa", HexDir.SOUTH_WEST);
	HexPattern REPLACE = HexPattern.fromAngleString("wqaeaqw", HexDir.NORTH_WEST);
	HexPattern CONS = HexPattern.fromAngleString("ddewedd", HexDir.SOUTH_EAST);
	HexPattern UNCONS = HexPattern.fromAngleString("aaqwqaa", HexDir.SOUTH_WEST);

	// Boolean Logic, Comparisons, & Sets
	HexPattern AND = HexPattern.fromAngleString("wdw", HexDir.NORTH_EAST);
	HexPattern OR = HexPattern.fromAngleString("waw", HexDir.SOUTH_EAST);
	HexPattern XOR = HexPattern.fromAngleString("dwa", HexDir.NORTH_WEST);
	HexPattern GREATER = HexPattern.fromAngleString("e", HexDir.SOUTH_EAST);
	HexPattern LESS = HexPattern.fromAngleString("q", HexDir.SOUTH_WEST);
	HexPattern GREATER_EQ = HexPattern.fromAngleString("ee", HexDir.SOUTH_EAST);
	HexPattern LESS_EQ = HexPattern.fromAngleString("qq", HexDir.SOUTH_WEST);
	HexPattern NOT = HexPattern.fromAngleString("dw", HexDir.NORTH_WEST);
	HexPattern UNIQUE = HexPattern.fromAngleString("aweaqa", HexDir.NORTH_EAST);
}
