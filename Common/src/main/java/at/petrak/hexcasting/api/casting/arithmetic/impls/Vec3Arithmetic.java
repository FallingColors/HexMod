package at.petrak.hexcasting.api.casting.arithmetic.impls;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IotaPredicate;
import at.petrak.hexcasting.api.casting.math.HexPattern;

import java.util.ArrayList;
import java.util.List;

import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*;

public enum Vec3Arithmetic implements Arithmetic {
	INSTANCE;

	public static final List<HexPattern> OPS;
	static {
		var ops = new ArrayList<>(DoubleArithmetic.OPS);
		ops.add(PACK);
		ops.add(UNPACK);
		ops.remove(FLOOR);
		ops.remove(CEIL);
		OPS = ops;
	}

	public static final IotaMultiPredicate ACCEPTS = IotaMultiPredicate.any(IotaPredicate.ofType(VEC3), IotaPredicate.ofType(DOUBLE));

	@Override
	public String arithName() {
		return "vec3_math";
	}

	@Override
	public Iterable<Symbol> opTypes() {
		return OPS;
	}

	@Override
	public Operator getOperator(Symbol name) {
		switch (name.inner()) {
		case "pack":  return OperatorPack.INSTANCE;
		case "add":   return make2(name, null);
		case "sub":   return make2(name, null);
		case "mul":   return make2(name, Vec3::dot);
		case "div":   return make2(name, Vec3::cross);
		case "abs":   return make1(Vec3::len);
		case "pow":   return make2(name, Vec3::proj);
		case "mod":   return make2(name, null);
		}
		return null;
	}
	public static OperatorUnary make1(Function<Vec3, Object> op) {
		return new OperatorUnary(ACCEPTS, i -> new Iota(op.apply(i.downcast(Vec3.class))));
	}
	public static OperatorVec3Delegating make2(Symbol name, BiFunction<Vec3, Vec3, Object> op) {
		return new OperatorVec3Delegating(op, name);
	}
}
