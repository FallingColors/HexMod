package at.petrak.hexcasting.api.casting.arithmetic;

import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class TripleIterable<A,B,C,D> implements Iterable<D> {
    private final Iterable<A> iterableA;
    private final Iterable<B> iterableB;
    private final Iterable<C> iterableC;

    private final TriFunction<A, B, C, D> map;

    public TripleIterable(Iterable<A> iterableA, Iterable<B> iterableB, Iterable<C> iterableC, TriFunction<A, B, C, D> map) {
        this.iterableA = iterableA;
        this.iterableB = iterableB;
        this.iterableC = iterableC;
        this.map = map;
    }

    @NotNull
    @Override
    public Iterator<D> iterator() {
        return new TripleIterator();
    }

    class TripleIterator implements Iterator<D> {
        private final Iterator<A> iteratorA = iterableA.iterator();
        private final Iterator<B> iteratorB = iterableB.iterator();
        private final Iterator<C> iteratorC = iterableC.iterator();

        @Override
        public boolean hasNext() {
            return iteratorA.hasNext() && iteratorB.hasNext() && iteratorC.hasNext();
        }

        @Override
        public D next() {
            return map.apply(iteratorA.next(), iteratorB.next(), iteratorC.next());
        }
    }
}
