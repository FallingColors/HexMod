package at.petrak.hexcasting.common.casting.arithmetic;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.engine.InvalidOperatorException;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.operator.*;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.casting.arithmetic.operator.vec.OperatorPack;
import at.petrak.hexcasting.common.casting.arithmetic.operator.vec.OperatorUnpack;
import at.petrak.hexcasting.common.casting.arithmetic.operator.vec.OperatorVec3Delegating;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.downcast;
import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*;

public enum Vec3Arithmetic implements Arithmetic {
	INSTANCE;

	public static final List<HexPattern> OPS = List.of(
			PACK,
			UNPACK,
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

	public static final IotaMultiPredicate ACCEPTS = IotaMultiPredicate.any(IotaPredicate.ofType(VEC3), IotaPredicate.ofType(DOUBLE));

	@Override
	public String arithName() {
		return "vec3_math";
	}

	@Override
	public Iterable<HexPattern> opTypes() {
		return OPS;
	}

	@Override
	public Operator getOperator(HexPattern pattern) {
		if (pattern.equals(PACK)) {
			return OperatorPack.INSTANCE;
		} else if (pattern.equals(UNPACK)) {
			return OperatorUnpack.INSTANCE;
		} else if (pattern.equals(ADD)) {
			return make2Fallback(pattern);
		} else if (pattern.equals(SUB)) {
			return make2Fallback(pattern);
		} else if (pattern.equals(MUL)) {
			return make2Double(pattern, Vec3::dot);
		} else if (pattern.equals(DIV)) {
			return make2Vec(pattern, Vec3::cross);
		} else if (pattern.equals(ABS)) {
			return make1Double(Vec3::length);
		} else if (pattern.equals(POW)) {
			return make2Vec(pattern, (u, v) -> v.normalize().scale(u.dot(v.normalize())));
		} else if (pattern.equals(FLOOR)) {
			return make1(v -> new Vec3(Math.floor(v.x), Math.floor(v.y), Math.floor(v.z)));
		} else if (pattern.equals(CEIL)) {
			return make1(v -> new Vec3(Math.ceil(v.x), Math.ceil(v.y), Math.ceil(v.z)));
		} else if (pattern.equals(MOD)) {
			return make2Fallback(pattern);
		}
		throw new InvalidOperatorException(pattern + " is not a valid operator in Arithmetic " + this + ".");
	}
	public static OperatorUnary make1(Function<Vec3, Vec3> op) {
		return new OperatorUnary(ACCEPTS, i -> new Vec3Iota(op.apply(downcast(i, VEC3).getVec3())));
	}
	public static OperatorUnary make1Double(Function<Vec3, Double> op) {
		return new OperatorUnary(ACCEPTS, i -> new DoubleIota(op.apply(downcast(i, VEC3).getVec3())));
	}
	public static OperatorVec3Delegating make2Fallback(HexPattern pattern) {
		return new OperatorVec3Delegating(null, pattern);
	}
	public static OperatorVec3Delegating make2Double(HexPattern pattern, BiFunction<Vec3, Vec3, Double> op) {
		return new OperatorVec3Delegating(op.andThen(DoubleIota::new), pattern);
	}
	public static OperatorVec3Delegating make2Vec(HexPattern pattern, BiFunction<Vec3, Vec3, Vec3> op) {
		return new OperatorVec3Delegating(op.andThen(Vec3Iota::new), pattern);
	}
}
