package at.petrak.hexcasting.api.casting.arithmetic.operator;


import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * A helper class for defining {@link Operator}s of one iota.
 */
public class OperatorUnary extends Operator {
	public UnaryOperator<Iota> inner;

	public OperatorUnary(IotaMultiPredicate accepts, UnaryOperator<Iota> inner) {
		super(1, accepts);
		this.inner = inner;
	}

	@Override
	public @NotNull Iterable<Iota> apply(@NotNull Iterable<Iota> iotas, @NotNull CastingEnvironment env) {
		return List.of(inner.apply(iotas.iterator().next()));
	}
}
