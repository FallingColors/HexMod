package at.petrak.hexcasting.api.casting.arithmetic.operator;


import at.petrak.hexcasting.api.casting.arithmetic.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.iota.Iota;

import java.util.function.UnaryOperator;

public class OperatorUnary extends Operator {
	public UnaryOperator<Iota> inner;

	public OperatorUnary(IotaMultiPredicate accepts, UnaryOperator<Iota> inner) {
		super(1, accepts);
		this.inner = inner;
	}

	@Override
	public Iota apply(Iterable<Iota> iotas) {
		return inner.apply(iotas.iterator().next());
	}
}
