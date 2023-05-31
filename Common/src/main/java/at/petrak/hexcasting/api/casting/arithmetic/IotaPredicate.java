package at.petrak.hexcasting.api.casting.arithmetic;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;

@FunctionalInterface
public interface IotaPredicate {
	boolean test(Iota iota);

	static IotaPredicate or(IotaPredicate left, IotaPredicate right) {
		return new Or(left, right);
	}

	static IotaPredicate ofType(IotaType<?> type) {
		return new OfType(type);
	}

	record Or(IotaPredicate left, IotaPredicate right) implements IotaPredicate {
		@Override
		public boolean test(Iota iota) {
			return left.test(iota) || right.test(iota);
		}
	}

	record OfType(IotaType<?> type) implements IotaPredicate {
		@Override
		public boolean test(Iota iota) {
			return iota.getType().equals(this.type);
		}
	}
}
