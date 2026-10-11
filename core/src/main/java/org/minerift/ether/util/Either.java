package org.minerift.ether.util;

import com.google.common.base.Preconditions;
import org.minerift.ether.debug.NeedsReview;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Either<L, R> {

    private final Object obj;
    private final boolean isLeft;

    public static <L, R> Either<L, R> left(L left) {
        return new Either<>(left, true);
    }

    public static <L, R> Either<L, R> right(R right) {
        return new Either<>(right, false);
    }

    private Either(Object obj, boolean isLeft) {
        this.obj = obj;
        this.isLeft = isLeft;
    }

    public boolean isLeft() {
        return isLeft;
    }

    public boolean isRight() {
        return !isLeft;
    }

    public <U> U map(Function<L, U> lmapper, Function<R, U> rmapper) {
        return isLeft
                ? lmapper.apply(getLeft())
                : rmapper.apply(getRight());
    }

    public <E extends Exception> L getLeftOrThrow(Supplier<E> ex) throws E {
        if(!isLeft) {
            throw ex.get();
        }
        return (L) obj;
    }

    public <E extends Exception> R getRightOrThrow(Supplier<E> ex) throws E {
        if(isLeft) {
            throw ex.get();
        }
        return (R) obj;
    }

    @NeedsReview
    public void runIfLeft(boolean b, Consumer<L> run) {
        if(b) {
            run.accept(getLeft());
        }
    }

    @NeedsReview
    public void runIfLeft(Consumer<L> run) {
        runIfLeft(isLeft(), run);
    }

    @NeedsReview
    public void runIfRight(boolean b, Consumer<R> run) {
        if(b) {
            run.accept(getRight());
        }
    }

    @NeedsReview
    public void runIfRight(Consumer<R> run) {
        runIfRight(isRight(), run);
    }

    @NeedsReview
    public void runIfLeftOrElse(Consumer<L> con, Runnable run) {
        if(isLeft()) {
            con.accept(getLeft());
        } else {
            run.run();
        }
    }

    @NeedsReview
    public void runIfRightOrElse(Consumer<R> con, Runnable run) {
        if(isRight()) {
            con.accept(getRight());
        } else {
            run.run();
        }
    }

    public L getLeft() {
        Preconditions.checkState(isLeft, "Attempted to get left side for right-sided Either");
        return (L) obj;
    }

    public R getRight() {
        Preconditions.checkState(!isLeft, "Attempted to get right side for left-sided Either");
        return (R) obj;
    }

}
