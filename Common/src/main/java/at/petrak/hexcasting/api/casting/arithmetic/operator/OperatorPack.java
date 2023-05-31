package at.petrak.hexcasting.api.casting.arithmetic.operator;


import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.IotaPredicate;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class OperatorPack extends Operator {
	private OperatorPack() {
		super(3, IotaMultiPredicate.all(IotaPredicate.ofType(HexIotaTypes.DOUBLE)));
	}

	public static OperatorPack INSTANCE = new OperatorPack();

	@Override
	public Iterable<Iota> apply(Iterable<Iota> iotas) {
		var it = iotas.iterator();
		return List.of(new Vec3Iota(new Vec3(
			downcast(it.next(), HexIotaTypes.DOUBLE).getDouble(),
			downcast(it.next(), HexIotaTypes.DOUBLE).getDouble(),
			downcast(it.next(), HexIotaTypes.DOUBLE).getDouble()
		)));
	}
}
