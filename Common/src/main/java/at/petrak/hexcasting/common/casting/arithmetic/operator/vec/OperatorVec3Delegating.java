package at.petrak.hexcasting.common.casting.arithmetic.operator.vec;

import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IterPair;
import at.petrak.hexcasting.api.casting.arithmetic.TripleIterable;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapDivideByZero;
import at.petrak.hexcasting.common.casting.arithmetic.DoubleArithmetic;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.DOUBLE;
import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.VEC3;

public class OperatorVec3Delegating extends Operator {
	private final BiFunction<Vec3, Vec3, Iota> op;
	private final Operator fb;
	public OperatorVec3Delegating(BiFunction<Vec3, Vec3, Iota> core, HexPattern fallback) {
		super(2, IotaMultiPredicate.any(IotaPredicate.ofType(VEC3), IotaPredicate.ofType(DOUBLE)));
		op = core;
		fb = Objects.requireNonNull(DoubleArithmetic.INSTANCE.getOperator(fallback));
	}

	@Override
	public @NotNull Iterable<Iota> apply(@NotNull Iterable<Iota> iotas, @NotNull CastingEnvironment env) throws Mishap {
		var it = iotas.iterator();
		var left = it.next();
		var right = it.next();
		try {
			if (op != null && left instanceof Vec3Iota lh && right instanceof Vec3Iota rh) {
				return List.of(op.apply(lh.getVec3(), rh.getVec3()));
			}
			var lh = left instanceof Vec3Iota l ? l.getVec3() : triplicate(downcast(left, DOUBLE).getDouble());
			var rh = right instanceof Vec3Iota r ? r.getVec3() : triplicate(downcast(right, DOUBLE).getDouble());
			return new TripleIterable<>(
					fb.apply(new IterPair<>(new DoubleIota(lh.x()), new DoubleIota(rh.x())), env),
					fb.apply(new IterPair<>(new DoubleIota(lh.y()), new DoubleIota(rh.y())), env),
					fb.apply(new IterPair<>(new DoubleIota(lh.z()), new DoubleIota(rh.z())), env),
					(x, y, z) -> new Vec3Iota(new Vec3(downcast(x, DOUBLE).getDouble(), downcast(y, DOUBLE).getDouble(), downcast(z, DOUBLE).getDouble()))
			);
		} catch (MishapDivideByZero e) {
			throw MishapDivideByZero.of(left, right, e.getSuffix());
		}
	}

	public static Vec3 triplicate(double in) {
		return new Vec3(in, in, in);
	}
}
