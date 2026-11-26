package org.minerift.ether.database.nusql.query.cond;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.database.Field;

import java.util.ArrayDeque;
import java.util.Deque;

public abstract class Condition {

    public static <T> Condition eq(Field<?, T> field, @Nullable T value) {
        return new Eq<>(field, value);
    }

    public static Condition eq(Field<?, ?> field) {
        return eq(field, null);
    }

    public static <T extends Comparable<T>> Condition gt(Field<?, T> field, @NotNull T value) {
        return new Gt<>(field, value);
    }

    public static <T extends Comparable<T>> Condition gt(Field<?, T> field) {
        return new Gt<>(field, null);
    }

    public static <T extends Comparable<T>> Condition lt(Field<?, T> field, @NotNull T value) {
        return new Lt<>(field, value);
    }

    public static <T extends Comparable<T>> Condition lt(Field<?, T> field) {
        return new Lt<>(field, null);
    }

    public static <T extends Comparable<T>> Condition between(Field<?, T> field, @NotNull T lo, @NotNull T hi) {
        return new Between<>(field, lo, hi);
    }

    public static <T extends Comparable<T>> Condition between(Field<?, T> field) {
        return new Between<>(field, null, null);
    }

    public enum Type {
        AND,
        OR,
        EQ,
        GT,
        LT,
        BETWEEN,
    }

    protected Condition root; //initial condition
    protected LogicalCondition next; //linked list of logical conditions (AND, OR)

    public Condition(LogicalCondition next) {
        this.next = next;
    }

    public Condition() {
        this.root = this;
        this.next = null;
    }

    //Cmp condition stores reference to next bool condition
    //Bool (logical) condition stores references to next bool condition and right cmp condition

    public LogicalCondition and(@NotNull Condition condition) {
        this.next = new And(condition.root);
        next.root = root;
        return next;
    }

    public LogicalCondition or(@NotNull Condition condition) {
        this.next = new Or(condition.root);
        next.root = root;
        return next;
    }

    public LogicalCondition next() {
        return next;
    }

    public Condition root() {
        return root;
    }

    public abstract CharSequence getSQLPart();

    public abstract Type type();

    public boolean isLogical() {
        return false;
    }

    @Override
    public String toString() {
        if(next == null)
            return getSQLPart().toString();

        StringBuilder str = new StringBuilder(getSQLPart());
        Deque<LogicalCondition> lconds = new ArrayDeque<>();
        lconds.push(next);
        LogicalCondition lcond;
        while((lcond = lconds.peek()) != null) {
            boolean useParens = lcond.right.next != null;
            str.append(' ').append(lcond.getSQLPart()).append(' ');
            if(useParens)
                str.append('(');
            str.append(lcond.right.getSQLPart());

            if(lcond.right.next != null) { //enter new condition tree/linked list
                lconds.push(lcond.right.next);
            } else /* if(lcond.next == null) */ { //continue down linked list
                while((lcond = lconds.pop()).next == null && !lconds.isEmpty())
                    str.append(')');
                if(lcond.next == null)
                    break;
                lconds.push(lcond.next);
            }
        }
        return str.toString();
    }
}
