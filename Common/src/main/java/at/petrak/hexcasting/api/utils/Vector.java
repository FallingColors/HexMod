package at.petrak.hexcasting.api.utils;

import com.google.common.collect.Lists;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

// TODO mark everything with use-site variance

/**
 * Ported from Scala 2.13.16's scala.collection.immutable.Vector
 */
@SuppressWarnings("unchecked")
public sealed abstract class Vector<A> extends AbstractList<A> implements RandomAccess {

    private static int copyToArray(Iterable<?> it, Object[] dst, int start) {
        if(it instanceof Collection<?> cc) {
            final int ccSize = cc.size();
            final int dstRoom = dst.length - start;
            final int toCopy = Math.min(ccSize, dstRoom);
            System.arraycopy(cc.toArray(), 0, dst, start, toCopy);
            return toCopy;
        } else {
            int i = 0;
            Iterator<?> iter = it.iterator();
            while (i + start < dst.length && iter.hasNext()) {
                dst[i + start] =  iter.next();
                i++;
            }
            return i;
        }
    }

    private static int computeSize(Iterable<?> it) {
        if(it instanceof Collection<?> cc) return cc.size();
        else {
            int i = 0;
            for (final Object o : it) {
                i++;
            }
            return i;
        }
    }

    private static int knownSize(Iterable<?> it) {
        if(it instanceof Collection<?> cc) {
            return cc.size();
        } else return Integer.MAX_VALUE;
    }

    private static int sizeCompare(Iterable<?> left, int right) {
        if(right == Integer.MAX_VALUE) return 1;
        else {
            final int known = knownSize(left);
            if(known != Integer.MAX_VALUE) return Integer.compare(known, right);
            else {
                int i;
                Iterator<?> it;
                for(i = 0, it = left.iterator(); it.hasNext(); i++, it.next()) {
                    if(i == right) return 1;
                }
                return i - right;
            }
        }
    }

    private static int sizeCompare(Iterable<?> left, Iterable<?> right) {
        final int rightKnownSize = knownSize(right);

        if(rightKnownSize != Integer.MAX_VALUE) return sizeCompare(left, rightKnownSize);
        else {
            final int leftKnownSize = knownSize(left);

            if(leftKnownSize != Integer.MAX_VALUE) {
                final int res = sizeCompare(right, leftKnownSize);
                if(res == Integer.MIN_VALUE) return 1; else return -res;
            } else {
                final Iterator<?> leftIt = left.iterator();
                final Iterator<?> rightIt = right.iterator();

                while(leftIt.hasNext() && rightIt.hasNext()) {
                    leftIt.next();
                    rightIt.next();
                }
                return Boolean.compare(leftIt.hasNext(), rightIt.hasNext());
            }
        }
    }

    private static final int BITS = 5;
    private static final int WIDTH = 1 << BITS;
    private static final int MASK = WIDTH - 1;
    private static final int BITS2 = BITS * 2;
    private static final int WIDTH2 = 1 << BITS2;
    private static final int BITS3 = BITS * 3;
    private static final int WIDTH3 = 1 << BITS3;
    private static final int BITS4 = BITS * 4;
    private static final int WIDTH4 = 1 << BITS4;
    private static final int BITS5 = BITS * 5;
    private static final int WIDTH5 = 1 << BITS5;
    private static final int LASTWIDTH = WIDTH << 1; // 1 extra bit in the last level to go up to Int.MaxValue (2^31-1) instead of 2^30:
    private static final int LOG2_CONCAT_FASTER = 5;
    private static final int ALIGN_TO_FASTER = 64;

    private static int vectorSliceDim(int count, int idx) {
        final int c = count / 2;
        return c + 1 - Math.abs(idx - c);
    }

    private static <T> T[] copyOrUse(T[] a, int start, int end) {
        if(start == 0 && end == a.length) return a; else return copyOfRange(a, start, end);
    }

    private static <T> T[] copyTail(T[] a) {
        return copyOfRange(a, 1, a.length);
    }

    private static <T> T[] copyInit(T[] a) {
        return copyOfRange(a, 0, a.length - 1);
    }

    private static <T> T[] copyIfDifferentSize(T[] a, int len) {
        if(a.length == len) return a; else return copyOf(a, len);
    }

    private static Object[] wrap1(Object x) {
        Object[] arr = new Object[1];
        arr[0] = x;
        return arr;
    }

    private static Object[][] wrap2(Object[] x) {
        Object[][] arr = new Object[1][];
        arr[0] = x;
        return arr;
    }

    private static Object[][][] wrap3(Object[][] x) {
        Object[][][] arr = new Object[1][][];
        arr[0] = x;
        return arr;
    }

    private static Object[][][][] wrap4(Object[][][] x) {
        Object[][][][] arr = new Object[1][][][];
        arr[0] = x;
        return arr;
    }

    private static Object[][][][][] wrap5(Object[][][][] x) {
        Object[][][][][] arr = new Object[1][][][][];
        arr[0] = x;
        return arr;
    }

    private static Object[] copyUpdate(Object[] a1, int idx1, Object elem) {
        final Object[] a1c = a1.clone();
        a1c[idx1] = elem;
        return a1c;
    }

    private static Object[][] copyUpdate(Object[][] a2, int idx2, int idx1, Object elem) {
        final Object[][] a2c = a2.clone();
        a2c[idx2] = copyUpdate(a2c[idx2], idx1, elem);
        return a2c;
    }

    private static Object[][][] copyUpdate(Object[][][] a3, int idx3, int idx2, int idx1, Object elem) {
        final Object[][][] a3c = a3.clone();
        a3c[idx3] = copyUpdate(a3c[idx3], idx2, idx1, elem);
        return a3c;
    }

    private static Object[][][][] copyUpdate(Object[][][][] a4, int idx4, int idx3, int idx2, int idx1, Object elem) {
        final Object[][][][] a4c = a4.clone();
        a4c[idx4] = copyUpdate(a4c[idx4], idx3, idx2, idx1, elem);
        return a4c;
    }

    private static Object[][][][][] copyUpdate(Object[][][][][] a5, int idx5, int idx4, int idx3, int idx2, int idx1, Object elem) {
        final Object[][][][][] a5c = a5.clone();
        a5c[idx5] = copyUpdate(a5c[idx5], idx4, idx3, idx2, idx1, elem);
        return a5c;
    }

    private static Object[][][][][][] copyUpdate(Object[][][][][][] a6, int idx6, int idx5, int idx4, int idx3, int idx2, int idx1, Object elem) {
        final Object[][][][][][] a6c = a6.clone();
        a6c[idx6] = copyUpdate(a6c[idx6], idx5, idx4, idx3, idx2, idx1, elem);
        return a6c;
    }

    private static <T> T[] concatArrays(T[] a, T[] b) {
        T[] dest = copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, dest, a.length, b.length);
        return dest;
    }

    private static Object[] copyAppend1(Object[] a, Object elem) {
        final int alen = a.length;
        final Object[] ac = new Object[alen + 1];
        System.arraycopy(a, 0, ac, 0, alen);
        ac[alen] = elem;
        return ac;
    }

    private static <T> T[] copyAppend(T[] a, T elem) {
        T[] ac = copyOf(a, a.length + 1);
        ac[ac.length - 1] = elem;
        return ac;
    }

    private static Object[] copyPrepend1(Object elem, Object[] a) {
        final Object[] ac = new Object[a.length + 1];
        System.arraycopy(a, 0, ac, 1, a.length);
        ac[0] = elem;
        return ac;
    }

    private static <T> T[] copyPrepend(T elem, T[] a) {
        T[] ac = (T[]) Array.newInstance(a.getClass().getComponentType(), a.length + 1);
        System.arraycopy(a, 0, ac, 1, a.length);
        ac[0] = elem;
        return ac;
    }

    private static final Object[] empty1 = new Object[0];
    private static final Object[][] empty2 = new Object[0][];
    private static final Object[][][] empty3 = new Object[0][][];
    private static final Object[][][][] empty4 = new Object[0][][][];
    private static final Object[][][][][] empty5 = new Object[0][][][][];
    private static final Object[][][][][][] empty6 = new Object[0][][][][][];

    private static <T, A> void foreachRec(int level, T[] a, Consumer<A> f) {
        int i = 0;
        final int len = a.length;
        if(level == 0) {
            while(i < len) {
                f.accept((A) a[i]);
                i++;
            }
        } else {
            final int l = level - 1;
            while(i < len) {
                foreachRec(l, (Object[]) a[i], f);
                i++;
            }
        }
    }

    private static <A, B> Object[] mapElems1(Object[] a, Function<A, B> f) {
        for(int i = 0; i < a.length; i++) {
            final Object v1 = a[i];
            final Object v2 = f.apply((A) v1);
            if(v1 != v2) return mapElems1Rest(a, f, i, v2);
        }
        return a;
    }

    private static <A, B> Object[] mapElems1Rest(Object[] a, Function<A, B> f, int at, Object v2) {
        Object[] ac = new Object[a.length];
        if(at > 0) System.arraycopy(a, 0, ac, 0, at);
        ac[at] = v2;
        for(int i = at + 1; i < a.length; i++) {
            ac[i] = f.apply((A) a[i]);
        }
        return ac;
    }

    private static <A, B, T> T[] mapElems(int n, T[] a, Function<A, B> f) {
        if(n == 1) return (T[]) mapElems1(a, f);
        else {
            for(int i = 0; i < a.length; i++) {
                final T v1 = a[i];
                final Object[] v2 = mapElems(n - 1, (Object[]) v1, f);
                if(v1 != v2) return mapElemsRest(n, a, f, i, v2);
            }
            return a;
        }
    }

    private static <A, B, T> T[] mapElemsRest(int n, T[] a, Function<A, B> f, int at, Object v2) {
        final Object[] ac = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length);
        if(at > 0) System.arraycopy(a, 0, ac, 0, at);
        ac[at] = v2;
        for(int i = at + 1; i < a.length; i++) {
            ac[i] = mapElems(n - 1, (Object[]) a[i], f);
        }
        return (T[]) ac;
    }

    private static Object[] prepend1IfSpace(Object[] prefix1, Iterable<?> it) {
        if(sizeCompare(it, WIDTH - prefix1.length) <= 0) {
            final int s = computeSize(it);
            if(s == 0) return null;
            else if(s == 1) return copyPrepend(it.iterator().next(), prefix1);
            else {
                final Object[] prefix1b = new Object[prefix1.length + s];
                System.arraycopy(prefix1, 0, prefix1b, s, prefix1.length);
                copyToArray(it, prefix1b, 0);
                return prefix1b;
            }
        } else return null;
    }

    private static Object[] append1IfSpace(Object[] suffix1, Iterable<?> it) {
        if(sizeCompare(it, WIDTH - suffix1.length) <= 0) {
            final int s = computeSize(it);
            if(s == 0) return null;
            else if(s == 1) return copyAppend(suffix1, it.iterator().next());
            else {
                final Object[] suffix1b = copyOf(suffix1, suffix1.length + s);
                copyToArray(it, suffix1b, suffix1.length);
                return suffix1b;
            }
        } else return null;
    }

    final Object[] prefix1;

    Vector(Object[] prefix1) {
        this.prefix1 = prefix1;
    }

    protected final IndexOutOfBoundsException ioob(int index) {
        return new IndexOutOfBoundsException("%d is out of bounds (min 0, max %d)".formatted(index, this.length() - 1));
    }

    public int length() {
        return this.prefix1.length;
    }

    public ListIterator<A> iterator() {
        return new NewVectorIterator<>(this, this.length(), this.vectorSliceCount());
    }

    protected Vector<A> filterImpl(Predicate<A> predicate, boolean isFlipped) {
        int i = 0;
        final int len = this.prefix1.length;
        while (i != len) {
            if(predicate.test((A) this.prefix1[i]) == isFlipped) {
                // each 1 bit indicates that index passes the filter.
                // all indices < i are also assumed to pass the filter
                int bitmap = 0;
                int j = i + 1;
                while (j < len) {
                    if(predicate.test((A) this.prefix1[j]) != isFlipped) {
                        bitmap |= (1 << j);
                    }
                    j += 1;
                }
                final int newLen = Integer.bitCount(bitmap);

                if(newLen == 0) return Vector0.getInstance();
                else {
                    final Object[] newData = new Object[newLen];
                    System.arraycopy(prefix1, 0, newData, 0, i);
                    int k = i + 1;
                    while (i != newLen) {
                        if (((1 << k) & bitmap) != 0) {
                            newData[i] = this.prefix1[k];
                            i += 1;
                        }
                        k += 1;
                    }
                    return new Vector1<A>(newData);
                }
            }

            i++;
        }

        return this;
    }

    public abstract Vector<A> updated(int index, A elem);

    public abstract Vector<A> appended(A elem);

    public abstract Vector<A> prepended(A elem);

    public Vector<A> prependedAll(Iterable<A> prefix) {
        int k = knownSize(prefix);
        if(k == 0) return this;
        else if (k >= Integer.MAX_VALUE) {
            VectorBuilder<A> builder = new VectorBuilder<A>();
            builder.addAll(prefix);
            builder.addAll(this);
            return builder.result();
        } else return this.prependedAll0(prefix, k);
    }

    public Vector<A> appendedAll(Iterable<A> suffix) {
        int k = knownSize(suffix);
        if(k == 0) return this;
        else if (k >= Integer.MAX_VALUE) {
            VectorBuilder<A> builder = new VectorBuilder<A>();
            builder.addAll(this);
            builder.addAll(suffix);
            return builder.result();
        } else return this.appendedAll0(suffix, k);
    }

    protected Vector<A> prependedAll0(Iterable<A> prefix, int k) {
        // k >= 0, k = prefix.knownSize
        final int tinyAppendLimit = 4 + this.vectorSliceCount();
        if (k < tinyAppendLimit /*|| k < (this.size >>> Log2ConcatFaster)*/) {
            Vector<A> v = this;
            Object[] elements = new Object[k];
            Iterator<A> it = prefix.iterator();
            for(int i = elements.length - 1; i >= 0; i--) {
                elements[i] = it.next();
            }
            for(Object elem : elements) {
                v = v.prepended((A) elem);
            }
            return v;
        } else if (this.size() < (k >>> LOG2_CONCAT_FASTER) && prefix instanceof Vector<?>) {
            Vector<A> v = (Vector<A>) prefix;
            for (A a : this) v = v.appended(a);
            return v;
        } else if (k < this.size() - ALIGN_TO_FASTER) {
            return new VectorBuilder<A>().alignTo(k, this).addAll(prefix).addAll(this).result();
        } else {
            VectorBuilder<A> builder = new VectorBuilder<A>();
            builder.addAll(prefix);
            builder.addAll(this);
            return builder.result();
        }
    }

    protected Vector<A> appendedAll0(Iterable<A> suffix, int k) {
        // k >= 0, k = suffix.knownSize
        final int tinyAppendLimit = 4 + this.vectorSliceCount();
        if (k < tinyAppendLimit) {
            Vector<A> v = this;
            for(A a : suffix) v = v.appended(a);
            return v;
        } else if (this.size() < (k >>> LOG2_CONCAT_FASTER) && suffix instanceof Vector<?>) {
            Vector<A> v = (Vector<A>) suffix;
            for (A a : this.reversed()) v = v.prepended(a);
            return v;
        } else if (this.size() < k - ALIGN_TO_FASTER && suffix instanceof Vector<?>) {
            Vector<A> v = (Vector<A>) suffix;
            return new VectorBuilder<A>().alignTo(this.size(), v).addAll(this).addAll(v).result();
        } else return new VectorBuilder<A>().initFrom(this).addAll(suffix).result();
    }

    public final Vector<A> take(int n) {
        return this.slice(0, n);
    }

    public final Vector<A> drop(int n) {
        return this.slice(n, this.length());
    }

    public final Vector<A> takeRight(int n) {
        return this.slice(this.length() - Math.max(n, 0), this.length());
    }

    public final Vector<A> dropRight(int n) {
        return this.slice(0, this.length() - Math.max(n, 0));
    }

    public Vector<A> tail() {
        return this.slice(1, this.length());
    }

    public Vector<A> init() {
        return this.slice(0, this.length() - 1);
    }

    /** Like slice but parameters must be 0 <= lo < hi < length */
    protected abstract Vector<A> slice0(int lo, int hi);

    /** Number of slices */
    abstract int vectorSliceCount();
    /** Slice at index */
    abstract Object[] vectorSlice(int idx);
    /** Length of all slices up to and including index */
    abstract int vectorSlicePrefixLength(int idx);

    public final A head() {
        if(this.prefix1.length == 0) throw new NoSuchElementException("empty.head");
        else return (A) this.prefix1[0];
    }

    public A last() {
        return (A) this.prefix1[this.prefix1.length - 1];
    }

    public void foreach(Consumer<A> f) {
        final int c = this.vectorSliceCount();
        for(int i = 0; i < c; i++) {
            foreachRec(vectorSliceDim(c, i) - 1, this.vectorSlice(i), f);
        }
    }

    public final Vector<A> slice(int from, int until) {
        final int lo = Math.max(from, 0);
        final int hi = Math.min(until, this.length());
        if(hi <= lo) return Vector0.getInstance();
        else if(hi - lo == this.length()) return this;
        else return this.slice0(lo, hi);
    }

    public boolean contains(Object elem) {
        return this.exists(elem::equals);
    }

    public int copyToArray(A[] arr, int start, int len) {
        int i;
        ListIterator<A> li;
        for(i = 0, li = this.iterator(); i < len && (i + start) < arr.length && li.hasNext(); i++) {
            arr[start + i] = li.next();
        }
        return i;
    }

    public final int copyToArray(A[] arr, int start) {
        return this.copyToArray(arr, start, Integer.MAX_VALUE);
    }

    public final int copyToArray(A[] arr) {
        return this.copyToArray(arr, 0, Integer.MAX_VALUE);
    }

    public static <A> Vector<A> empty() {
        return Vector0.getInstance();
    }

    public static <A> Vector<A> from(Iterable<A> it) {
        if(it instanceof Vector<A> v) return v;

        final int knownSize = knownSize(it);
        if(knownSize == 0) return empty();
        else if(knownSize > 0 && knownSize <= WIDTH) {
            Object[] a1;
            if(it instanceof Collection<A> c) a1 = c.toArray();
            else {
                a1 = new Object[knownSize];
                copyToArray(it, a1, 0);
            }
            return new Vector1<>(a1);
        } else {
            VectorBuilder<A> builder = new VectorBuilder<>();
            builder.addAll(it);
            return builder.result();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) return true;
        else if(obj instanceof List<?> la) {
            return this.sameElements((List<A>) la);
        } else return false;
    }

    public boolean sameElements(Iterable<A> iterable) {
        final int thisKnownSize = this.knownSize();
        final boolean knownSizeDifference;
        if(thisKnownSize != Integer.MAX_VALUE) {
            final int thatKnownSize = knownSize(iterable);
            knownSizeDifference = thatKnownSize != Integer.MAX_VALUE && thisKnownSize != thatKnownSize;
        } else knownSizeDifference = false;
        if(knownSizeDifference) return false;
        Iterator<A> left = this.iterator();
        Iterator<A> right = iterable.iterator();
        while (left.hasNext() && right.hasNext()) {
            if(!left.next().equals(right.next())) return false;
        }
        return left.hasNext() == right.hasNext();
    }

    public boolean exists(Predicate<A> p) {
        boolean res = false;
        final Iterator<A> it = this.iterator();
        while (!res && it.hasNext()) { res = p.test(it.next()); }
        return res;
    }

    public Vector<A> filter(Predicate<A> pred) {
        return this.filterImpl(pred, false);
    }

    public <B> Vector<B> flatMap(Function<A, Iterable<B>> func) {
        VectorBuilder<B> builder = new VectorBuilder<B>();
        for(A a : this) {
            builder.addAll(func.apply(a));
        }
        return builder.result();
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for(A a : this) {
            hashCode *= 31;
            if(a != null) hashCode += a.hashCode();
        }
        return hashCode;
    }

    public boolean isEmpty() {
        return this.length() == 0;
    }

    public int knownSize() {
        return this.length();
    }

    public abstract <B> Vector<B> map(Function<A, B> f);

    public Vector<A> reversed() {
        // FIXME: Understand NewVectorIterator
        VectorBuilder<A> builder = new VectorBuilder<>();
        for(int i = this.size() - 1; i >= 0; i--) {
            builder.addOne(this.get(i));
        }
        return builder.result();
    }

    public final int size() {
        return this.length();
    }

    public A[] toArray(Class<A> clazz) {
        final A[] destination = (A[]) Array.newInstance(clazz, this.knownSize());
        copyToArray(destination, 0);
        return destination;
    }

    public abstract A get(int i);

    private static sealed abstract class BigVector<A> extends Vector<A> {
        final Object[] suffix1;
        final int length0;

        protected BigVector(Object[] prefix1, Object[] suffix1, int length0) {
            super(prefix1);
            this.suffix1 = suffix1;
            this.length0 = length0;
        }

        @Override
        public int length() {
            return this.length0;
        }

        @Override
        protected Vector<A> filterImpl(Predicate<A> predicate, boolean isFlipped) {
            int i = 0;
            final int len = this.prefix1.length;
            while (i != len) {
                if(predicate.test((A) this.prefix1[i]) == isFlipped) {
                    int bitmap = 0;
                    int j = i + 1;
                    while(j < len) {
                        if(predicate.test((A) this.prefix1[j]) != isFlipped) {
                            bitmap |= (1 << j);
                        }
                        j += 1;
                    }
                    final int newLen = i + Integer.bitCount(bitmap);

                    VectorBuilder<A> b = new VectorBuilder<>();
                    int k = 0;
                    while (k < i) {
                        b.addOne((A) this.prefix1[k]);
                        k += 1;
                    }
                    k = i + 1;
                    while(i != newLen) {
                        if(((1 << k) & bitmap) != 0) {
                            b.addOne((A) this.prefix1[k]);
                            i += 1;
                        }
                        k += 1;
                    }
                    this.foreachRest(v -> {
                        if(predicate.test(v) != isFlipped) b.addOne(v);
                    });
                    return b.result();
                }
                i += 1;
            }
            VectorBuilder<A> b = new VectorBuilder<>();
            b.initFrom(this.prefix1);
            this.foreachRest(v -> {
                if(predicate.test(v) != isFlipped) b.addOne(v);
            });
            return b.result();
        }

        protected final void foreachRest(Consumer<A> f) {
            final int c = this.vectorSliceCount();
            for(int i = 1; i < c; i++) {
                foreachRec(vectorSliceDim(c, i)-1, vectorSlice(i), f);
            }
        }

        @Override
        public A last() {
            final Object[] suffix = this.suffix1;
            if(suffix.length == 0) throw new NoSuchElementException("empty.tail");
            else return (A) suffix[suffix.length - 1];
        }
    }

    private static final class Vector0 extends BigVector<Object> {
        private static final Vector0 INSTANCE = new Vector0();

        public static <A> Vector<A> getInstance() { return (Vector<A>) INSTANCE; }

        private Vector0() {
            super(empty1, empty1, 0);
        }

        public ListIterator<Object> iterator() {
            return Collections.emptyListIterator();
        }

        @Override
        public Void get(int i) {
            throw ioob(i);
        }

        @Override
        public Vector<Object> updated(int index, Object elem) {
            throw ioob(index);
        }

        @Override
        public Vector<Object> appended(Object elem) {
            return new Vector1<>(wrap1(elem));
        }

        @Override
        public Vector<Object> prepended(Object elem) {
            return new Vector1<>(wrap1(elem));
        }

        @Override
        public <B> Vector<B> map(Function<Object, B> f) {
            return (Vector<B>) this;
        }

        @Override
        public Vector<Object> tail() {
            throw new UnsupportedOperationException("empty.tail");
        }

        @Override
        public Vector<Object> init() {
            throw new UnsupportedOperationException("empty.init");
        }

        @Override
        protected Vector<Object> slice0(int lo, int hi) {
            return this;
        }

        @Override
        int vectorSliceCount() {
            return 0;
        }

        @Override
        Object[] vectorSlice(int idx) {
            return null;
        }

        @Override
        int vectorSlicePrefixLength(int idx) {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj) return true;
            else if(obj instanceof Vector<?>) return false;
            else return super.equals(obj);
        }

        @Override
        protected Vector<Object> prependedAll0(Iterable<Object> prefix, int k) {
            return Vector.from(prefix);
        }

        @Override
        protected Vector<Object> appendedAll0(Iterable<Object> suffix, int k) {
            return Vector.from(suffix);
        }
    }

    private static final class Vector1<A> extends Vector<A> {
        Vector1(Object[] data1) {
            super(data1);
        }

        @Override
        public A get(int index) {
            if(index >= 0 && index < this.prefix1.length)
                return (A) this.prefix1[index];
            else throw ioob(index);
        }

        @Override
        public Vector<A> updated(int index, A elem) {
            if(index >= 0 && index < this.prefix1.length)
                return new Vector1<>(copyUpdate(this.prefix1, index, elem));
            else throw ioob(index);
        }

        @Override
        public Vector<A> appended(A elem) {
            final int len1 = this.prefix1.length;
            if(len1 < WIDTH) return new Vector1<>(copyAppend1(this.prefix1, elem));
            else return new Vector2<>(this.prefix1, WIDTH, empty2, wrap1(elem), WIDTH + 1);
        }

        @Override
        public Vector<A> prepended(A elem) {
            final int len1 = this.prefix1.length;
            if(len1 < WIDTH) return new Vector1<>(copyPrepend1(elem, this.prefix1));
            else return new Vector2<>(wrap1(elem), 1, empty2, this.prefix1, len1 + 1);
        }

        @Override
        public <B> Vector<B> map(Function<A, B> f) {
            return new Vector1<>(mapElems1(this.prefix1, f));
        }

        @Override
        protected Vector<A> slice0(int lo, int hi) {
            return new Vector1<>(copyOfRange(this.prefix1, lo, hi));
        }

        @Override
        public Vector<A> tail() {
            if(this.prefix1.length == 1) return Vector0.getInstance();
            else return new Vector1<>(copyTail(this.prefix1));
        }

        @Override
        public Vector<A> init() {
            if(this.prefix1.length == 1) return Vector0.getInstance();
            else return new Vector1<>(copyInit(this.prefix1));
        }

        @Override
        int vectorSliceCount() {
            return 1;
        }

        @Override
        Object[] vectorSlice(int idx) {
            return this.prefix1;
        }

        @Override
        int vectorSlicePrefixLength(int idx) {
            return this.prefix1.length;
        }

        @Override
        protected Vector<A> prependedAll0(Iterable<A> prefix, int k) {
            final Object[] data1b = prepend1IfSpace(this.prefix1, prefix);
            if(data1b == null) return super.prependedAll0(prefix, k);
            else return new Vector1<>(data1b);
        }

        @Override
        protected Vector<A> appendedAll0(Iterable<A> suffix, int k) {
            final Object[] data1b = append1IfSpace(this.prefix1, suffix);
            if(data1b != null) return new Vector1<>(data1b);
            else return super.appendedAll0(suffix, k);
        }
    }

    private static final class Vector2<A> extends BigVector<A> {
        final int len1;
        final Object[][] data2;

        Vector2(Object[] prefix1, int len1, Object[][] data2, Object[] suffix1, int length0) {
            super(prefix1, suffix1, length0);
            this.len1 = len1;
            this.data2 = data2;
        }

        @Override
        public A get(int i) {
            if(i >= 0 && i < this.length0) {
                final int io = i - this.len1;
                if(io >= 0) {
                    final int i2 = io >>> BITS;
                    final int i1 = io & MASK;
                    if(i2 < data2.length) return (A) this.data2[i2][i1];
                    else return (A) suffix1[io & MASK];
                } else return (A) this.prefix1[i];
            } else throw ioob(i);
        }

        @Override
        public Vector<A> updated(int index, A elem) {
            if(index >= 0 && index < this.length0) {
                if(index >= this.len1) {
                    final int io = index - this.len1;
                    final int i2 = io >>> BITS;
                    final int i1 = io & MASK;
                    if(i2 < data2.length) return new Vector2<>(
                            this.prefix1, this.len1,
                            copyUpdate(this.data2, i2, i1, elem),
                            this.suffix1, this.length0
                    ); else return new Vector2<>(
                            this.prefix1, this.len1, this.data2,
                            copyUpdate(this.suffix1, i1, elem),
                            this.length0
                    );
                } else {
                    return new Vector2<>(
                            copyUpdate(this.prefix1, index, elem),
                            this.len1, this.data2, this.suffix1, this.length0
                    );
                }
            } else throw ioob(index);
        }

        @Override
        public Vector<A> appended(A elem) {
            if (suffix1.length < WIDTH) return new Vector2<>(
                    this.prefix1, this.len1, this.data2,
                    copyAppend1(this.suffix1, elem), this.length0 + 1
            ); else if(data2.length < WIDTH - 2) return new Vector2<>(
                    this.prefix1, this.len1,
                    copyAppend(this.data2, this.suffix1), wrap1(elem), this.length0 + 1
            ); else return new Vector3<>(
                    this.prefix1, this.len1, this.data2, WIDTH * (WIDTH - 2) + this.len1,
                    empty3, wrap2(this.suffix1), wrap1(elem), this.length0 + 1
            );
        }

        @Override
        public Vector<A> prepended(A elem) {
            if (this.len1 < WIDTH) return new Vector2<>(
                    copyPrepend1(elem, this.prefix1), this.len1 + 1,
                    this.data2, this.suffix1,
                    this.length0 + 1
            ); else if(data2.length < WIDTH - 2) return new Vector2<>(
                    wrap1(elem), 1, copyPrepend(this.prefix1, this.data2),
                    this.suffix1,
                    this.length0 + 1
            ); else return new Vector3<>(
                    wrap1(elem), 1, wrap2(this.prefix1), this.len1 + 1,
                    empty3, this.data2, this.suffix1, this.length0 + 1
            );
        }

        @Override
        public <B> Vector<B> map(Function<A, B> f) {
            return new Vector2<>(
                    mapElems1(this.prefix1, f), this.len1,
                    mapElems(2, this.data2, f),
                    mapElems1(this.suffix1, f), this.length0
            );
        }

        @Override
        protected Vector<A> slice0(int lo, int hi) {
            final VectorSliceBuilder b = new VectorSliceBuilder(lo, hi);
            b.consider(1, this.prefix1);
            b.consider(2, this.data2);
            b.consider(1, this.suffix1);
            return b.result();
        }

        @Override
        public Vector<A> tail() {
            if(this.len1 > 1) return new Vector2<>(
                    copyTail(this.prefix1), this.len1 - 1,
                    this.data2, this.suffix1,
                    this.length0 - 1
            ); else return this.slice0(1, this.length0);
        }

        @Override
        public Vector<A> init() {
            if(this.suffix1.length > 1) return new Vector2<>(
                    this.prefix1, this.len1, this.data2,
                    copyInit(this.suffix1), this.length0 - 1
            ); else return this.slice0(0, this.length0 - 1);
        }

        @Override
        int vectorSliceCount() {
            return 3;
        }

        @Override
        Object[] vectorSlice(int idx) {
            return switch(idx) {
                case 0 -> this.prefix1;
                case 1 -> this.data2;
                case 2 -> this.suffix1;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        int vectorSlicePrefixLength(int idx) {
            return switch(idx) {
                case 0 -> this.len1;
                case 1 -> this.length0 - this.suffix1.length;
                case 2 -> this.length0;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        protected Vector<A> prependedAll0(Iterable<A> prefix, int k) {
            final Object[] prefix1b = prepend1IfSpace(this.prefix1, prefix);
            if(prefix1b == null) return super.prependedAll0(prefix, k);
            else {
                final int diff = prefix1b.length - prefix1.length;
                return new Vector2<>(
                        prefix1b, this.len1 + diff,
                        this.data2, this.suffix1,
                        this.length0 + diff
                );
            }
        }

        @Override
        protected Vector<A> appendedAll0(Iterable<A> suffix, int k) {
            final Object[] suffix1b = append1IfSpace(this.suffix1, suffix);
            if(suffix1b != null) return new Vector2<>(
                    this.prefix1, this.len1, this.data2,
                    suffix1b, this.length0 - suffix1.length + suffix1b.length
            ); else return super.appendedAll0(suffix, k);
        }
    }

    private static final class Vector3<A> extends BigVector<A> {

        final int len1;
        final Object[][] prefix2;
        final int len12;
        final Object[][][] data3;
        final Object[][] suffix2;

        Vector3(
                Object[] prefix1, int len1,
                Object[][] prefix2, int len12,
                Object[][][] data3, Object[][] suffix2, Object[] suffix1,
                int length0
        ) {
            super(prefix1, suffix1, length0);
            this.len1 = len1;
            this.prefix2 = prefix2;
            this.len12 = len12;
            this.data3 = data3;
            this.suffix2 = suffix2;
        }

        @Override
        public A get(int i) {
            if(i >= 0 && i < this.length0) {
                final int io = i - this.len12;
                if(io >= 0) {
                    final int i3 = io >>> BITS2;
                    final int i2 = (io >>> BITS) & MASK;
                    final int i1 = io & MASK;
                    if(i3 < this.data3.length) return (A) this.data3[i3][i2][i1];
                    else if(i2 < this.suffix2.length) return (A) this.suffix2[i2][i1];
                    else return (A) this.suffix1[i1];
                } else if(i >= this.len1) {
                    final int io_ = i - this.len1;
                    return (A) prefix2[io_ >>> BITS][io_ & MASK];
                } else return (A) this.prefix1[i];
            } else throw ioob(i);
        }

        @Override
        public Vector<A> updated(int index, A elem) {
            if(index >= 0 && index < this.length0) {
                if(index >= this.len12) {
                    final int io = index - this.len12;
                    final int i3 = io >>> BITS2;
                    final int i2 = (io >>> BITS) & MASK;
                    final int i1 = io & MASK;
                    if(i3 < this.data3.length) return new Vector3<>(
                            this.prefix1, this.len1, this.prefix2, this.len12,
                            copyUpdate(this.data3, i3, i2, i1, elem),
                            this.suffix2, this.suffix1, this.length0
                    ); else if(i2 < this.suffix2.length) return new Vector3<>(
                            this.prefix1, this.len1, this.prefix2, this.len12, this.data3,
                            copyUpdate(this.suffix2, i2, i1, elem),
                            this.suffix1, this.length0
                    ); else return new Vector3<>(
                            this.prefix1, this.len1, this.prefix2, this.len12, this.data3, this.suffix2,
                            copyUpdate(this.suffix1, i1, elem),
                            this.length0
                    );
                } else if(index >= this.len1) {
                    final int io = index - this.len1;
                    return new Vector3<>(
                            this.prefix1, this.len1,
                            copyUpdate(prefix2, io >>> BITS, io & MASK, elem),
                            this.len12, this.data3, this.suffix2, this.suffix1, this.length0
                    );
                } else {
                    return new Vector3<>(
                            copyUpdate(this.prefix1, index, elem),
                            this.len1, this.prefix2, this.len12, this.data3, this.suffix2, this.suffix1, this.length0
                    );
                }
            } else throw ioob(index);
        }

        @Override
        public Vector<A> appended(A elem) {
            if(this.suffix1.length < WIDTH) return new Vector3<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.data3, this.suffix2,
                    copyAppend1(this.suffix1, elem), this.length0 + 1
            ); else if(this.suffix2.length < WIDTH - 1) return new Vector3<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.data3,
                    copyAppend(this.suffix2, this.suffix1), wrap1(elem),  this.length0 + 1
            ); else if(this.data3.length < WIDTH - 2) return new Vector3<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    copyAppend(this.data3, copyAppend(this.suffix2, this.suffix1)), empty2, wrap1(elem), this.length0 + 1
            ); else return new Vector4<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.data3, (WIDTH - 2) * WIDTH2 + this.len12,
                    empty4, wrap3(copyAppend(this.suffix2, this.suffix1)), empty2, wrap1(elem), this.length0 + 1
            );
        }

        @Override
        public Vector<A> prepended(A elem) {
            if(this.len1 < WIDTH) return new Vector3<>(
                    copyPrepend1(elem, this.prefix1), this.len1 + 1,
                    this.prefix2,
                    this.len12 + 1,
                    this.data3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len12 < WIDTH2) return new Vector3<>(
                    wrap1(elem), 1, copyPrepend(this.prefix1, this.prefix2), this.len12 + 1,
                    this.data3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.data3.length < WIDTH - 2) return new Vector3<>(
                    wrap1(elem), 1, empty2, 1, copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.data3),
                    this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else return new Vector4<>(
                    wrap1(elem), 1, empty2, 1, wrap3(copyPrepend(this.prefix1, this.prefix2)), this.len12 + 1,
                    empty4, this.data3, this.suffix2, this.suffix1, this.length0 + 1
            );
        }

        @Override
        public <B> Vector<B> map(Function<A, B> f) {
            return new Vector3<>(
                    mapElems1(this.prefix1, f), this.len1,
                    mapElems(2, this.prefix2, f), this.len12,
                    mapElems(3, this.data3, f),
                    mapElems(2, this.suffix2, f),
                    mapElems1(this.suffix1, f),
                    this.length0
            );
        }

        @Override
        protected Vector<A> slice0(int lo, int hi) {
            final VectorSliceBuilder b = new VectorSliceBuilder(lo, hi);
            b.consider(1, this.prefix1);
            b.consider(2, this.prefix2);
            b.consider(3, this.data3);
            b.consider(2, this.suffix2);
            b.consider(1, this.suffix1);
            return b.result();
        }

        @Override
        public Vector<A> tail() {
            if(this.len1 > 1) return new Vector3<>(
                    copyTail(this.prefix1), this.len1 - 1,
                    this.prefix2, this.len12 - 1,
                    this.data3, this.suffix2, this.suffix1,
                    this.length0 - 1
            ); else return this.slice0(1, this.length0);
        }

        @Override
        public Vector<A> init() {
            if(this.suffix1.length > 1) return new Vector3<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.data3, this.suffix2,
                    copyInit(this.suffix1), this.length0 - 1
            ); else return this.slice0(0, this.length0 - 1);
        }

        @Override
        int vectorSliceCount() {
            return 5;
        }

        @Override
        Object[] vectorSlice(int idx) {
            return switch(idx) {
                case 0 -> this.prefix1;
                case 1 -> this.prefix2;
                case 2 -> this.data3;
                case 3 -> this.suffix2;
                case 4 -> this.suffix1;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        int vectorSlicePrefixLength(int idx) {
            return switch (idx) {
                case 0 -> this.len1;
                case 1 -> this.len12;
                case 2 -> this.len12 + this.data3.length * WIDTH2;
                case 3 -> this.length0 - this.suffix1.length;
                case 4 -> this.length0;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        protected Vector<A> prependedAll0(Iterable<A> prefix, int k) {
            final Object[] prefix1b = prepend1IfSpace(this.prefix1, prefix);
            if(prefix1b == null) return super.prependedAll0(prefix, k);
            else {
                final int diff = prefix1b.length - this.prefix1.length;
                return new Vector3<>(
                        prefix1b, this.len1 + diff,
                        this.prefix2, this.len12 + diff,
                        this.data3, this.suffix2, this.suffix1,
                        this.length0 + diff
                );
            }
        }

        @Override
        protected Vector<A> appendedAll0(Iterable<A> suffix, int k) {
            final Object[] suffix1b = prepend1IfSpace(this.suffix1, suffix);
            if(suffix1b != null) return new Vector3<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.data3, this.suffix2,
                    suffix1b, this.length0 - this.suffix1.length + suffix1b.length
            ); else return super.appendedAll0(suffix, k);
        }
    }

    private static final class Vector4<A> extends BigVector<A> {

        final int len1;
        final Object[][] prefix2;
        final int len12;
        final Object[][][] prefix3;
        final int len123;
        final Object[][][][] data4;
        final Object[][][] suffix3;
        final Object[][] suffix2;

        Vector4(
                Object[] prefix1, int len1,
                Object[][] prefix2, int len12,
                Object[][][] prefix3, int len123,
                Object[][][][] data4,
                Object[][][] suffix3, Object[][] suffix2, Object[] suffix1,
                int length0
        ) {
            super(prefix1, suffix1, length0);
            this.len1 = len1;
            this.prefix2 = prefix2;
            this.len12 = len12;
            this.prefix3 = prefix3;
            this.len123 = len123;
            this.data4 = data4;
            this.suffix3 = suffix3;
            this.suffix2 = suffix2;
        }

        @Override
        public A get(int i) {
            if(i >= 0 && i < this.length0) {
                final int io_ = i - this.len123;
                if(io_ >= 0) {
                    final int i4 = io_ >>> BITS3;
                    final int i3 = (io_ >>> BITS2) & MASK;
                    final int i2 = (io_ >>> BITS) & MASK;
                    final int i1 = io_ & MASK;
                    if(i4 < this.data4.length) return (A) this.data4[i4][i3][i2][i1];
                    else if(i3 < this.suffix3.length) return (A) this.suffix3[i3][i2][i1];
                    else if(i2 < this.suffix2.length) return (A) this.suffix2[i2][i1];
                    else return (A) this.suffix1[i1];
                } else if(i >= this.len12) {
                    final int io = i - this.len12;
                    return (A) this.prefix3[io >>> BITS2][(io >>> BITS) & MASK][io & MASK];
                } else if(i >= this.len1) {
                    final int io = i - this.len1;
                    return (A) this.prefix2[io >>> BITS][io & MASK];
                } else return (A) this.prefix1[i];
            } else throw ioob(i);
        }

        @Override
        public Vector<A> updated(int index, A elem) {
            if(index >= 0 && index < this.length0) {
                if(index >= this.len123) {
                    final int io = index - this.len123;
                    final int i4 = io >>> BITS3;
                    final int i3 = (io >>> BITS2) & MASK;
                    final int i2 = (io >>> BITS) & MASK;
                    final int i1 = io & MASK;
                    if(i4 < this.data4.length) return new Vector4<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            copyUpdate(this.data4, i4, i3, i2, i1, elem),
                            this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i3 < suffix3.length) return new Vector4<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.data4,
                            copyUpdate(this.suffix3, i3, i2, i1, elem), this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i2 < suffix2.length) return new Vector4<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.data4,
                            this.suffix3, copyUpdate(this.suffix2, i2, i1, elem), this.suffix1,
                            this.length0
                    ); else return new Vector4<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.data4,
                            this.suffix3, this.suffix2, copyUpdate(this.suffix1, i1, elem),
                            this.length0
                    );
                } else if(index >= this.len12) {
                    final int io = index - this.len12;
                    return new Vector4<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            copyUpdate(this.prefix3,io >>> BITS2, (io >>> BITS) & MASK, io & MASK, elem), this.len123,
                            this.data4,
                            this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else if(index >= this.len1) {
                    final int io = index - this.len1;
                    return new Vector4<>(
                            this.prefix1, this.len1,
                            copyUpdate(this.prefix2,io >>> BITS, io & MASK, elem), this.len12,
                            this.prefix3, this.len123,
                            this.data4,
                            this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else return new Vector4<>(
                        copyUpdate(this.prefix1, index, elem), this.len1,
                        this.prefix2, this.len12,
                        this.prefix3, this.len123,
                        this.data4,
                        this.suffix3, this.suffix2, this.suffix1,
                        this.length0
                );
            } else throw ioob(index);
        }

        @Override
        public Vector<A> appended(A elem) {
            if(suffix1.length < WIDTH) return new Vector4<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.data4,
                    this.suffix3, this.suffix2, copyAppend1(this.suffix1, elem), this.length0 + 1
            ); else if(suffix2.length < WIDTH - 1) return new Vector4<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.data4,
                    this.suffix3, copyAppend(this.suffix2, this.suffix1), wrap1(elem), this.length0 + 1
            ); else if(suffix3.length < WIDTH - 1) return new Vector4<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.data4,
                    copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1)), empty2, wrap1(elem), this.length0 + 1
            ); else if(data4.length < WIDTH - 2) return new Vector4<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123,
                    copyAppend(this.data4, copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1))),
                    empty3, empty2, wrap1(elem), this.length0 + 1
            ); else return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123,
                    this.data4, (WIDTH - 2) * WIDTH3 + this.len123, empty5,
                    wrap4(copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1))), empty3, empty2, wrap1(elem),
                    this.length0 + 1
            );
        }

        @Override
        public Vector<A> prepended(A elem) {
            if(this.len1 < WIDTH) return new Vector4<>(
                    copyPrepend1(elem, this.prefix1), this.len1 + 1, this.prefix2, this.len12 + 1,
                    this.prefix3, this.len123 + 1, this.data4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len12 < WIDTH2) return new Vector4<>(
                    wrap1(elem), 1, copyPrepend(this.prefix1, this.prefix2), this.len12 + 1,
                    this.prefix3, this.len123 + 1, this.data4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len123 < WIDTH3) return new Vector4<>(
                    wrap1(elem), 1, empty2, 1,
                    copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.len123 + 1,
                    this.data4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.data4.length < WIDTH - 2) return new Vector4<>(
                    wrap1(elem), 1, empty2, 1, empty3, 1,
                    copyPrepend(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.data4),
                    this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else return new Vector5<>(
                    wrap1(elem), 1, empty2, 1, empty3, 1,
                    wrap4(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3)), this.len123 + 1,
                    empty5, this.data4, this.suffix3, this.suffix2, this.suffix1, this.length0 + 1
            );
        }

        @Override
        public <B> Vector<B> map(Function<A, B> f) {
            return new Vector4<>(
                    mapElems1(this.prefix1, f), this.len1,
                    mapElems(2, this.prefix2, f), this.len12,
                    mapElems(3, this.prefix3, f), this.len123,
                    mapElems(4, this.data4, f),
                    mapElems(3, this.suffix3, f), mapElems(2, this.suffix2, f), mapElems1(this.suffix1, f),
                    this.length0
            );
        }

        @Override
        protected Vector<A> slice0(int lo, int hi) {
            final VectorSliceBuilder b = new VectorSliceBuilder(lo, hi);
            b.consider(1, this.prefix1);
            b.consider(2, this.prefix2);
            b.consider(3, this.prefix3);
            b.consider(4, this.data4);
            b.consider(3, this.suffix3);
            b.consider(2, this.suffix2);
            b.consider(1, this.suffix1);
            return b.result();
        }

        @Override
        public Vector<A> tail() {
            if(this.len1 > 1) return new Vector4<>(
                    copyTail(this.prefix1), this.len1 - 1,
                    this.prefix2, this.len12 - 1,
                    this.prefix3, this.len123 - 1,
                    this.data4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 - 1
            ); else return this.slice0(1, this.length0);
        }

        @Override
        public Vector<A> init() {
            if(this.suffix1.length > 1) return new Vector4<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.data4,
                    this.suffix3, this.suffix2, copyInit(this.suffix1),
                    this.length0 - 1
            ); else return this.slice(0, this.length0 - 1);
        }

        @Override
        int vectorSliceCount() {
            return 7;
        }

        @Override
        Object[] vectorSlice(int idx) {
            return switch(idx) {
                case 0 -> this.prefix1;
                case 1 -> this.prefix2;
                case 2 -> this.prefix3;
                case 3 -> this.data4;
                case 4 -> this.suffix3;
                case 5 -> this.suffix2;
                case 6 -> this.suffix1;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        int vectorSlicePrefixLength(int idx) {
            return switch (idx) {
                case 0 -> this.len1;
                case 1 -> this.len12;
                case 2 -> this.len123;
                case 3 -> this.len123 + this.data4.length * WIDTH3;
                case 4 -> this.len123 + this.data4.length * WIDTH3 + this.suffix3.length * WIDTH2;
                case 5 -> this.length0 - this.suffix1.length;
                case 6 -> this.length0;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        protected Vector<A> prependedAll0(Iterable<A> prefix, int k) {
            Object[] prefix1b = prepend1IfSpace(this.prefix1, prefix);
            if(prefix1b == null) return super.prependedAll0(prefix, k);
            else {
                final int diff = prefix1b.length - this.prefix1.length;
                return new Vector4<>(
                        prefix1b, this.len1 + diff,
                        this.prefix2, this.len12 + diff,
                        this.prefix3, this.len123 + diff,
                        this.data4, this.suffix3, this.suffix2, this.suffix1,
                        this.length0 + diff
                );
            }
        }

        @Override
        protected Vector<A> appendedAll0(Iterable<A> suffix, int k) {
            Object[] suffix1b = append1IfSpace(this.suffix1, suffix);
            if(suffix1b != null) return new Vector4<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.data4, this.suffix3, this.suffix2,
                    suffix1b, this.length0 - this.suffix1.length + suffix1b.length
            ); else return super.appendedAll0(suffix, k);
        }
    }

    private static final class Vector5<A> extends BigVector<A> {

        final int len1;
        final Object[][] prefix2;
        final int len12;
        final Object[][][] prefix3;
        final int len123;
        final Object[][][][] prefix4;
        final int len1234;
        final Object[][][][][] data5;
        final Object[][][][] suffix4;
        final Object[][][] suffix3;
        final Object[][] suffix2;

        Vector5(
                Object[] prefix1, int len1,
                Object[][] prefix2, int len12,
                Object[][][] prefix3, int len123,
                Object[][][][] prefix4, int len1234,
                Object[][][][][] data5,
                Object[][][][] suffix4, Object[][][] suffix3, Object[][] suffix2, Object[] suffix1,
                int length0
        ) {
            super(prefix1,  suffix1, length0);
            this.len1 = len1;
            this.prefix2 = prefix2;
            this.len12 = len12;
            this.prefix3 = prefix3;
            this.len123 = len123;
            this.prefix4 = prefix4;
            this.len1234 = len1234;
            this.data5 = data5;
            this.suffix4 = suffix4;
            this.suffix3 = suffix3;
            this.suffix2 = suffix2;
        }

        @Override
        public A get(int i) {
            if(i >= 0 && i < this.length0) {
                final int io_ = i - this.len1234;
                if(io_ >= 0) {
                    final int i5 = io_ >>> BITS4;
                    final int i4 = (io_ >>> BITS3) & MASK;
                    final int i3 = (io_ >>> BITS2) & MASK;
                    final int i2 = (io_ >>> BITS) & MASK;
                    final int i1 = io_ & MASK;
                    if(i5 < this.data5.length) return (A) this.data5[i5][i4][i3][i2][i1];
                    else if(i4 < this.suffix4.length) return (A) this.suffix4[i4][i3][i2][i1];
                    else if(i3 < this.suffix3.length) return (A) this.suffix3[i3][i2][i1];
                    else if(i2 < this.suffix2.length) return (A) this.suffix2[i2][i1];
                    else return (A) this.suffix1[i1];
                } else if(i >= this.len123) {
                    final int io = i - this.len123;
                    return (A) this.prefix4[io >>> BITS3][(io >>> BITS2) & MASK][(io >>> BITS) & MASK][io & MASK];
                } else if(i >= this.len12) {
                    final int io = i - this.len12;
                    return (A) this.prefix3[io >>> BITS2][(io >>> BITS) & MASK][io & MASK];
                } else if(i >= this.len1) {
                    final int io = i - this.len1;
                    return (A) this.prefix2[io >>> BITS][io & MASK];
                } else return (A) this.prefix1[i];
            } else throw ioob(i);
        }

        @Override
        public Vector<A> updated(int index, A elem) {
            if(index >= 0 && index < this.length0) {
                if(index >= this.len1234) {
                    final int io = index - this.len1234;
                    final int i5 = io >>> BITS4;
                    final int i4 = (io >>> BITS3) & MASK;
                    final int i3 = (io >>> BITS2) & MASK;
                    final int i2 = (io >>> BITS) & MASK;
                    final int i1 = io & MASK;
                    if(i5 < this.data5.length) return new Vector5<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            copyUpdate(this.data5, i5, i4, i3, i2, i1, elem),
                            this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i4 < suffix4.length) return new Vector5<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.data5,
                            copyUpdate(this.suffix4, i4, i3, i2, i1, elem), this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i3 < suffix3.length) return new Vector5<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.data5,
                            this.suffix4, copyUpdate(this.suffix3, i3, i2, i1, elem), this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i2 < suffix2.length) return new Vector5<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.data5,
                            this.suffix4, this.suffix3, copyUpdate(this.suffix2, i2, i1, elem), this.suffix1,
                            this.length0
                    ); else return new Vector5<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.data5,
                            this.suffix4, this.suffix3, this.suffix2, copyUpdate(this.suffix1, i1, elem),
                            this.length0
                    );
                } else if(index >= this.len123) {
                    final int io = index - this.len123;
                    return new Vector5<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            copyUpdate(this.prefix4,io >>> BITS3, (io >>> BITS2) & MASK, (io >>> BITS) & MASK, io & MASK, elem), this.len1234,
                            this.data5,
                            this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else if(index >= this.len12) {
                    final int io = index - this.len12;
                    return new Vector5<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            copyUpdate(this.prefix3,io >>> BITS2, (io >>> BITS) & MASK, io & MASK, elem), this.len123,
                            this.prefix4, this.len1234,
                            this.data5,
                            this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else if(index >= this.len1) {
                    final int io = index - this.len1;
                    return new Vector5<>(
                            this.prefix1, this.len1,
                            copyUpdate(this.prefix2,io >>> BITS, io & MASK, elem), this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.data5,
                            this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else return new Vector5<>(
                        copyUpdate(this.prefix1, index, elem), this.len1,
                        this.prefix2, this.len12,
                        this.prefix3, this.len123,
                        this.prefix4, this.len1234,
                        this.data5,
                        this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                        this.length0
                );
            } else throw ioob(index);
        }

        @Override
        public Vector<A> appended(A elem) {
            if(suffix1.length < WIDTH) return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.data5, this.suffix4, this.suffix3, this.suffix2, copyAppend1(this.suffix1, elem), this.length0 + 1
            ); else if(suffix2.length < WIDTH - 1) return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.data5, this.suffix4, this.suffix3, copyAppend(this.suffix2, this.suffix1), wrap1(elem), this.length0 + 1
            ); else if(suffix3.length < WIDTH - 1) return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.data5, this.suffix4,
                    copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1)), empty2, wrap1(elem), this.length0 + 1
            ); else if(suffix4.length < WIDTH - 1) return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.prefix4, this.len1234, this.data5,
                    copyAppend(this.suffix4, copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1))), empty3, empty2, wrap1(elem),
                    this.length0 + 1
            ); else if(data5.length < WIDTH - 2) return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.prefix4, this.len1234,
                    copyAppend(this.data5, copyAppend(this.suffix4, copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1)))),
                    empty4, empty3, empty2, wrap1(elem), this.length0 + 1
            ); else return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12, this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.data5, (WIDTH - 2) * WIDTH4 + this.len1234, empty6,
                    wrap5(copyAppend(this.suffix4, copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1)))),
                    empty4, empty3, empty2, wrap1(elem),
                    this.length0 + 1
            );
        }

        @Override
        public Vector<A> prepended(A elem) {
            if(this.len1 < WIDTH) return new Vector5<>(
                    copyPrepend1(elem, this.prefix1), this.len1 + 1, this.prefix2, this.len12 + 1,
                    this.prefix3, this.len123 + 1, this.prefix4, this.len1234 + 1,
                    this.data5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len12 < WIDTH2) return new Vector5<>(
                    wrap1(elem), 1, copyPrepend(this.prefix1, this.prefix2), this.len12 + 1,
                    this.prefix3, this.len123 + 1, this.prefix4, this.len1234 + 1,
                    this.data5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len123 < WIDTH3) return new Vector5<>(
                    wrap1(elem), 1, empty2, 1,
                    copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.len123 + 1,
                    this.prefix4, this.len1234 + 1, this.data5,
                    this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len1234 < WIDTH4) return new Vector5<>(
                    wrap1(elem), 1, empty2, 1, empty3, 1,
                    copyPrepend(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.prefix4), this.len1234 + 1,
                    this.data5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.data5.length < WIDTH - 2) return new Vector5<>(
                    wrap1(elem), 1, empty2, 1, empty3, 1, empty4, 1,
                    copyPrepend(copyPrepend(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.prefix4), this.data5),
                    this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else return new Vector6<>(
                    wrap1(elem), 1, empty2, 1, empty3, 1, empty4, 1,
                    wrap5(copyPrepend(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.prefix4)), this.len1234 + 1,
                    empty6, this.data5, this.suffix4, this.suffix3, this.suffix2, this.suffix1, this.length0 + 1
            );
        }

        @Override
        public <B> Vector<B> map(Function<A, B> f) {
            return new Vector5<>(
                    mapElems1(this.prefix1, f), this.len1,
                    mapElems(2, this.prefix2, f), this.len12,
                    mapElems(3, this.prefix3, f), this.len123,
                    mapElems(4, this.prefix4, f), this.len1234,
                    mapElems(5, this.data5, f),
                    mapElems(4, this.suffix4, f), mapElems(3, this.suffix3, f), mapElems(2, this.suffix2, f), mapElems1(this.suffix1, f),
                    this.length0
            );
        }

        @Override
        protected Vector<A> slice0(int lo, int hi) {
            final VectorSliceBuilder b = new VectorSliceBuilder(lo, hi);
            b.consider(1, prefix1);
            b.consider(2, prefix2);
            b.consider(3, prefix3);
            b.consider(4, prefix4);
            b.consider(5, data5);
            b.consider(4, suffix4);
            b.consider(3, suffix3);
            b.consider(2, suffix2);
            b.consider(1, suffix1);
            return b.result();
        }

        @Override
        public Vector<A> tail() {
            if(this.len1 > 1) return new Vector5<>(
                    copyTail(this.prefix1), this.len1 - 1,
                    this.prefix2, this.len12 - 1,
                    this.prefix3, this.len123 - 1,
                    this.prefix4, this.len1234 - 1,
                    this.data5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 - 1
            ); else return this.slice0(1, this.length0);
        }

        @Override
        public Vector<A> init() {
            if(this.suffix1.length > 1) return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.data5, this.suffix4, this.suffix3, this.suffix2,
                    copyInit(this.suffix1), this.length0 - 1
            ); else return this.slice(0, this.length0 - 1);
        }

        @Override
        int vectorSliceCount() {
            return 9;
        }

        @Override
        Object[] vectorSlice(int idx) {
            return switch(idx) {
                case 0 -> this.prefix1;
                case 1 -> this.prefix2;
                case 2 -> this.prefix3;
                case 3 -> this.prefix4;
                case 4 -> this.data5;
                case 5 -> this.suffix4;
                case 6 -> this.suffix3;
                case 7 -> this.suffix2;
                case 8 -> this.suffix1;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        int vectorSlicePrefixLength(int idx) {
            return switch(idx) {
                case 0 -> this.len1;
                case 1 -> this.len12;
                case 2 -> this.len123;
                case 3 -> this.len1234;
                case 4 -> this.len1234 + this.data5.length * WIDTH4;
                case 5 -> this.len1234 + this.data5.length * WIDTH4 + this.suffix4.length * WIDTH3;
                case 6 -> this.len1234 + this.data5.length * WIDTH4 + this.suffix4.length * WIDTH3 + this.suffix3.length * WIDTH2;
                case 7 -> this.length0 - this.suffix1.length;
                case 8 -> this.length0;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        protected Vector<A> prependedAll0(Iterable<A> prefix, int k) {
            Object[] prefix1b = prepend1IfSpace(this.prefix1, prefix);
            if(prefix1b == null) return super.prependedAll0(prefix, k);
            else {
                final int diff = prefix1b.length - this.prefix1.length;
                return new Vector5<>(
                        prefix1b, this.len1 + diff,
                        this.prefix2, this.len12 + diff,
                        this.prefix3, this.len123 + diff,
                        this.prefix4, this.len1234 + diff,
                        this.data5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                        this.length0 + diff
                );
            }
        }

        @Override
        protected Vector<A> appendedAll0(Iterable<A> suffix, int k) {
            Object[] suffix1b = append1IfSpace(this.suffix1, suffix);
            if(suffix1b != null) return new Vector5<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.data5, this.suffix4, this.suffix3, this.suffix2,
                    suffix1b, this.length0 - this.suffix1.length + suffix1b.length
            ); else return super.appendedAll0(suffix, k);
        }
    }

    private static final class Vector6<A> extends BigVector<A> {

        final int len1;
        final Object[][] prefix2;
        final int len12;
        final Object[][][] prefix3;
        final int len123;
        final Object[][][][] prefix4;
        final int len1234;
        final Object[][][][][] prefix5;
        final int len12345;
        final Object[][][][][][] data6;
        final Object[][][][][] suffix5;
        final Object[][][][] suffix4;
        final Object[][][] suffix3;
        final Object[][] suffix2;

        Vector6(
                Object[] prefix1, int len1,
                Object[][] prefix2, int len12,
                Object[][][] prefix3, int len123,
                Object[][][][] prefix4, int len1234,
                Object[][][][][] prefix5, int len12345,
                Object[][][][][][] data6,
                Object[][][][][] suffix5, Object[][][][] suffix4, Object[][][] suffix3, Object[][] suffix2, Object[] suffix1,
                int length0
        ) {
            super(prefix1, suffix1, length0);
            this.len1 = len1;
            this.prefix2 = prefix2;
            this.len12 = len12;
            this.prefix3 = prefix3;
            this.len123 = len123;
            this.prefix4 = prefix4;
            this.len1234 = len1234;
            this.prefix5 = prefix5;
            this.len12345 = len12345;
            this.data6 = data6;
            this.suffix5 = suffix5;
            this.suffix4 = suffix4;
            this.suffix3 = suffix3;
            this.suffix2 = suffix2;
        }

        @Override
        public A get(int i) {
            if(i >= 0 && i < this.length0) {
                final int io_ = i - this.len12345;
                if(io_ >= 0) {
                    final int i6 = io_ >>> BITS5;
                    final int i5 = (io_ >>> BITS4) & MASK;
                    final int i4 = (io_ >>> BITS3) & MASK;
                    final int i3 = (io_ >>> BITS2) & MASK;
                    final int i2 = (io_ >>> BITS) & MASK;
                    final int i1 = io_ & MASK;
                    if(i6 < this.data6.length) return (A) this.data6[i6][i5][i4][i3][i2][i1];
                    else if(i5 < this.suffix5.length) return (A) this.suffix5[i5][i4][i3][i2][i1];
                    else if(i4 < this.suffix4.length) return (A) this.suffix4[i4][i3][i2][i1];
                    else if(i3 < this.suffix3.length) return (A) this.suffix3[i3][i2][i1];
                    else if(i2 < this.suffix2.length) return (A) this.suffix2[i2][i1];
                    else return (A) this.suffix1[i1];
                } else if(i >= this.len1234) {
                    final int io = i - this.len1234;
                    return (A) this.prefix5[io >>> BITS4][(io >>> BITS3) & MASK][(io >>> BITS2) & MASK][(io >>> BITS) & MASK][io & MASK];
                } else if(i >= this.len123) {
                    final int io = i - this.len123;
                    return (A) this.prefix4[io >>> BITS3][(io >>> BITS2) & MASK][(io >>> BITS) & MASK][io & MASK];
                } else if(i >= this.len12) {
                    final int io = i - this.len12;
                    return (A) this.prefix3[io >>> BITS2][(io >>> BITS) & MASK][io & MASK];
                } else if(i >= this.len1) {
                    final int io = i - this.len1;
                    return (A) this.prefix2[io >>> BITS][io & MASK];
                } else return (A) this.prefix1[i];
            } else throw ioob(i);
        }

        @Override
        public Vector<A> updated(int index, A elem) {
            if(index >= 0 && index < this.length0) {
                if(index >= this.len12345) {
                    final int io = index - this.len12345;
                    final int i6 = io >>> BITS5;
                    final int i5 = (io >>> BITS4) & MASK;
                    final int i4 = (io >>> BITS3) & MASK;
                    final int i3 = (io >>> BITS2) & MASK;
                    final int i2 = (io >>> BITS) & MASK;
                    final int i1 = io & MASK;
                    if(i6 < this.data6.length) return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            copyUpdate(this.data6, i6, i5, i4, i3, i2, i1, elem),
                            this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i5 < suffix5.length) return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            copyUpdate(this.suffix5, i5, i4, i3, i2, i1, elem), this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i4 < suffix4.length) return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            this.suffix5, copyUpdate(this.suffix4, i4, i3, i2, i1, elem), this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i3 < suffix3.length) return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            this.suffix5, this.suffix4, copyUpdate(this.suffix3, i3, i2, i1, elem), this.suffix2, this.suffix1,
                            this.length0
                    ); else if(i2 < suffix2.length) return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            this.suffix5, this.suffix4, this.suffix3, copyUpdate(this.suffix2, i2, i1, elem), this.suffix1,
                            this.length0
                    ); else return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            this.suffix5, this.suffix4, this.suffix3, this.suffix2, copyUpdate(this.suffix1, i1, elem),
                            this.length0
                    );
                } else if(index >= this.len1234) {
                    final int io = index - this.len1234;
                    return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            copyUpdate(this.prefix5, io >>> BITS4, (io >>> BITS3) & MASK, (io >>> BITS2) & MASK, (io >>> BITS) & MASK, io & MASK, elem), this.len12345,
                            this.data6,
                            this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else if(index >= this.len123) {
                    final int io = index - this.len123;
                    return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            this.prefix3, this.len123,
                            copyUpdate(this.prefix4,io >>> BITS3, (io >>> BITS2) & MASK, (io >>> BITS) & MASK, io & MASK, elem), this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else if(index >= this.len12) {
                    final int io = index - this.len12;
                    return new Vector6<>(
                            this.prefix1, this.len1,
                            this.prefix2, this.len12,
                            copyUpdate(this.prefix3,io >>> BITS2, (io >>> BITS) & MASK, io & MASK, elem), this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else if(index >= this.len1) {
                    final int io = index - this.len1;
                    return new Vector6<>(
                            this.prefix1, this.len1,
                            copyUpdate(this.prefix2,io >>> BITS, io & MASK, elem), this.len12,
                            this.prefix3, this.len123,
                            this.prefix4, this.len1234,
                            this.prefix5, this.len12345,
                            this.data6,
                            this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                            this.length0
                    );
                } else return new Vector6<>(
                        copyUpdate(this.prefix1, index, elem), this.len1,
                        this.prefix2, this.len12,
                        this.prefix3, this.len123,
                        this.prefix4, this.len1234,
                        this.prefix5, this.len12345,
                        this.data6,
                        this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                        this.length0
                );
            } else throw ioob(index);
        }

        @Override
        public Vector<A> appended(A elem) {
            if(suffix1.length < WIDTH) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345, this.data6,
                    this.suffix5, this.suffix4, this.suffix3, this.suffix2,
                    copyAppend1(this.suffix1, elem), this.length0 + 1
            ); else if(suffix2.length < WIDTH - 1) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345, this.data6,
                    this.suffix5, this.suffix4, this.suffix3,
                    copyAppend(this.suffix2, this.suffix1), wrap1(elem), this.length0 + 1
            ); else if(suffix3.length < WIDTH - 1) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345, this.data6,
                    this.suffix5, this.suffix4,
                    copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1)), empty2, wrap1(elem), this.length0 + 1
            ); else if(suffix4.length < WIDTH - 1) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345, this.data6,
                    this.suffix5,
                    copyAppend(this.suffix4, copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1))),
                    empty3, empty2, wrap1(elem), this.length0 + 1
            ); else if(suffix5.length < WIDTH - 1) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345, this.data6,
                    copyAppend(this.suffix5, copyAppend(this.suffix4, copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1)))),
                    empty4, empty3, empty2, wrap1(elem), this.length0 + 1
            ); else if(data6.length < LASTWIDTH - 2) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345,
                    copyAppend(this.data6, copyAppend(this.suffix5, copyAppend(this.suffix4, copyAppend(this.suffix3, copyAppend(this.suffix2, this.suffix1))))),
                    empty5, empty4, empty3, empty2, wrap1(elem),  this.length0 + 1
            ); else throw new IllegalArgumentException();
        }

        @Override
        public Vector<A> prepended(A elem) {
            if(this.len1 < WIDTH) return new Vector6<>(
                    copyPrepend1(elem, this.prefix1), this.len1 + 1,
                    this.prefix2, this.len12 + 1,
                    this.prefix3, this.len123 + 1,
                    this.prefix4, this.len1234 + 1,
                    this.prefix5, this.len12345 + 1,
                    this.data6, this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len12 < WIDTH2) return new Vector6<>(
                    wrap1(elem), 1,
                    copyPrepend(this.prefix1, this.prefix2), this.len12 + 1,
                    this.prefix3, this.len123 + 1,
                    this.prefix4, this.len1234 + 1,
                    this.prefix5, this.len12345 + 1,
                    this.data6, this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len123 < WIDTH3) return new Vector6<>(
                    wrap1(elem), 1,
                    empty2, 1,
                    copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.len123 + 1,
                    this.prefix4, this.len1234 + 1,
                    this.prefix5, this.len12345 + 1,
                    this.data6, this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len1234 < WIDTH4) return new Vector6<>(
                    wrap1(elem), 1,
                    empty2, 1,
                    empty3, 1,
                    copyPrepend(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.prefix4), this.len1234 + 1,
                    this.prefix5, this.len12345 + 1,
                    this.data6, this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(this.len12345 < WIDTH5) return new Vector6<>(
                    wrap1(elem), 1,
                    empty2, 1,
                    empty3, 1,
                    empty4, 1,
                    copyPrepend(copyPrepend(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.prefix4), this.prefix5), this.len12345 + 1,
                    this.data6, this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else if(data6.length < LASTWIDTH - 2) return new Vector6<>(
                    wrap1(elem), 1,
                    empty2, 1,
                    empty3, 1,
                    empty4, 1,
                    empty5, 1,
                    copyPrepend(copyPrepend(copyPrepend(copyPrepend(copyPrepend(this.prefix1, this.prefix2), this.prefix3), this.prefix4), this.prefix5), this.data6),
                    this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 + 1
            ); else throw new IllegalArgumentException();
        }

        @Override
        public <B> Vector<B> map(Function<A, B> f) {
            return new Vector6<>(
                    mapElems1(this.prefix1, f), this.len1,
                    mapElems(2, this.prefix2, f), this.len12,
                    mapElems(3, this.prefix3, f), this.len123,
                    mapElems(4, this.prefix4, f), this.len1234,
                    mapElems(5, this.prefix5, f), this.len12345,
                    mapElems(6, this.data6, f),
                    mapElems(5, this.suffix5, f), mapElems(4, this.suffix4, f), mapElems(3, this.suffix3, f), mapElems(2, this.suffix2, f), mapElems1(this.suffix1, f),
                    this.length0
            );
        }

        @Override
        protected Vector<A> slice0(int lo, int hi) {
            final VectorSliceBuilder b = new VectorSliceBuilder(lo, hi);
            b.consider(1, this.prefix1);
            b.consider(2, this.prefix2);
            b.consider(3, this.prefix3);
            b.consider(4, this.prefix4);
            b.consider(5, this.prefix5);
            b.consider(6, this.data6);
            b.consider(5, this.suffix5);
            b.consider(4, this.suffix4);
            b.consider(3, this.suffix3);
            b.consider(2, this.suffix2);
            b.consider(1, this.suffix1);
            return b.result();
        }

        @Override
        public Vector<A> tail() {
            if(this.len1 > 1) return new Vector6<>(
                    copyTail(this.prefix1), this.len1 - 1,
                    this.prefix2, this.len12 - 1,
                    this.prefix3, this.len123 - 1,
                    this.prefix4, this.len1234 - 1,
                    this.prefix5, this.len12345 - 1,
                    this.data6, this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                    this.length0 - 1
            ); else return this.slice0(1, this.length0);
        }

        @Override
        public Vector<A> init() {
            if(this.suffix1.length > 1) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345, this.data6,
                    this.suffix5, this.suffix4, this.suffix3, this.suffix2,
                    copyInit(this.suffix1), this.length0 - 1
            ); else return this.slice0(0, this.length0 - 1);
        }

        @Override
        int vectorSliceCount() {
            return 11;
        }

        @Override
        Object[] vectorSlice(int idx) {
            return switch(idx) {
                case 0 -> this.prefix1;
                case 1 -> this.prefix2;
                case 2 -> this.prefix3;
                case 3 -> this.prefix4;
                case 4 -> this.prefix5;
                case 5 -> this.data6;
                case 6 -> this.suffix5;
                case 7 -> this.suffix4;
                case 8 -> this.suffix3;
                case 9 -> this.suffix2;
                case 10 -> this.suffix1;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        int vectorSlicePrefixLength(int idx) {
            return switch(idx) {
                case 0 -> this.len1;
                case 1 -> this.len12;
                case 2 -> this.len123;
                case 3 -> this.len1234;
                case 4 -> this.len12345;
                case 5 -> this.len12345 + this.data6.length * WIDTH5;
                case 6 -> this.len12345 + this.data6.length * WIDTH5 + this.suffix5.length * WIDTH4;
                case 7 -> this.len12345 + this.data6.length * WIDTH5 + this.suffix5.length * WIDTH4 + this.suffix4.length * WIDTH3;
                case 8 -> this.len12345 + this.data6.length * WIDTH5 + this.suffix5.length * WIDTH4 + this.suffix4.length * WIDTH3 +  this.suffix3.length * WIDTH2;
                case 9 -> this.length0 - this.suffix1.length;
                case 10 -> this.length0;
                default -> throw new IllegalArgumentException("slice idx " + idx);
            };
        }

        @Override
        protected Vector<A> prependedAll0(Iterable<A> prefix, int k) {
            final Object[] prefix1b = prepend1IfSpace(this.prefix1, prefix);
            if(prefix1b == null) return super.prependedAll0(prefix, k);
            else {
                final int diff = prefix1b.length - this.prefix1.length;
                return new Vector6<>(
                        prefix1b, this.len1 + diff,
                        this.prefix2, this.len12 + diff,
                        this.prefix3, this.len123 + diff,
                        this.prefix4, this.len1234 + diff,
                        this.prefix5, this.len12345 + diff,
                        this.data6, this.suffix5, this.suffix4, this.suffix3, this.suffix2, this.suffix1,
                        this.length0 + diff
                );
            }
        }

        @Override
        protected Vector<A> appendedAll0(Iterable<A> suffix, int k) {
            final Object[] suffix1b = append1IfSpace(this.suffix1, suffix);
            if(suffix1b != null) return new Vector6<>(
                    this.prefix1, this.len1, this.prefix2, this.len12,
                    this.prefix3, this.len123, this.prefix4, this.len1234,
                    this.prefix5, this.len12345, this.data6,
                    this.suffix5, this.suffix4, this.suffix3, this.suffix2,
                    suffix1b, this.length0 - this.suffix1.length + suffix1b.length
            ); else return super.appendedAll0(suffix, k);
        }
    }

    private static final class VectorSliceBuilder {
        private static int prefixIdx(int n) {
            return n - 1;
        }

        private static int suffixIdx(int n) {
            return 11 - n;
        }

        private final int lo;
        private final int hi;

        private final Object[][] slices;
        private int len;
        private int pos;
        private int maxDim;

        public VectorSliceBuilder(int lo, int hi) {
            this.lo = lo;
            this.hi = hi;
            this.slices = new Object[11][];
            this.len = 0;
            this.pos = 0;
            this.maxDim = 0;
        }

        public <T> void consider(int n, T[] a) {
            final int count = a.length * (1 << (BITS * (n - 1)));
            final int lo0 = Math.max(this.lo - this.pos, 0);
            final int hi0 = Math.min(this.hi - this.pos, count);
            if(hi0 > lo0) {
                this.addSlice(n, a, lo0, hi0);
                this.len += (hi0 - lo0);
            }
            this.pos += count;
        }

        private <T> void addSlice(int n, T[] a, int lo, int hi) {
            if(n == 1) {
                this.add(1, copyOrUse(a, lo, hi));
            } else {
                final int bitsN = BITS * (n - 1);
                final int widthN = 1 << bitsN;
                final int loN = lo >>> bitsN;
                final int hiN = hi >>> bitsN;
                final int loRest = lo & (widthN - 1);
                final int hiRest = hi & (widthN - 1);

                if(loRest == 0) {
                    if(hiRest == 0) {
                        this.add(n, copyOrUse(a, loN, hiN));
                    } else {
                        if(hiN > loN) this.add(n, copyOrUse(a, loN, hiN));
                        this.addSlice(n - 1, (Object[]) a[hiN], 0, hiRest);
                    }
                } else {
                    if(hiN == loN) {
                        this.addSlice(n - 1, (Object[]) a[loN], loRest, hiRest);
                    } else {
                        this.addSlice(n - 1, (Object[]) a[loN], loRest, widthN);
                        if(hiRest == 0) {
                            if(hiN > loN + 1) this.add(n, copyOrUse(a, loN + 1, hiN));
                        } else {
                            if(hiN > loN + 1) this.add(n, copyOrUse(a, loN + 1, hiN));
                            this.addSlice(n - 1, (Object[]) a[hiN], 0, hiRest);
                        }
                    }
                }
            }
        }

        private <T> void add(int n, T[] a) {
            final int idx;
            if(n <= maxDim) idx = suffixIdx(n);
            else {
                maxDim = n;
                idx = prefixIdx(n);
            }
            this.slices[idx] = a;
        }

        public <A> Vector<A> result() {
            if(this.len <= 32) {
                if(this.len == 0) return Vector0.getInstance();
                else {
                    final Object[] prefix1 = slices[prefixIdx(1)];
                    final Object[] suffix1 = slices[suffixIdx(1)];
                    final Object[] a;
                    if(prefix1 != null) {
                        if(suffix1 != null) a = concatArrays(prefix1, suffix1);
                        else a = prefix1;
                    } else if(suffix1 != null) a = suffix1;
                    else {
                        final Object[][] prefix2 = (Object[][]) slices[prefixIdx(2)];
                        if(prefix2 != null) a = prefix2[0];
                        else {
                            final Object[][] suffix2 = (Object[][]) slices[suffixIdx(2)];
                            a = suffix2[0];
                        }
                    }
                    return new Vector1<>(a);
                }
            } else {
                this.balancePrefix(1);
                this.balanceSuffix(1);
                int resultDim = this.maxDim;
                if(resultDim < 6) {
                    final Object[] pre = this.slices[prefixIdx(this.maxDim)];
                    final Object[] suf = this.slices[suffixIdx(this.maxDim)];
                    if(pre != null && suf != null) {
                        if(pre.length + suf.length <= WIDTH - 2) {
                            this.slices[prefixIdx(this.maxDim)] = concatArrays(pre, suf);
                            this.slices[suffixIdx(this.maxDim)] = null;
                        } else resultDim += 1;
                    } else {
                        final Object[] one = pre != null ? pre : suf;
                        if(one.length > WIDTH - 2) resultDim += 1;
                    }
                }
                final Object[] prefix1 = this.slices[prefixIdx(1)];
                final Object[] suffix1 = this.slices[suffixIdx(1)];
                final int len1 = prefix1.length;
                return switch(resultDim) {
                    case 2 -> {
                        final Object[][] data2 = dataOr(2, empty2);
                        yield new Vector2<>(prefix1, len1, data2, suffix1, this.len);
                    }
                    case 3 -> {
                        final Object[][] prefix2 = prefixOr(2, empty2);
                        final Object[][][] data3 = dataOr(3, empty3);
                        final Object[][] suffix2 = suffixOr(2, empty2);
                        final int len12 = len1 + (prefix2.length * WIDTH);
                        yield new Vector3<>(
                                prefix1, len1,
                                prefix2, len12,
                                data3,
                                suffix2, suffix1,
                                this.len
                        );
                    }
                    case 4 -> {
                        final Object[][] prefix2 = prefixOr(2, empty2);
                        final Object[][][] prefix3 = prefixOr(3, empty3);
                        final Object[][][][] data4 = dataOr(4, empty4);
                        final Object[][][] suffix3 = suffixOr(3, empty3);
                        final Object[][] suffix2 = suffixOr(2, empty2);
                        final int len12 = len1 + (prefix2.length * WIDTH);
                        final int len123 = len12 + (prefix3.length * WIDTH2);
                        yield new Vector4<>(
                                prefix1, len1,
                                prefix2, len12,
                                prefix3, len123,
                                data4,
                                suffix3, suffix2, suffix1,
                                this.len
                        );
                    }
                    case 5 -> {
                        final Object[][] prefix2 = prefixOr(2, empty2);
                        final Object[][][] prefix3 = prefixOr(3, empty3);
                        final Object[][][][] prefix4 = prefixOr(4, empty4);
                        final Object[][][][][] data5 = dataOr(5, empty5);
                        final Object[][][][] suffix4 = suffixOr(4, empty4);
                        final Object[][][] suffix3 = suffixOr(3, empty3);
                        final Object[][] suffix2 = suffixOr(2, empty2);
                        final int len12 = len1 + (prefix2.length * WIDTH);
                        final int len123 = len12 + (prefix3.length * WIDTH2);
                        final int len1234 = len123 + (prefix4.length * WIDTH3);
                        yield new Vector5<>(
                                prefix1, len1,
                                prefix2, len12,
                                prefix3, len123,
                                prefix4, len1234,
                                data5,
                                suffix4, suffix3, suffix2, suffix1,
                                this.len
                        );
                    }
                    case 6 -> {
                        final Object[][] prefix2 = prefixOr(2, empty2);
                        final Object[][][] prefix3 = prefixOr(3, empty3);
                        final Object[][][][] prefix4 = prefixOr(4, empty4);
                        final Object[][][][][] prefix5 = prefixOr(5, empty5);
                        final Object[][][][][][] data6 = dataOr(6, empty6);
                        final Object[][][][][] suffix5 = suffixOr(5, empty5);
                        final Object[][][][] suffix4 = suffixOr(4, empty4);
                        final Object[][][] suffix3 = suffixOr(3, empty3);
                        final Object[][] suffix2 = suffixOr(2, empty2);
                        final int len12 = len1 + (prefix2.length * WIDTH);
                        final int len123 = len12 + (prefix3.length * WIDTH2);
                        final int len1234 = len123 + (prefix4.length * WIDTH3);
                        final int len12345 = len1234 + (prefix5.length * WIDTH4);
                        yield new Vector6<>(
                                prefix1, len1, prefix2, len12,
                                prefix3, len123, prefix4, len1234,
                                prefix5, len12345,
                                data6,
                                suffix5, suffix4, suffix3, suffix2, suffix1,
                                this.len
                        );
                    }
                    default -> throw new IllegalStateException("bad resultDim " + resultDim);
                };
            }
        }

        private <T> T[] prefixOr(int n, T[] a) {
            final Object[] p = this.slices[prefixIdx(n)];
            return p != null ? (T[]) p : a;
        }

        private <T> T[] suffixOr(int n, T[] a) {
            final Object[] s = this.slices[suffixIdx(n)];
            return s != null ? (T[]) s : a;
        }

        private <T> T[] dataOr(int n, T[] a) {
            final Object[] p = this.slices[prefixIdx(n)];
            if(p != null) return (T[]) p;
            else {
                final Object[] s = this.slices[suffixIdx(n)];
                return s != null ? (T[]) s : a;
            }
        }

        private void balancePrefix(int n) {
            if(this.slices[prefixIdx(n)] == null) {
                if(n == this.maxDim) {
                    this.slices[prefixIdx(n)] = this.slices[suffixIdx(n)];
                    this.slices[suffixIdx(n)] = null;
                } else {
                    this.balancePrefix(n + 1);
                    final Object[][] preN1 = (Object[][]) this.slices[prefixIdx(n + 1)];
                    this.slices[prefixIdx(n)] = preN1[0];
                    if(preN1.length == 1) {
                        this.slices[prefixIdx(n + 1)] = null;
                        if((this.maxDim == n + 1) && (this.slices[suffixIdx(n + 1)] == null)) this.maxDim = n;
                    } else {
                        this.slices[prefixIdx(n + 1)] = copyOfRange(preN1, 1, preN1.length);
                    }
                }
            }
        }

        private void balanceSuffix(int n) {
            if(this.slices[suffixIdx(n)] == null) {
                if(n == this.maxDim) {
                    slices[suffixIdx(n)] = this.slices[prefixIdx(n)];
                    slices[prefixIdx(n)] = null;
                } else {
                    this.balanceSuffix(n + 1);
                    final Object[][] sufN1 = (Object[][]) this.slices[suffixIdx(n + 1)];
                    this.slices[suffixIdx(n)] = sufN1[sufN1.length - 1];
                    if(sufN1.length == 1) {
                        this.slices[suffixIdx(n + 1)] = null;
                        if((this.maxDim == n + 1) && (this.slices[prefixIdx(n + 1)] == null)) this.maxDim = n;
                    } else {
                        this.slices[suffixIdx(n + 1)] = copyOfRange(sufN1, 0, sufN1.length - 1);
                    }
                }
            }
        }
    }

    public static final class VectorBuilder<A> {
        private Object[][][][][][] a6;
        private Object[][][][][] a5;
        private Object[][][][] a4;
        private Object[][][] a3;
        private Object[][] a2;
        private Object[] a1;
        private int len1;
        private int lenRest;
        private int offset;
        private boolean prefixIsRightAligned;
        private int depth;

        public VectorBuilder() {
            this.clear();
        }

        private void setLen(int i) {
            this.len1 = i & MASK;
            this.lenRest = i - this.len1;
        }

        public int size() {
            return this.len1 + this.lenRest - this.offset;
        }

        public void clear() {
            this.a6 = null;
            this.a5 = null;
            this.a4 = null;
            this.a3 = null;
            this.a2 = null;
            this.a1 = new Object[WIDTH];
            this.len1 = 0;
            this.lenRest = 0;
            this.offset = 0;
            this.prefixIsRightAligned = false;
            this.depth = 1;
        }

        void initSparse(int size, A elem) {
            this.setLen(size);
            Arrays.fill(this.a1, elem);
            if(size > WIDTH) {
                this.a2 = new Object[WIDTH][];
                Arrays.fill(this.a2, this.a1);
                if(size > WIDTH2) {
                    this.a3 = new Object[WIDTH][][];
                    Arrays.fill(this.a3, this.a2);
                    if(size > WIDTH3) {
                        this.a4 = new Object[WIDTH][][][];
                        Arrays.fill(this.a4, this.a3);
                        if(size > WIDTH4) {
                            this.a5 = new Object[WIDTH][][][][];
                            Arrays.fill(this.a5, this.a4);
                            if(size > WIDTH5) {
                                this.a6 = new Object[WIDTH][][][][][];
                                Arrays.fill(this.a6, this.a5);
                                this.depth = 6;
                            } else this.depth = 5;
                        } else this.depth = 4;
                    } else this.depth = 3;
                } else this.depth = 2;
            } else this.depth = 1;
        }

        void initFrom(Object[] prefix1) {
            this.depth = 1;
            this.setLen(prefix1.length);
            this.a1 = copyOrUse(prefix1, 0, WIDTH);
            if(this.len1 == 0 && this.lenRest > 0) {
                this.len1 = WIDTH;
                this.lenRest -= WIDTH;
            }
        }

        VectorBuilder<A> initFrom(Vector<?> v) {
            switch(v.vectorSliceCount()) {
                case 0 -> {}
                case 1 -> {
                    final Vector1<?> v1 = (Vector1<?>) v;
                    this.depth = 1;
                    this.setLen(v1.prefix1.length);
                    this.a1 = copyOrUse(v1.prefix1, 0, WIDTH);
                }
                case 3 -> {
                    final Vector2<?> v2 = (Vector2<?>) v;
                    final Object[][] d2 = v2.data2;
                    this.a1 = copyOrUse(v2.suffix1, 0, WIDTH);
                    this.depth = 2;
                    this.offset = WIDTH - v2.len1;
                    this.setLen(v2.length0 + this.offset);
                    this.a2 = new Object[WIDTH][];
                    this.a2[0] = v2.prefix1;
                    System.arraycopy(d2, 0, this.a2, 1, d2.length);
                    this.a2[d2.length + 1] = this.a1;
                }
                case 5 -> {
                    final Vector3<?> v3 = (Vector3<?>) v;
                    final Object[][][] d3 = v3.data3;
                    final Object[][] s2 = v3.suffix2;
                    this.a1 = copyOrUse(v3.suffix1, 0, WIDTH);
                    this.depth = 3;
                    this.offset = WIDTH2 - v3.len12;
                    this.setLen(v3.length0 + this.offset);
                    this.a3 = new Object[WIDTH][][];
                    this.a3[0] = copyPrepend(v3.prefix1, v3.prefix2);
                    System.arraycopy(d3, 0, this.a3, 1, d3.length);
                    this.a2 = copyOf(s2, WIDTH);
                    this.a3[d3.length + 1] = this.a2;
                    this.a2[s2.length] = this.a1;
                }
                case 7 -> {
                    final Vector4<?> v4 = (Vector4<?>) v;
                    final Object[][][][] d4 = v4.data4;
                    final Object[][][] s3 = v4.suffix3;
                    final Object[][] s2 = v4.suffix2;
                    this.a1 = copyOrUse(v4.suffix1, 0, WIDTH);
                    this.depth = 4;
                    this.offset = WIDTH3 - v4.len123;
                    this.setLen(v4.length0 + this.offset);
                    this.a4 = new Object[WIDTH][][][];
                    this.a4[0] = copyPrepend(copyPrepend(v4.prefix1, v4.prefix2), v4.prefix3);
                    System.arraycopy(d4, 0, this.a4, 1, d4.length);
                    this.a3 = copyOf(s3, WIDTH);
                    this.a2 = copyOf(s2, WIDTH);
                    this.a4[d4.length + 1] = this.a3;
                    this.a3[s3.length] = this.a2;
                    this.a2[s2.length] = this.a1;
                }
                case 9 -> {
                    final Vector5<?> v5 = (Vector5<?>) v;
                    final Object[][][][][] d5 = v5.data5;
                    final Object[][][][] s4 = v5.suffix4;
                    final Object[][][] s3 = v5.suffix3;
                    final Object[][] s2 = v5.suffix2;
                    this.a1 = copyOrUse(v5.suffix1, 0, WIDTH);
                    this.depth = 5;
                    this.offset = WIDTH4 - v5.len1234;
                    this.setLen(v5.length0 + this.offset);
                    this.a5 = new Object[WIDTH][][][][];
                    this.a5[0] = copyPrepend(copyPrepend(copyPrepend(v5.prefix1, v5.prefix2), v5.prefix3), v5.prefix4);
                    System.arraycopy(d5, 0, this.a5, 1, d5.length);
                    this.a4 = copyOf(s4, WIDTH);
                    this.a3 = copyOf(s3, WIDTH);
                    this.a2 = copyOf(s2, WIDTH);
                    this.a5[d5.length + 1] = this.a4;
                    this.a4[s4.length] = this.a3;
                    this.a3[s3.length] = this.a2;
                    this.a2[s2.length] = this.a1;
                }
                case 11 -> {
                    final Vector6<?> v6 = (Vector6<?>) v;
                    final Object[][][][][][] d6 = v6.data6;
                    final Object[][][][][] s5 = v6.suffix5;
                    final Object[][][][] s4 = v6.suffix4;
                    final Object[][][] s3 = v6.suffix3;
                    final Object[][] s2 = v6.suffix2;
                    this.a1 = copyOrUse(v6.suffix1, 0, WIDTH);
                    this.depth = 6;
                    this.offset = WIDTH5 - v6.len12345;
                    this.setLen(v6.length0 + this.offset);
                    this.a6 = new Object[WIDTH][][][][][];
                    this.a6[0] = copyPrepend(copyPrepend(copyPrepend(copyPrepend(v6.prefix1, v6.prefix2), v6.prefix3), v6.prefix4), v6.prefix5);
                    System.arraycopy(d6, 0, this.a6, 1, d6.length);
                    this.a5 = copyOf(s5, WIDTH);
                    this.a4 = copyOf(s4, WIDTH);
                    this.a3 = copyOf(s3, WIDTH);
                    this.a2 = copyOf(s2, WIDTH);
                    this.a6[d6.length + 1] = this.a5;
                    this.a5[s5.length] = this.a4;
                    this.a4[s4.length] = this.a3;
                    this.a3[s3.length] = this.a2;
                    this.a2[s2.length] = this.a1;
                }
            }
            if(this.len1 == 0 && this.lenRest > 0) {
                this.len1 = WIDTH;
                this.lenRest -= WIDTH;
            }
            return this;
        }

        public VectorBuilder<A> alignTo(int before, Vector<A> bigVector) {
            if(this.len1 != 0 || this.lenRest != 0)
                throw new UnsupportedOperationException("A non-empty VectorBuilder cannot be aligned retrospectively. Please call .reset() or use a new VectorBuilder.");

            final int prefixLength;
            final int maxPrefixLength;
            if(bigVector instanceof Vector0) {
                prefixLength = 0; maxPrefixLength = 1;
            } else if(bigVector instanceof Vector1<?>) {
                prefixLength = 0; maxPrefixLength = 1;
            } else if(bigVector instanceof Vector2<?> v2) {
                prefixLength = v2.len1; maxPrefixLength = WIDTH;
            } else if(bigVector instanceof Vector3<?> v3) {
                prefixLength = v3.len12; maxPrefixLength = WIDTH2;
            } else if(bigVector instanceof Vector4<?> v4) {
                prefixLength = v4.len123; maxPrefixLength = WIDTH3;
            } else if(bigVector instanceof Vector5<?> v5) {
                prefixLength = v5.len1234; maxPrefixLength = WIDTH4;
            } else if(bigVector instanceof Vector6<?> v6) {
                prefixLength = v6.len12345; maxPrefixLength = WIDTH5;
            } else {
                throw new IllegalStateException();
            }

            if(maxPrefixLength == 1) return this;

            final int overallPrefixLength = (before + prefixLength) % maxPrefixLength;
            this.offset = (maxPrefixLength - overallPrefixLength) % maxPrefixLength;

            this.advanceN(this.offset & ~MASK);
            this.len1 = this.offset & MASK;
            this.prefixIsRightAligned = true;
            return this;
        }

        private void shrinkOffsetIfTooLarge(int width) {
            final int newOffset = this.offset % width;
            this.lenRest -= (offset - newOffset);
            this.offset = newOffset;
        }

        private void leftAlignPrefix() {
            Object[] a = null;
            Object[] aParent = null;
            if(this.depth >= 6) {
                a = this.a6;
                final int i = this.offset >>> BITS5;
                if(i > 0) System.arraycopy(a, i, a, 0, LASTWIDTH - i);
                this.shrinkOffsetIfTooLarge(WIDTH5);
                if((this.lenRest >>> BITS5) == 0) this.depth = 5;
                aParent = a;
                a = (Object[]) a[0];
            }
            if(this.depth >= 5) {
                if(a == null) a = this.a5;
                final int i = (this.offset >>> BITS4) & MASK;
                if(this.depth == 5) {
                    if(i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                    this.a5 = (Object[][][][][]) a;
                    this.shrinkOffsetIfTooLarge(WIDTH4);
                    if((this.lenRest >>> BITS4) == 0) this.depth = 4;
                } else {
                    if(i > 0) a = copyOfRange(a, i, WIDTH);
                    aParent[0] = a;
                }
                aParent = a;
                a = (Object[]) a[0];
            }
            if(this.depth >= 4) {
                if(a == null) a = this.a4;
                final int i = (this.offset >>> BITS3) & MASK;
                if(this.depth == 4) {
                    if(i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                    this.a4 = (Object[][][][]) a;
                    this.shrinkOffsetIfTooLarge(WIDTH3);
                    if((this.lenRest >>> BITS3) == 0) this.depth = 3;
                } else {
                    if(i > 0) a = copyOfRange(a, i, WIDTH);
                    aParent[0] = a;
                }
                aParent = a;
                a = (Object[]) a[0];
            }
            if(this.depth >= 3) {
                if(a == null) a = this.a3;
                final int i = (this.offset >>> BITS2) & MASK;
                if(this.depth == 3) {
                    if(i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                    this.a3 = (Object[][][]) a;
                    this.shrinkOffsetIfTooLarge(WIDTH2);
                    if((this.lenRest >>> BITS2) == 0) this.depth = 2;
                } else {
                    if(i > 0) a = copyOfRange(a, i, WIDTH);
                    aParent[0] = a;
                }
                aParent = a;
                a = (Object[]) a[0];
            }
            if(this.depth >= 2) {
                if(a == null) a = this.a2;
                final int i = (this.offset >>> BITS) & MASK;
                if(this.depth == 2) {
                    if(i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                    this.a2 = (Object[][]) a;
                    this.shrinkOffsetIfTooLarge(WIDTH);
                    if((this.lenRest >>> BITS) == 0) this.depth = 1;
                } else {
                    if(i > 0) a = copyOfRange(a, i, WIDTH);
                    aParent[0] = a;
                }
                aParent = a;
                a = (Object[]) a[0];
            }
            if(this.depth >= 1) {
                if(a == null) a = this.a1;
                final int i = this.offset & MASK;
                if(this.depth == 1) {
                    if(i > 0) System.arraycopy(a, i, a, 0, WIDTH - i);
                    this.a1 = (Object[]) a;
                    this.len1 -= this.offset;
                    this.offset = 0;
                } else {
                    if(i > 0) a = copyOfRange(a, i, WIDTH);
                    aParent[0] = a;
                }
            }
            this.prefixIsRightAligned = false;
        }

        public VectorBuilder<A> addOne(A elem) {
            if(this.len1 == WIDTH) this.advance();
            this.a1[len1] = elem;
            this.len1++;
            return this;
        }

        private void addArr1(Object[] data) {
            final int dl = data.length;
            if(dl > 0) {
                if(this.len1 == WIDTH) this.advance();
                final int copy1 = Math.min(WIDTH - this.len1, dl);
                final int copy2 = dl - copy1;
                System.arraycopy(data, 0, this.a1, this.len1, copy1);
                this.len1 += copy1;
                if(copy2 > 0) {
                    this.advance();
                    System.arraycopy(data, copy1, this.a1, 0, copy2);
                    this.len1 += copy2;
                }
            }
        }

        private void addArrN(Object[] slice, int dim) {
            if(slice.length == 0) return;
            if(this.len1 == WIDTH) this.advance();

            final int sl = slice.length;
            switch(dim) {
                case 2 -> {
                    final int copy1 = Math.min(((WIDTH2 - this.lenRest) >>> BITS) & MASK, sl);
                    final int copy2 = sl - copy1;
                    final int destPos = (this.lenRest >>> BITS) & MASK;
                    System.arraycopy((Object[][]) slice, 0, this.a2, destPos, copy1);
                    this.advanceN(WIDTH * copy1);
                    if(copy2 > 0) {
                        System.arraycopy((Object[][]) slice, copy1, this.a2, 0, copy2);
                        this.advanceN(WIDTH * copy2);
                    }
                }
                case 3 -> {
                    if(this.lenRest % WIDTH2 != 0) {
                        for(Object[][] e : (Object[][][]) slice) {
                            this.addArrN(e, 2);
                        }
                    } else {
                        final int copy1 = Math.min(((WIDTH3 - this.lenRest) >>> BITS2) & MASK, sl);
                        final int copy2 = sl - copy1;
                        final int destPos = (this.lenRest >>> BITS2) & MASK;
                        System.arraycopy((Object[][][]) slice, 0, this.a3, destPos, copy1);
                        this.advanceN(WIDTH2 * copy1);
                        if(copy2 > 0) {
                            System.arraycopy((Object[][][]) slice, copy1, this.a3, 0, copy2);
                            this.advanceN(WIDTH2 * copy2);
                        }
                    }
                }
                case 4 -> {
                    if(this.lenRest % WIDTH3 != 0) {
                        for(Object[][][] e : (Object[][][][]) slice) {
                            this.addArrN(e, 3);
                        }
                    } else {
                        final int copy1 = Math.min(((WIDTH4 - this.lenRest) >>> BITS3) & MASK, sl);
                        final int copy2 = sl - copy1;
                        final int destPos = (this.lenRest >>> BITS3) & MASK;
                        System.arraycopy((Object[][][][]) slice, 0, this.a4, destPos, copy1);
                        this.advanceN(WIDTH3 * copy1);
                        if(copy2 > 0) {
                            System.arraycopy((Object[][][][]) slice, copy1, this.a4, 0, copy2);
                            this.advanceN(WIDTH3 * copy2);
                        }
                    }
                }
                case 5 -> {
                    if(this.lenRest % WIDTH4 != 0) {
                        for(Object[][][][] e : (Object[][][][][]) slice) {
                            this.addArrN(e, 4);
                        }
                    } else {
                        final int copy1 = Math.min(((WIDTH5 - this.lenRest) >>> BITS4) & MASK, sl);
                        final int copy2 = sl - copy1;
                        final int destPos = (this.lenRest >>> BITS4) & MASK;
                        System.arraycopy((Object[][][][][]) slice, 0, this.a5, destPos, copy1);
                        this.advanceN(WIDTH4 * copy1);
                        if(copy2 > 0) {
                            System.arraycopy((Object[][][][][]) slice, copy1, this.a5, 0, copy2);
                            this.advanceN(WIDTH4 * copy2);
                        }
                    }
                }
                case 6 -> {
                    if(this.lenRest % WIDTH5 != 0) {
                        for(Object[][][][][] e : (Object[][][][][][]) slice) {
                            this.addArrN(e, 5);
                        }
                    } else {
                        final int copy1 = sl;
                        final int destPos = this.lenRest >>> BITS5;
                        if(destPos + copy1 > LASTWIDTH)
                            throw new IllegalArgumentException("exceeding 2^31 elements");
                        System.arraycopy((Object[][][][][][]) slice, 0, this.a6, destPos, copy1);
                        this.advanceN(WIDTH5 * copy1);
                    }
                }
                default -> throw new IllegalArgumentException();
            }
        }

        private VectorBuilder<A> addVector(Vector<A> xs) {
            final int sliceCount = xs.vectorSliceCount();
            for(int sliceIdx = 0; sliceIdx < sliceCount; sliceIdx++) {
                final Object[] slice = xs.vectorSlice(sliceIdx);
                final int n = vectorSliceDim(sliceCount, sliceIdx);
                if(n == 1) this.addArr1(slice);
                else if(this.len1 == WIDTH || this.len1 == 0) this.addArrN(slice, n);
                else foreachRec(n - 2, slice, this::addArr1);
            }
            return this;
        }

        public VectorBuilder<A> addAll(Iterable<A> xs) {
            if(xs instanceof Vector<?> v) {
                if(this.len1 == 0 && this.lenRest == 0 && !prefixIsRightAligned) this.initFrom(v);
                else this.addVector((Vector<A>) v);
            } else {
                for(A a : xs) {
                    this.addOne(a);
                }
            }
            return this;
        }

        private void advance() {
            final int idx = this.lenRest + WIDTH;
            final int xor = idx ^ this.lenRest;
            this.lenRest = idx;
            this.len1 = 0;
            this.advance1(idx, xor);
        }

        private void advanceN(int n) {
            if(n > 0) {
                final int idx = this.lenRest + n;
                final int xor = idx ^ this.lenRest;
                this.lenRest = idx;
                this.len1 = 0;
                this.advance1(idx, xor);
            }
        }

        private void advance1(int idx, int xor) {
            if(xor <= 0) {
                // TODO actually print stuff like Scala stdlib does
                throw new IllegalStateException();
            } else if(xor < WIDTH2) {
                if(this.depth <= 1) {
                    this.a2 = new Object[WIDTH][];
                    this.a2[0] = this.a1;
                    this.depth = 2;
                }
                this.a1 = new Object[WIDTH];
                this.a2[(idx >>> BITS) & MASK] = this.a1;
            } else if(xor < WIDTH3) {
                if(this.depth <= 2) {
                    this.a3 = new Object[WIDTH][][];
                    this.a3[0] = this.a2;
                    this.depth = 3;
                }
                this.a1 = new Object[WIDTH];
                this.a2 = new Object[WIDTH][];
                this.a2[(idx >>> BITS) & MASK] = this.a1;
                this.a3[(idx >>> BITS2) & MASK] = this.a2;
            } else if(xor < WIDTH4) {
                if(this.depth <= 3) {
                    this.a4 = new Object[WIDTH][][][];
                    this.a4[0] = this.a3;
                    this.depth = 4;
                }
                this.a1 = new Object[WIDTH];
                this.a2 = new Object[WIDTH][];
                this.a3 = new Object[WIDTH][][];
                this.a2[(idx >>> BITS) & MASK] = this.a1;
                this.a3[(idx >>> BITS2) & MASK] = this.a2;
                this.a4[(idx >>> BITS3) & MASK] = this.a3;
            } else if(xor < WIDTH5) {
                if(this.depth <= 4) {
                    this.a5 = new Object[WIDTH][][][][];
                    this.a5[0] = this.a4;
                    this.depth = 5;
                }
                this.a1 = new Object[WIDTH];
                this.a2 = new Object[WIDTH][];
                this.a3 = new Object[WIDTH][][];
                this.a4 = new Object[WIDTH][][][];
                this.a2[(idx >>> BITS) & MASK] = this.a1;
                this.a3[(idx >>> BITS2) & MASK] = this.a2;
                this.a4[(idx >>> BITS3) & MASK] = this.a3;
                this.a5[(idx >>> BITS4) & MASK] = this.a4;
            } else {
                if(this.depth <= 5) {
                    this.a6 = new Object[LASTWIDTH][][][][][];
                    this.a6[0] = this.a5;
                    this.depth = 6;
                }
                this.a1 = new Object[WIDTH];
                this.a2 = new Object[WIDTH][];
                this.a3 = new Object[WIDTH][][];
                this.a4 = new Object[WIDTH][][][];
                this.a5 = new Object[WIDTH][][][][];
                this.a2[(idx >>> BITS) & MASK] = this.a1;
                this.a3[(idx >>> BITS2) & MASK] = this.a2;
                this.a4[(idx >>> BITS3) & MASK] = this.a3;
                this.a5[(idx >>> BITS4) & MASK] = this.a4;
                this.a6[idx >>> BITS5] = this.a5;
            }
        }

        public Vector<A> result() {
            if(this.prefixIsRightAligned) this.leftAlignPrefix();

            final int len = this.len1 + this.lenRest;
            final int realLen = len - this.offset;
            if(realLen == 0) return Vector.empty();
            else if(len < 0) throw new IndexOutOfBoundsException("Vector cannot have negative size " + len);
            else if(len <= WIDTH) {
                return new Vector1<>(copyIfDifferentSize(this.a1, realLen));
            } else if(len <= WIDTH2) {
                final int i1 = (len - 1) & MASK;
                final int i2 = (len - 1) >>> BITS;
                final Object[][] data = copyOfRange(this.a2, 1, i2);
                final Object[] prefix1 = this.a2[0];
                final Object[] suffix1 = copyIfDifferentSize(this.a2[i2], i1 + 1);
                return new Vector2<>(prefix1, WIDTH - this.offset, data, suffix1, realLen);
            } else if(len <= WIDTH3) {
                final int i1 = (len - 1) & MASK;
                final int i2 = ((len - 1) >>> BITS) & MASK;
                final int i3 = (len - 1) >>> BITS2;
                final Object[][][] data = copyOfRange(this.a3, 1, i3);
                final Object[][] prefix2 = copyTail(this.a3[0]);
                final Object[] prefix1 = this.a3[0][0];
                final Object[][] suffix2 = copyOf(this.a3[i3], i2);
                final Object[] suffix1 = copyIfDifferentSize(this.a3[i3][i2], i1 + 1);
                final int len1 = prefix1.length;
                final int len12 = len1 + prefix2.length * WIDTH;
                return new Vector3<>(prefix1, len1, prefix2, len12, data, suffix2, suffix1, realLen);
            } else if(len <= WIDTH4) {
                final int i1 = (len - 1) & MASK;
                final int i2 = ((len - 1) >>> BITS) & MASK;
                final int i3 = ((len - 1) >>> BITS2) & MASK;
                final int i4 = (len - 1) >>> BITS3;
                final Object[][][][] data = copyOfRange(this.a4, 1, i4);
                final Object[][][] prefix3 = copyTail(this.a4[0]);
                final Object[][] prefix2 = copyTail(this.a4[0][0]);
                final Object[] prefix1 = this.a4[0][0][0];
                final Object[][][] suffix3 = copyOf(this.a4[i4], i3);
                final Object[][] suffix2 = copyOf(this.a4[i4][i3], i2);
                final Object[] suffix1 = copyIfDifferentSize(this.a4[i4][i3][i2], i1 + 1);
                final int len1 = prefix1.length;
                final int len12 = len1 + prefix2.length * WIDTH;
                final int len123 = len12 + prefix3.length * WIDTH2;
                return new Vector4<>(
                        prefix1, len1, prefix2, len12, prefix3, len123, data, suffix3, suffix2, suffix1, realLen
                );
            } else if(len <= WIDTH5) {
                final int i1 = (len - 1) & MASK;
                final int i2 = ((len - 1) >>> BITS) & MASK;
                final int i3 = ((len - 1) >>> BITS2) & MASK;
                final int i4 = ((len - 1) >>> BITS3) & MASK;
                final int i5 = (len - 1) >>> BITS4;
                final Object[][][][][] data = copyOfRange(this.a5, 1, i5);
                final Object[][][][] prefix4 = copyTail(this.a5[0]);
                final Object[][][] prefix3 = copyTail(this.a5[0][0]);
                final Object[][] prefix2 = copyTail(this.a5[0][0][0]);
                final Object[] prefix1 = this.a5[0][0][0][0];
                final Object[][][][] suffix4 = copyOf(this.a5[i5], i4);
                final Object[][][] suffix3 = copyOf(this.a5[i5][i4], i3);
                final Object[][] suffix2 = copyOf(this.a5[i5][i4][i3], i2);
                final Object[] suffix1 = copyIfDifferentSize(a5[i5][i4][i3][i2], i1 + 1);
                final int len1 = prefix1.length;
                final int len12 = len1 + prefix2.length * WIDTH;
                final int len123 = len12 + prefix3.length * WIDTH2;
                final int len1234 = len123 + prefix4.length * WIDTH3;
                return new Vector5<>(
                        prefix1, len1, prefix2, len12, prefix3, len123, prefix4, len1234,
                        data, suffix4, suffix3, suffix2, suffix1, realLen
                );
            } else {
                final int i1 = (len - 1) & MASK;
                final int i2 = ((len - 1) >>> BITS) & MASK;
                final int i3 = ((len - 1) >>> BITS2) & MASK;
                final int i4 = ((len - 1) >>> BITS3) & MASK;
                final int i5 = ((len - 1) >>> BITS4) & MASK;
                final int i6 = (len - 1) >>> BITS5;
                final Object[][][][][][] data = copyOfRange(this.a6, 1, i6);
                final Object[][][][][] prefix5 = copyTail(this.a6[0]);
                final Object[][][][] prefix4 = copyTail(this.a6[0][0]);
                final Object[][][] prefix3 = copyTail(this.a6[0][0][0]);
                final Object[][] prefix2 = copyTail(this.a6[0][0][0][0]);
                final Object[] prefix1 = this.a6[0][0][0][0][0];
                final Object[][][][][] suffix5 = copyOf(this.a6[i6], i5);
                final Object[][][][] suffix4 = copyOf(this.a6[i6][i5], i4);
                final Object[][][] suffix3 = copyOf(this.a6[i6][i5][i4], i3);
                final Object[][] suffix2 = copyOf(this.a6[i6][i5][i4][i3], i2);
                final Object[] suffix1 = copyIfDifferentSize(this.a6[i6][i5][i4][i3][i2], i1 + 1);
                final int len1 = prefix1.length;
                final int len12 = len1 + prefix2.length * WIDTH;
                final int len123 = len12 + prefix3.length * WIDTH2;
                final int len1234 = len123 + prefix4.length * WIDTH3;
                final int len12345 = len1234 + prefix5.length * WIDTH4;
                return new Vector6<>(
                        prefix1, len1, prefix2, len12, prefix3, len123, prefix4, len1234,
                        prefix5, len12345, data, suffix5, suffix4, suffix3, suffix2, suffix1,
                        realLen
                );
            }
        }

    }

    private static final class NewVectorIterator<A> implements ListIterator<A> {

        private final Vector<A> v;
        private int totalLength;
        private final int sliceCount;

        private Object[] a1;
        private Object[][] a2;
        private Object[][][] a3;
        private Object[][][][] a4;
        private Object[][][][][] a5;
        private Object[][][][][][] a6;
        private int a1len;
        private int i1;
        private int oldPos;
        private int len1;

        private int sliceIdx;
        private int sliceDim;
        private int sliceStart;
        private int sliceEnd;

        public NewVectorIterator(Vector<A> v, int totalLength, int sliceCount) {
            this.v = v;
            this.totalLength = totalLength;
            this.sliceCount = sliceCount;

            this.a1 = v.prefix1;
            this.a2 = null;
            this.a3 = null;
            this.a4 = null;
            this.a5 = null;
            this.a6 = null;
            this.a1len = this.a1.length;
            this.i1 = 0;
            this.oldPos = 0;
            this.len1 = this.totalLength;

            this.sliceIdx = 0;
            this.sliceDim = 1;
            this.sliceStart = 0;
            this.sliceEnd = this.a1len;
        }

        @Override
        public boolean hasNext() {
            return this.len1 > this.i1;
        }

        @Override
        public A next() {
            if(this.i1 == this.a1len) this.advance();

            final Object r = this.a1[this.i1];
            this.i1++;
            return (A) r;
        }

        private void advanceSlice() {
            if(!this.hasNext()) Collections.emptyIterator().next();

            this.sliceIdx++;
            Object[] slice = v.vectorSlice(this.sliceIdx);
            while(slice.length == 0) {
                this.sliceIdx++;
                slice = v.vectorSlice(this.sliceIdx);
            }

            this.sliceStart = this.sliceEnd;
            this.sliceDim = vectorSliceDim(this.sliceCount, this.sliceIdx);
            switch(this.sliceDim) {
                case 1 -> this.a1 = (Object[]) slice;
                case 2 -> this.a2 = (Object[][]) slice;
                case 3 -> this.a3 = (Object[][][]) slice;
                case 4 -> this.a4 = (Object[][][][]) slice;
                case 5 -> this.a5 = (Object[][][][][]) slice;
                case 6 -> this.a6 = (Object[][][][][][]) slice;
            }
            this.sliceEnd = this.sliceStart + slice.length * (1 << (BITS * (this.sliceDim - 1)));
            if(this.sliceEnd > this.totalLength) this.sliceEnd = this.totalLength;
            if(this.sliceDim > 1) this.oldPos = (1 << (BITS * this.sliceDim)) - 1;
        }

        private void advance() {
            final int pos = this.i1 - this.len1 + this.totalLength;
            if(pos == sliceEnd) this.advanceSlice();
            if(this.sliceDim > 1) {
                final int io = pos - this.sliceStart;
                final int xor = this.oldPos ^ io;
                this.advanceA(io, xor);
                this.oldPos = io;
            }
            this.len1 -= this.i1;
            this.a1len = Math.min(this.a1.length, this.len1);
            this.i1 = 0;
        }

        private void advanceA(int io, int xor) {
            if(xor < WIDTH2) {
                this.a1 = this.a2[(io >>> BITS) & MASK];
            } else if(xor < WIDTH3) {
                this.a2 = this.a3[(io >>> BITS2) & MASK];
                this.a1 = this.a2[0];
            } else if(xor < WIDTH4) {
                this.a3 = this.a4[(io >>> BITS3) & MASK];
                this.a2 = this.a3[0];
                this.a1 = this.a2[0];
            } else if(xor < WIDTH5) {
                this.a4 = this.a5[(io >>> BITS4) & MASK];
                this.a3 = this.a4[0];
                this.a2 = this.a3[0];
                this.a1 = this.a2[0];
            } else {
                this.a5 = this.a6[io >>> BITS5];
                this.a4 = this.a5[0];
                this.a3 = this.a4[0];
                this.a2 = this.a3[0];
                this.a1 = this.a2[0];
            }
        }

        private void setA(int io, int xor) {
            if(xor < WIDTH2) {
                this.a1 = this.a2[(io >>> BITS) & MASK];
            } else if(xor < WIDTH3) {
                this.a2 = this.a3[(io >>> BITS2) & MASK];
                this.a1 = this.a2[(io >>> BITS) & MASK];
            } else if(xor < WIDTH4) {
                this.a3 = this.a4[(io >>> BITS3) & MASK];
                this.a2 = this.a3[(io >>> BITS2) & MASK];
                this.a1 = this.a2[(io >>> BITS) & MASK];
            } else if(xor < WIDTH5) {
                this.a4 = this.a5[(io >>> BITS4) & MASK];
                this.a3 = this.a4[(io >>> BITS3) & MASK];
                this.a2 = this.a3[(io >>> BITS2) & MASK];
                this.a1 = this.a2[(io >>> BITS) & MASK];
            } else {
                this.a5 = this.a6[io >>> BITS5];
                this.a4 = this.a5[(io >>> BITS4) & MASK];
                this.a3 = this.a4[(io >>> BITS3) & MASK];
                this.a2 = this.a3[(io >>> BITS2) & MASK];
                this.a1 = this.a2[(io >>> BITS) & MASK];
            }
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public boolean hasPrevious() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public A previous() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Immutable collection");
        }

        @Override
        public void set(A a) {
            throw new UnsupportedOperationException("Immutable collection");
        }

        @Override
        public void add(A a) {
            throw new UnsupportedOperationException("Immutable collection");
        }
    }

}
