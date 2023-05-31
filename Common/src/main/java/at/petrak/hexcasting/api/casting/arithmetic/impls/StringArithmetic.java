package at.petrak.hexcasting.api.casting.arithmetic.impls;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorBinary;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;

import java.util.List;

public enum StringArithmetic implements Arithmetic {
	INSTANCE;

	public static final List<HexPattern> OPS = List.of(
		new ADD,
		new MUL,
		new ABS
	);

	@Override
	public String arithName() {
		return "string_math";
	}

	@Override
	public Iterable<HexPattern> opTypes() {
		return OPS;
	}

	@Override
	public Operator getOperator(HexPattern pattern) {
		switch (pattern) {
		case ADD: return new OperatorBinary(
				IotaMultiPredicate.all(IotaPredicate.ofType(HexIotaTypes.BOOLEAN)),
				(p, q) -> new Iota(p.downcast(String.class) + q.downcast(String.class)));
		case MUL: return new OperatorBinary(
				IotaMultiPredicate.bofa(IotaPredicate.ofClass(String.class), IotaPredicate.ofClass(Double.class)),
				(p, q) -> new Iota(new String(new char[q.downcast(Double.class).intValue()]).replace("\0", p.downcast(String.class))));
		case ABS: return new OperatorUnary(
				IotaMultiPredicate.all(IotaPredicate.ofClass(String.class)),
				s -> new Iota((double) s.downcast(String.class).length()));
		}
		return null;
	}
}
