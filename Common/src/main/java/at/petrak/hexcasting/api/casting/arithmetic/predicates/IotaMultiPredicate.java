package at.petrak.hexcasting.api.casting.arithmetic.predicates;

import at.petrak.hexcasting.api.casting.iota.Iota;

/**
 * Used to determine whether a given set of iotas on the stack are acceptable types for
 * the operator that is storing this IotaMultiPredicate.
 */
@FunctionalInterface
public interface IotaMultiPredicate {
	boolean test(Iterable<Iota> iotas);

	/**
	 * The resulting IotaMultiPredicate only returns true if all iotas passed into test match the type dictated by child.
	 */
	static IotaMultiPredicate all(IotaPredicate child) {
		return new All(child);
	}
	/**
	 * The resulting IotaMultiPredicate returns true if two iotas are passed, the first matching first, and the second matching second.
	 */
	static IotaMultiPredicate pair(IotaPredicate first, IotaPredicate second) {
		return new Pair(first, second);
	}
	/**
	 * The resulting IotaMultiPredicate returns true if three iotas are passed, the first matching first, the second matching second, and the third matching third.
	 */
	static IotaMultiPredicate triple(IotaPredicate first, IotaPredicate second, IotaPredicate third) {
		return new Triple(first, second, third);
	}
	/**
	 * The resulting IotaMultiPredicate returns true if at least one iota passed matches needs, and the rest match fallback.
	 */
	static IotaMultiPredicate any(IotaPredicate needs, IotaPredicate fallback) {
		return new Any(needs, fallback);
	}

	/**
	 * The resulting IotaMultiPredicate returns true if either the first returns true or the second returns true.
	 */
	static IotaMultiPredicate either(IotaMultiPredicate first, IotaMultiPredicate second) {
		return new Either(first, second);
	}

	record Pair(IotaPredicate first, IotaPredicate second) implements IotaMultiPredicate {
		@Override
		public boolean test(Iterable<Iota> iotas) {
			var it = iotas.iterator();
			return it.hasNext() && first.test(it.next()) && it.hasNext() && second.test(it.next()) && !it.hasNext();
		}
	}
	record Triple(IotaPredicate first, IotaPredicate second, IotaPredicate third) implements IotaMultiPredicate {
		@Override
		public boolean test(Iterable<Iota> iotas) {
			var it = iotas.iterator();
			return it.hasNext() && first.test(it.next()) && it.hasNext() && second.test(it.next()) && it.hasNext() && third.test(it.next()) && !it.hasNext();
		}
	}
	record Any(IotaPredicate needs, IotaPredicate fallback) implements IotaMultiPredicate {
		@Override
		public boolean test(Iterable<Iota> iotas) {
			var ok = false;
			for (var iota : iotas) {
				if (needs.test(iota)) {
					ok = true;
				} else if (!fallback.test(iota)) {
					return false;
				}
			}
			return ok;
		}
	}
	record All(IotaPredicate inner) implements IotaMultiPredicate {
		@Override
		public boolean test(Iterable<Iota> iotas) {
			for (var iota : iotas) {
				if (!inner.test(iota)) {
					return false;
				}
			}
			return true;
		}
	}

	record Either(IotaMultiPredicate first, IotaMultiPredicate second) implements IotaMultiPredicate {
		@Override
		public boolean test(Iterable<Iota> iotas) {
			return first.test(iotas) || second.test(iotas);
		}
	}
}
