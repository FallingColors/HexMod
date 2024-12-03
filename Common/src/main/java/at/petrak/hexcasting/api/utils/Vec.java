package at.petrak.hexcasting.api.utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.Map;
import java.util.Iterator;
import java.util.RandomAccess;

public class Vec<V> implements Iterable<V>, RandomAccess {

	static int hash(int key) {
		return Integer.hashCode(key);
	}

	// This is a persistent vector backed by a HAMT, see https://github.com/python/cpython/blob/main/Python/hamt.c.
	// Currently just supports push/pop/random access. Will need to update in future if this becomes more user-facing.
	sealed interface HamtNode<V> {
		HamtNode<V> assoc(int hash, V val);
		Optional<V> get(int hash);
		HamtNode<V> dissoc(int hash);
		int size();
	}

	// Array node: store children "densely" (when there are >16 children); size is the number of nonnull children
	static record ArrayNode<V>(int size, HamtNode<V>[] children) implements HamtNode<V> {
		@Override
		public HamtNode<V> assoc(int hash, V val) {
			int next = hash >>> 5;
			hash &= 0x1f;
			var child = children[hash];
			if (child != null) {
				var newChild = child.assoc(next, val);
				if (newChild == child) {
					return this;
				}
				var newChildren = Arrays.copyOf(children, children.length);
				newChildren[hash] = newChild;
				return new ArrayNode<>(size, newChildren);
			}
			var newChildren = Arrays.copyOf(children, children.length);
			newChildren[hash] = new SingleNode<>(next, val);
			return new ArrayNode<>(size + 1, newChildren);
		}
		@Override
		public Optional<V> get(int hash) {
			int next = hash >>> 5;
			var child = children[hash & 0x1f];
			return child == null ? Optional.empty() : child.get(next);
		}
		@Override
		public HamtNode<V> dissoc(int hash) {
			int next = hash >>> 5;
			hash &= 0x1f;
			var child = children[hash];
			if (child == null) {
				return this;
			}
			var newChild = child.dissoc(next);
			if (newChild == child) {
				return this;
			}
			// TODO: if nchildren = 16 && newChild == null, downgrade?
			if (size <= 16 && newChild == null) {
				int pop = 0, index = 0;
				@SuppressWarnings("unchecked")
				var newChildren = (HamtNode<V>[]) new HamtNode<?>[size - 1];

				for (int i = 0; i < children.length; i++) {
					if (i != hash && children[i] != null) {
						pop |= 1 << i;
						newChildren[index++] = children[i];
					}
				}
				assert (size - 1 == index);
				return new HamNode<>(pop, newChildren);
			}
			var newChildren = Arrays.copyOf(children, children.length);
			newChildren[hash] = newChild;
			return new ArrayNode<>(size - (newChild == null ? 1 : 0), newChildren);
		}

		@Override
		public int size() {
			int count = 0;
			for (int i = 0; i < children.length; i++) {
				if (children[i] != null) {
					count += children[i].size();
				}
			}
			return count;
		}

		@Override public String toString() { return "A[" + Arrays.toString(children) + "]"; }
	}

	// Array node: store children "sparsely" (<16 children); pop is a bitmap of the 32 children this can have
	static record HamNode<V>(int pop, HamtNode<V>[] children) implements HamtNode<V> {
		@Override
		public HamtNode<V> assoc(int hash, V val) {
			int next = hash >>> 5;
			hash &= 0x1f;
			int index = indexOf(pop, hash);
			if (hasHash(pop, hash)) {
				var child = children[index];
				var newChild = child.assoc(next, val);
				if (child == newChild) {
					return this;
				}
				var newChildren = Arrays.copyOf(children, children.length);
				newChildren[index] = newChild;
				return new HamNode<>(pop, newChildren);
			}
			if (children.length >= 15) {
				@SuppressWarnings("unchecked")
				var arrayEnts = (HamtNode<V>[]) new HamtNode<?>[32];
				int work = pop, inputPos = 0;
				while (work != 0) {
					int outputPos = Integer.numberOfTrailingZeros(work);
					work &= work - 1; // remove lowest 1
					arrayEnts[outputPos] = children[inputPos++];
				}
				arrayEnts[hash] = new SingleNode<>(next, val);
				return new ArrayNode<>(1 + inputPos, arrayEnts);
			}
			@SuppressWarnings("unchecked")
			var newChildren = (HamtNode<V>[]) new HamtNode<?>[children.length + 1];
			System.arraycopy(children, 0, newChildren, 0, index);
			System.arraycopy(children, index, newChildren, index + 1, children.length - index);
			newChildren[index] = new SingleNode<>(next, val);
			return new HamNode<>(pop | 1 << hash, newChildren);
		}
		@Override
		public Optional<V> get(int hash) {
			return hasHash(pop, hash & 0x1f) ? children[indexOf(pop, hash & 0x1f)].get(hash >>> 5) : Optional.empty();
		}
		@Override
		public HamtNode<V> dissoc(int hash) {
			int next = hash >>> 5;
			hash &= 0x1f;
			if (!hasHash(pop, hash)) {
				return this;
			}
			int index = indexOf(pop, hash);
			var child = children[index];
			var newChild = child.dissoc(next);
			if (child == newChild) {
				return this;
			}
			if (newChild != null) {
				var newChildren = Arrays.copyOf(children, children.length);
				newChildren[index] = newChild;
				return new HamNode<>(pop, newChildren);
			}
			if (children.length == 1) {
				return null;
			}
			int newPop = pop & ~(1 << hash);
			if (children.length == 2) {
				int remainingHash = Integer.numberOfTrailingZeros(newPop);
				var childNode = children[indexOf(pop, remainingHash)];
				if (childNode instanceof SingleNode<V> ln) {
					return ln.withNewHash(ln.tailHash() << 5 | remainingHash);
				}
			}
			@SuppressWarnings("unchecked")
			var newChildren = (HamtNode<V>[]) new HamtNode<?>[children.length - 1];
			System.arraycopy(children, 0, newChildren, 0, index);
			System.arraycopy(children, index + 1, newChildren, index, children.length - index - 1);
			return new HamNode<>(newPop, newChildren);
		}

		@Override
		public int size() {
			int count = 0;
			for (int i = 0; i < children.length; i++) {
				count += children[i].size();
			}
			return count;
		}

		static boolean hasHash(int pop, int hash) {
			int offset = 1 << hash;
			return (pop & offset) != 0;
		}

		static int indexOf(int pop, int hash) {
			int offset = 1 << hash;
			return Integer.bitCount(pop & (offset - 1));
		}

		@Override public String toString() { return "H[" + Integer.toString(pop, 2) + ", " + Arrays.toString(children) + "]"; }
	}

	static record SingleNode<V>(int tailHash, V value) implements HamtNode<V> {
		public SingleNode<V> withNewHash(int newHash) {
			return new SingleNode<>(newHash, value);
		}
		@Override
		public HamtNode<V> assoc(int hash, V val) {
			if (hash == tailHash) {
				return new SingleNode<>(tailHash, val);
			}
			return assocRecursive(hash, tailHash, val);
		}
		@Override
		public Optional<V> get(int hash) {
			if (tailHash == hash) {
				return Optional.of(value);
			}
			return Optional.empty();
		}
		@Override
		public HamtNode<V> dissoc(int hash) {
			if (tailHash == hash) {
				return null;
			}
			return this;
		}

		@Override public int size() { return 1; }

		private HamtNode<V> assocRecursive(int hash, int tailHash, V val) {
			int nextHash = hash >>> 5;
			int nextTailHash = tailHash >>> 5;
			hash &= 0x1f;
			tailHash &= 0x1f;
			if (hash == tailHash) {
				@SuppressWarnings("unchecked")
				var child = (HamtNode<V>[]) new HamtNode<?>[] {assocRecursive(nextHash, nextTailHash, val)};
				return new HamNode<>(1 << hash, child);
			}
			var existingNode = withNewHash(nextTailHash);
			var newNode = new SingleNode<>(nextHash, val);
			var left = hash < tailHash ? newNode : existingNode;
			var right = hash < tailHash ? existingNode : newNode;
			@SuppressWarnings("unchecked")
			var child = (HamtNode<V>[]) new HamtNode<?>[] {left, right};
			return new HamNode<>(1 << hash | 1 << tailHash, child);
		}
	}

	private HamtNode<V> root;
	public final int length;

	private Vec(HamtNode<V> root, int length) {
		this.root = root;
		this.length = length;
	}

	@SuppressWarnings("unchecked")
	public static<V> Vec<V> empty() {
		return new Vec<>(null, 0);
	}

	public Vec<V> append(V value) {
		int key = length;
		return new Vec<>(root != null ? root.assoc(Vec.hash(key), value) : new SingleNode<>(Vec.hash(key), value), length + 1);
	}

	public Vec<V> assoc(int pos, V value) {
		if (0 <= pos && pos < length) {
			return new Vec<>(root.assoc(Vec.hash(pos), value), length);
		}
		throw new IllegalArgumentException("Index " + pos + " out of bounds for vec of length " + length);
	}

	public Vec<V> pop() {
		if (isEmpty()) {
			throw new IllegalArgumentException("Can't pop from empty vec!");
		}
		return new Vec<>(root.dissoc(Vec.hash(length - 1)), length - 1);
	}

	public boolean isEmpty() {
		return length == 0;
	}

	public V get(int pos) {
		if (0 <= pos && pos < length) {
			return root.get(Vec.hash(pos)).orElseThrow(IllegalStateException::new);
		}
		throw new IllegalArgumentException("Index " + pos + " out of bounds for vec of length " + length);
	}

	public int size() {
		return length; // invariant: length == root.size()
		// return root == null ? 0 : root.size();
	}

	public Vec<V> appendAll(Iterable<V> values) {
		var out = this;
		for (var entry : values) {
			out = out.append(entry);
		}
		return out;
	}

	public static<V> Vec<V> ofIterable(Iterable<V> values) {
		return Vec.<V>empty().appendAll(values);
	}

	HamtNode<V> root() { return root; }

	// iterator over a HAMT
	@Override
	public Iterator<V> iterator() {
		return new Iterator<V>() {
			private int next = 0;
			@Override
			public boolean hasNext() {
				return next < length;
			}

			public V next() {
				return get(next++);
			}
		};
	}
}
