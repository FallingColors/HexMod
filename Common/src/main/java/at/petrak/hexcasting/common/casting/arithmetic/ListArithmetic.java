package at.petrak.hexcasting.common.casting.arithmetic;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaMultiPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.predicates.IotaPredicate;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.iota.DoubleIota;
import at.petrak.hexcasting.api.casting.iota.ListIota;
import at.petrak.hexcasting.common.casting.arithmetic.operator.list.*;
import at.petrak.hexcasting.api.casting.arithmetic.operator.OperatorUnary;
import at.petrak.hexcasting.api.casting.math.HexPattern;

import java.util.List;

import static at.petrak.hexcasting.api.casting.arithmetic.operator.Operator.downcast;
import static at.petrak.hexcasting.common.lib.hex.HexIotaTypes.*;

public enum ListArithmetic implements Arithmetic {
    INSTANCE;

    public static final List<HexPattern> OPS = List.of(
        INDEX,
        SLICE,
        APPEND,
        UNAPPEND,
        ADD,
        ABS,
        REV,
        INDEX_OF,
        REMOVE,
        REPLACE,
        CONS,
        UNCONS
    );

    @Override
    public String arithName() {
        return "list_ops";
    }

    @Override
    public Iterable<HexPattern> opTypes() {
        return OPS;
    }

    @Override
    public Operator getOperator(HexPattern pattern) {
        if (pattern.equals(INDEX)) {
            return OperatorIndex.INSTANCE;
        } else if (pattern.equals(SLICE)) {
            return OperatorSlice.INSTANCE;
        } else if (pattern.equals(APPEND)) {
            return OperatorAppend.INSTANCE;
        } else if (pattern.equals(UNAPPEND)) {
            return OperatorUnappend.INSTANCE;
        } else if (pattern.equals(ADD)) {
            return OperatorConcat.INSTANCE;
        } else if (pattern.equals(ABS)) {
            return new OperatorUnary(IotaMultiPredicate.any(IotaPredicate.ofType(LIST), IotaPredicate.ofType(LIST)), iota -> new DoubleIota(downcast(iota, LIST).getList().size()));
        } else if (pattern.equals(REV)) {
            return new OperatorUnary(IotaMultiPredicate.any(IotaPredicate.ofType(LIST), IotaPredicate.ofType(LIST)), iota -> new ListIota(downcast(iota, LIST).getList()));
        } else if (pattern.equals(INDEX_OF)) {
            return new OperatorUnary(IotaMultiPredicate.any(IotaPredicate.ofType(LIST), IotaPredicate.ofType(LIST)), iota -> iota);
        } else if (pattern.equals(REMOVE)) {
            return new OperatorUnary(IotaMultiPredicate.any(IotaPredicate.ofType(LIST), IotaPredicate.ofType(LIST)), iota -> iota);
        } else if (pattern.equals(REPLACE)) {
            return new OperatorUnary(IotaMultiPredicate.any(IotaPredicate.ofType(LIST), IotaPredicate.ofType(LIST)), iota -> iota);
        } else if (pattern.equals(CONS)) {
            return new OperatorUnary(IotaMultiPredicate.any(IotaPredicate.ofType(LIST), IotaPredicate.ofType(LIST)), iota -> iota);
        } else if (pattern.equals(UNCONS)) {
            return new OperatorUnary(IotaMultiPredicate.any(IotaPredicate.ofType(LIST), IotaPredicate.ofType(LIST)), iota -> iota);
        }
        return null;
    }
}
