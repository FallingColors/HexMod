package at.petrak.hexcasting.api.misc;

import com.mojang.datafixers.util.Unit;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * I'm sick and tired of not having a result class god dammit
 */
public abstract sealed class Result<T, E> {
    public static final class Ok<T, E> extends Result<T, E> {
        public final T ok;

        public Ok(T ok) {
            this.ok = ok;
        }
    }

    public static final class Err<T, E> extends Result<T, E> {
        public final E err;

        public Err(E err) {
            this.err = err;
        }
    }

    public boolean isOk() {
        return this instanceof Ok;
    }

    public boolean isErr() {
        return this instanceof Err;
    }

    public T unwrap() {
        if (this instanceof Ok<T, E> ok) {
            return ok.ok;
        } else {
            throw new IllegalStateException("tried to unwrap an Err");
        }
    }

    public E unwrapErr() {
        if (this instanceof Err<T, E> err) {
            return err.err;
        } else {
            throw new IllegalStateException("tried to unwrapErr an Ok");
        }
    }

    public <T2, E2> Result<T2, E2> match(Function<T, T2> okBranch, Function<E, E2> errBranch) {
        if (this instanceof Ok<T, E> ok) {
            return new Result.Ok<>(okBranch.apply(ok.ok));
        } else if (this instanceof Err<T, E> err) {
            return new Result.Err<>(errBranch.apply(err.err));
        } else {
            throw new IllegalStateException();
        }
    }

    public void matchVoid(Consumer<T> okBranch, Consumer<E> errBranch) {
        this.match(
            ok -> {
                okBranch.accept(ok);
                return Unit.INSTANCE;
            },
            err -> {
                errBranch.accept(err);
                return Unit.INSTANCE;
            }
        );
    }

    public <U> U collapse(Function<T, U> okBranch, Function<E, U> errBranch) {
        if (this instanceof Ok<T, E> ok) {
            return okBranch.apply(ok.ok);
        } else if (this instanceof Err<T, E> err) {
            return errBranch.apply(err.err);
        } else {
            throw new IllegalStateException();
        }
    }
}
