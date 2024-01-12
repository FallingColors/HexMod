package at.petrak.hexcasting.api.casting.arithmetic.engine;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.OperationResult;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation;
import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import at.petrak.hexcasting.api.casting.mishaps.Mishap;
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughArgs;

import java.util.*;

/**
 * This is the class responsible for managing the various Arithmetics that are in use, deciding based on the current
 * stack which Operator should be called, etc.
 */
public class ArithmeticEngine {
    /**
     * Data structure for mapping the pattern that gets drawn on the stack to the list of Operators that
     * are overloading that pattern.
     * @param pattern The pattern that the caster will need to draw to cast one of these operators.
     * @param arity The number of arguments that all of these operators must consume from the stack.
     * @param operators The list of all operators that overload this pattern.
     */
    private record OpCandidates(HexPattern pattern, int arity, List<Operator> operators) {
        public void addOp(Operator next) {
            if (next.arity != arity) {
                throw new IllegalArgumentException("Operators exist of differing arity! The pattern " + pattern
                        + " already had arity " + arity + " when the operator with arity " + next.arity + ", " + next + " was added.");
            }
            operators.add(next);
        }
    }

    public final Arithmetic[] arithmetics;
    private final Map<HexPattern, OpCandidates> operators = new HashMap<>();

    /**
     * A cache mapping specific sets of Pattern, IotaType, IotaType, ..., IotaType to Operators so that the Operators don't need to be
     * queried for what types they accept every time they are used.
     */
    private final Map<HashCons, Operator> cache = new HashMap<>();

    public ArithmeticEngine(List<Arithmetic> arithmetics) {
        this.arithmetics = arithmetics.toArray(new Arithmetic[0]);
        for (var arith : arithmetics) {
            for (var op : arith.opTypes()) {
                operators.compute(op, ($, info) -> {
                    var operator = arith.getOperator(op);
                    if (info == null) {
                        info = new OpCandidates(op, operator.arity, new ArrayList<>());
                    }
                    info.addOp(operator);
                    return info;
                });
            }
        }
    }

    public Iterable<HexPattern> operatorSyms() {
        return operators.keySet();
    }

    /**
     * Runs one of the contained Operators assigned to the given pattern, modifying the passed stack of iotas.
     * @param pattern The pattern that was drawn, used to determine which operators are candidates.
     * @param env The casting environment.
     * @param image The casting image.
     * @param continuation The current continuation.
     * @return The iotas to be added to the stack.
     * @throws Mishap mishaps if invalid input to the operators is given by the caster.
     */
    public OperationResult run(HexPattern pattern, CastingEnvironment env, CastingImage image, SpellContinuation continuation) throws Mishap {
        var stackList = image.getStack();
        var stack = new Stack<Iota>();
        stack.addAll(stackList);
        var startingLength = stackList.size();

        var candidates = operators.get(pattern);
        if (candidates == null)
            throw new InvalidOperatorException("the pattern " + pattern + " is not an operator."); //
        HashCons hash = new HashCons.Pattern(pattern);
        var args = new ArrayList<Iota>(candidates.arity());
        for (var i = 0; i < candidates.arity(); i++) {
            if (stack.isEmpty()) {
                throw new MishapNotEnoughArgs(candidates.arity, startingLength);
            }
            var iota = stack.pop();
            hash = new HashCons.Pair(iota.getType(), hash);
            args.add(iota);
        }
        Collections.reverse(args);
        var op = resolveCandidates(args, hash, candidates);
        return op.operate(env, image, continuation);
    }

    private Operator resolveCandidates(List<Iota> args, HashCons hash, OpCandidates candidates) {
        return cache.computeIfAbsent(hash, $ -> {
            for (var op : candidates.operators()) {
                if (op.accepts.test(args)) {
                    return op;
                }
            }
            throw new NoOperatorCandidatesException(candidates.pattern(), args, "No implementation candidates for op " + candidates.pattern() + " on args: " + args);
        });
    }
}
