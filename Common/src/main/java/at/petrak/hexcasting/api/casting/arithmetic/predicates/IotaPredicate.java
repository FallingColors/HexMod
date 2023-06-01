package at.petrak.hexcasting.api.casting.arithmetic.predicates;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;

/**
 * Used to determine whether a given iota is an acceptable type for the operator that is storing this. It must be strictly a function
 * of the passed Iota's IotaType, or the caching done by ArithmeticEngine will be invalid.
 */
@FunctionalInterface
public interface IotaPredicate {
	boolean test(Iota iota);

	/**
	 * The resulting IotaPredicate returns true if the given iota matches either the left or right predicates.
	 */
	static IotaPredicate or(IotaPredicate left, IotaPredicate right) {
		return new Or(left, right);
	}

	/**
	 * The resulting IotaPredicate returns true if the given iota's type is type.
	 */
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

	/**
	 * This IotaPredicate returns true for all iotas.
	 */
	IotaPredicate TRUE = iota -> true;
}
