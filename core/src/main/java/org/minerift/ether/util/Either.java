package org.minerift.ether.util;

import com.google.common.base.Preconditions;

import java.util.function.Consumer;

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

    // TODO: review
    public void runIfLeft(boolean b, Consumer<L> run) {
        if(b) {
            run.accept(getLeft());
        }
    }

    public void runIfLeft(Consumer<L> run) {
        runIfLeft(isLeft(), run);
    }

    // TODO: review
    public void runIfRight(boolean b, Consumer<R> run) {
        if(b) {
            run.accept(getRight());
        }
    }

    public void runIfRight(Consumer<R> run) {
        runIfRight(isRight(), run);
    }

    public void runIfLeftOrElse(Consumer<L> con, Runnable run) {
        if(isLeft()) {
            con.accept(getLeft());
        } else {
            run.run();
        }
    }

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
