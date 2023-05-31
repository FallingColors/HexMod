package at.petrak.hexcasting.api.casting.arithmetic.operator;

import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IterPair;
import at.petrak.hexcasting.api.casting.arithmetic.impls.DoubleArithmetic;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import kotlin.Pair;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.function.BiFunction;

public class OperatorVec3Delegating extends Operator {
	private final BiFunction<Vec3, Vec3, Iota> op;
	private final Operator fb;
	public OperatorVec3Delegating(BiFunction<Vec3, Vec3, Iota> core, HexPattern fallback) {
		super(2, IotaMultiPredicate.any(IotaPredicate.ofType(HexIotaTypes.VEC3), IotaPredicate.ofType(HexIotaTypes.DOUBLE)));
		op = core;
		fb = Objects.requireNonNull(DoubleArithmetic.INSTANCE.getOperator(fallback));
	}

	@Override
	public Iota apply(Iterable<Iota> iotas) {
		var it = iotas.iterator();
		var left = it.next();
		var right = it.next();
		if (op != null && left instanceof Vec3Iota lh && right instanceof Vec3Iota rh) {
			return op.apply(lh.getVec3(), rh.getVec3());
		}
		var lh = left instanceof Vec3Iota l ? l.getVec3() : triplicate(downcast(left, HexIotaTypes.DOUBLE).getDouble());
		var rh = right instanceof Vec3Iota r ? r.getVec3() : triplicate(downcast(right, HexIotaTypes.DOUBLE).getDouble());
		return new Vec3Iota(new Vec3(
			downcast(fb.apply(new IterPair<>(new DoubleIota(lh.x()), new DoubleIota(rh.x()))), HexIotaTypes.DOUBLE).getDouble(),
			downcast(fb.apply(new IterPair<>(new DoubleIota(lh.y()), new DoubleIota(rh.y()))), HexIotaTypes.DOUBLE).getDouble(),
			downcast(fb.apply(new IterPair<>(new DoubleIota(lh.z()), new DoubleIota(rh.z()))), HexIotaTypes.DOUBLE).getDouble()
		));
	}

	public static Vec3 triplicate(double in) {
		return new Vec3(in, in, in);
	}
}
