package at.petrak.hexcasting.api.casting.arithmetic.operator;

import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota;

public abstract class Operator {
	public final int arity;

	public final IotaMultiPredicate accepts;

	public Operator(int arity, IotaMultiPredicate accepts) {
		this.arity = arity;
		this.accepts = accepts;
	}

	public abstract Iota apply(Iterable<Iota> iotas);

	@SuppressWarnings("unchecked")
	public static <T extends Iota> T downcast(Iota iota, IotaType<T> iotaType) {
		if (iota.getType() != iotaType)
			throw new IllegalStateException("Attempting to downcast " + iota + " to type: " + iotaType);
		return (T) iota;
	}
}
