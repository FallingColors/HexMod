package at.petrak.hexcasting.api.casting.arithmetic.predicates;

import at.petrak.hexcasting.api.casting.iota.Iota;

@FunctionalInterface
public interface IotaMultiPredicate {
	boolean test(Iterable<Iota> iotas);

	static IotaMultiPredicate all(IotaPredicate child) {
		return new All(child);
	}
	static IotaMultiPredicate pair(IotaPredicate first, IotaPredicate second) {
		return new Pair(first, second);
	}
	static IotaMultiPredicate triple(IotaPredicate first, IotaPredicate second, IotaPredicate third) {
		return new Triple(first, second, third);
	}
	static IotaMultiPredicate any(IotaPredicate needs, IotaPredicate fallback) {
		return new Any(needs, fallback);
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
}
