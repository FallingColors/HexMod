package at.petrak.hexcasting.common.casting.arithmetic.operator.vec;


import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.Vec3Iota;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OperatorPack extends Operator {
	private OperatorPack() {
		super(3, IotaMultiPredicate.all(IotaPredicate.ofType(HexIotaTypes.DOUBLE)));
	}

	public static OperatorPack INSTANCE = new OperatorPack();

	@Override
	public @NotNull Iterable<Iota> apply(@NotNull Iterable<Iota> iotas, @NotNull CastingEnvironment env) {
		var it = iotas.iterator();
		return List.of(new Vec3Iota(new Vec3(
			downcast(it.next(), HexIotaTypes.DOUBLE).getDouble(),
			downcast(it.next(), HexIotaTypes.DOUBLE).getDouble(),
			downcast(it.next(), HexIotaTypes.DOUBLE).getDouble()
		)));
	}
}
