package at.petrak.hexcasting.api.casting.arithmetic.engine;

public class InvalidOperatorException extends RuntimeException {
    public InvalidOperatorException() {
    }

    public InvalidOperatorException(String s) {
        super(s);
    }

    public InvalidOperatorException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidOperatorException(Throwable cause) {
        super(cause);
    }
}
