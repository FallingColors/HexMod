package at.petrak.hexcasting.api.misc;

/**
 * Society if java actually had first-class function support
 */
@FunctionalInterface
public interface TriPredicate<A, B, C> {
    boolean test(A a, B b, C c);
}
