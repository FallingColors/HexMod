package at.petrak.hexcasting.api.casting.arithmetic.operator;

import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.iota.IotaType;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an Operator, similar to Action except that it also has a defined set of IotaTypes that it accepts, and
 * there can be multiple Operators 'assigned' to the same pattern in different Arithmetics as long as they don't have
 * overlapping matched types. (Overlapping matched types is not checked for, but will have undefined behaviour).
 */
public abstract class Operator {
	/**
	 * The number of arguments from the stack that this Operator requires; all Operators with the same pattern must have
	 * the same arity.
	 */
	public final int arity;

	/**
	 * A function that should return true if the passed list of Iotas satisfies this Operator's type constraints, and false otherwise.
	 */
	public final IotaMultiPredicate accepts;

	/**
	 * @param arity The number of arguments from the stack that this Operator requires; all Operators with the same pattern must have arity.
	 * @param accepts A function that should return true if the passed list of Iotas satisfies this Operator's type constraints, and false otherwise.
	 */
	public Operator(int arity, IotaMultiPredicate accepts) {
		this.arity = arity;
		this.accepts = accepts;
	}

	/**
	 /**
	 * The method called when this Operator is actually acting on the stack, for real.
	 * @param iotas An iterable of iotas with {@link Operator#arity} elements that satisfied {@link Operator#accepts}.
	 * @param env The casting environment, to make use of if this operator needs it.
	 * @return the iotas that this operator will return to the stack (with the first element of the returned iterable being placed deepest into the stack, and the last element on top of the stack).
	 * @throws Mishap if the Operator mishaps for any reason it will be passed up the chain.
	 */
	public abstract @NotNull Iterable<Iota> apply(@NotNull Iterable<Iota> iotas, @NotNull CastingEnvironment env) throws Mishap;

	/**
	 * A helper method to take an iota that you know is of iotaType and returning it as an iota of that type.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Iota> T downcast(Iota iota, IotaType<T> iotaType) {
		if (iota.getType() != iotaType)
			throw new IllegalStateException("Attempting to downcast " + iota + " to type: " + iotaType);
		return (T) iota;
	}
}
