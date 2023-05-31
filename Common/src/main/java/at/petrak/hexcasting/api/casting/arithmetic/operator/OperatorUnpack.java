package at.petrak.hexcasting.api.casting.arithmetic.operator;


import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IotaPredicate;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;

import java.util.List;

import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.VEC3;

public class OperatorUnpack extends Operator {
	private OperatorUnpack() {
		super(1, IotaMultiPredicate.all(IotaPredicate.ofType(HexIotaTypes.DOUBLE)));
	}

	public static OperatorUnpack INSTANCE = new OperatorUnpack();

	@Override
	public Iterable<Iota> apply(Iterable<Iota> iotas) {
		var it = iotas.iterator();
		var vec = downcast(it.next(), VEC3).getVec3();
		return List.of(new DoubleIota(vec.x), new DoubleIota(vec.y), new DoubleIota(vec.z));
	}
}
