package at.petrak.hexcasting.api.casting.arithmetic.impls;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary;
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;

import java.util.List;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;

import static at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.downcast;
import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*;

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
		switch (pattern) {
			case ADD: return make2(Double::sum);
			case SUB: return make2((p, q) -> p - q);
			case MUL: return make2((p, q) -> p * q);
			case DIV: return make2((p, q) -> p / q);
			case ABS: return make1(Math::abs);
			case POW: return make2(Math::pow);
			case FLOOR: return make1(Math::floor);
			case CEIL: return make1(Math::ceil);
			case MOD: return make2((p, q) -> p % q);
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
