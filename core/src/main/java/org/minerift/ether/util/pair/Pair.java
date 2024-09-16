package org.minerift.ether.util.pair;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Pair<F, S> {

    protected final F first;
    protected final S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }

    public Pair(Object[] pair) {
        Preconditions.checkArgument(pair.length == 2, "Must only be 2 elements in pair!");
        this.first = (F) pair[0];
        this.second = (S) pair[1];
    }

    public F getFirst() {
        return first;
    }

    public S getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;
        return Objects.equal(first, pair.first) && Objects.equal(second, pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(first, second);
    }
}
