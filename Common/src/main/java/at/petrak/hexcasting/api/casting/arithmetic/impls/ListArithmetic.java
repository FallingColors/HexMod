package at.petrak.hexcasting.api.casting.arithmetic.impls;

import at.petrak.hexcasting.api.casting.arithmetic.Arithmetic;
import at.petrak.hexcasting.api.casting.arithmetic.operator.Operator;
import at.petrak.hexcasting.api.casting.math.HexPattern;

import java.util.List;

public enum ListArithmetic implements Arithmetic {
    INSTANCE;

    public static final List<HexPattern> OPS = List.of(
        INDEX,
        SLICE,
        ADD,
        ABS,
        REV,
        INDEX_OF,
        REMOVE,
        REPLACE
    );

    @Override
    public String arithName() {
        return null;
    }

    @Override
    public Iterable<HexPattern> opTypes() {
        return null;
    }

    @Override
    public Operator getOperator(HexPattern pattern) {
        return null;
    }
}
