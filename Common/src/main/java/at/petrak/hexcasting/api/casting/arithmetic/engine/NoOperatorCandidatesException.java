package at.petrak.hexcasting.api.casting.arithmetic.engine;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.casting.math.HexPattern;

import java.util.List;

public class NoOperatorCandidatesException extends RuntimeException {
    HexPattern pattern;
    List<Iota> args;

    public NoOperatorCandidatesException(HexPattern pattern, List<Iota> args) {
        this.pattern = pattern;
        this.args = args;
    }

    public NoOperatorCandidatesException(HexPattern pattern, List<Iota> args, String s) {
        super(s);
        this.pattern = pattern;
        this.args = args;
    }

    public HexPattern getPattern() {
        return pattern;
    }

    public HexPattern setPattern(HexPattern pattern) {
        this.pattern = pattern;
        return this.pattern;
    }

    public List<Iota> getArgs() {
        return args;
    }

    public List<Iota> setArgs(List<Iota> args) {
        this.args = args;
        return this.args;
    }
}
