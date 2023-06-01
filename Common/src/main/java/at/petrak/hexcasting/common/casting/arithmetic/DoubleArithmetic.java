package at.petrak.hexcasting.common.casting.arithmetic;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary;
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.arithmetic.operator.OperatorLog;

import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import static at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.downcast;
import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.DOUBLE;

public enum DoubleArithmetic implements Arithmetic {
	INSTANCE;

	public static final List<HexPattern> OPS = List.of(
		ADD,
		SUB,
		MUL,
		DIV,
		ABS,
		POW,
		FLOOR,
		CEIL,
		SIN,
		COS,
		TAN,
		ARCSIN,
		ARCCOS,
		ARCTAN,
		ARCTAN2,
		LOG,
		MOD
	);

	public static final IotaMultiPredicate ACCEPTS = IotaMultiPredicate.all(IotaPredicate.ofType(DOUBLE));

	@Override
	public String arithName() {
		return "double_math";
	}

	@Override
	public Iterable<HexPattern> opTypes() {
		return OPS;
	}

	@Override
	public Operator getOperator(HexPattern pattern) {
		if (pattern.equals(ADD)) {
			return make2(Double::sum);
		} else if (pattern.equals(SUB)) {
			return make2((p, q) -> p - q);
		} else if (pattern.equals(MUL)) {
			return make2((p, q) -> p * q);
		} else if (pattern.equals(DIV)) {
			return make2((p, q) -> p / q);
		} else if (pattern.equals(ABS)) {
			return make1(Math::abs);
		} else if (pattern.equals(POW)) {
			return make2(Math::pow);
		} else if (pattern.equals(FLOOR)) {
			return make1(Math::floor);
		} else if (pattern.equals(CEIL)) {
			return make1(Math::ceil);
		} else if (pattern.equals(SIN)) {
			return make1(Math::sin);
		} else if (pattern.equals(COS)) {
			return make1(Math::cos);
		} else if (pattern.equals(TAN)) {
			return make1(Math::tan);
		} else if (pattern.equals(ARCSIN)) {
			return make1(Math::asin);
		} else if (pattern.equals(ARCCOS)) {
			return make1(Math::acos);
		} else if (pattern.equals(ARCTAN)) {
			return make1(Math::atan);
		} else if (pattern.equals(ARCTAN2)) {
			return make2(Math::atan2);
		} else if (pattern.equals(LOG)) {
			return OperatorLog.INSTANCE;
		} else if (pattern.equals(MOD)) {
			return make2((p, q) -> p % q);
		}
		return null;
	}
	public static OperatorUnary make1(DoubleUnaryOperator op) {
		return new OperatorUnary(ACCEPTS, i -> new DoubleIota(op.applyAsDouble(downcast(i, DOUBLE).getDouble())));
	}
	public static OperatorBinary make2(DoubleBinaryOperator op) {
		return new OperatorBinary(ACCEPTS, (i, j) -> new DoubleIota(op.applyAsDouble(downcast(i, DOUBLE).getDouble(), downcast(j, DOUBLE).getDouble())));
	}
}
