package at.petrak.hexcasting.api.casting.arithmetic;

import java.util.Iterator;

public record IterPair<T>(T left, T right) implements Iterable<T> {
	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			int ix;
			@Override
			public boolean hasNext() {
				return ix < 2;
			}
			@Override
			public T next() {
				switch (ix++) {
				case 0: return left;
				case 1: return right;
				}
				return null;
			}
		};
	}
}
