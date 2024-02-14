package org.minerift.ether.database.sql;

import com.google.common.collect.Sets;

import java.util.*;
import java.util.stream.Collectors;

import static org.minerift.ether.util.Utils.bool2Int;

// Provides differences between key sets
// Used for comparing local and db data / different versions of data
public class KeyDiff {

    /*
    *   - 2 bits (00)
    *   - least significant bit is old set
    *   - most significant bit is new set
    *
    *   - NO EXISTS -> N/A      -> 0 (00)
    *   - ONLY OLD  -> DELETED  -> 1 (01)
    *   - ONLY NEW  -> INSERTED -> 2 (10)
    *   - BOTH SETS -> UPDATED  -> 3 (11)
    * */

    private static final DiffType[] TYPES = DiffType.values();

    public static void main(String[] args) {

        Set<Integer> oldSet = Set.of(0, 1, 2, 4, 7, 10);
        Set<Integer> newSet = Set.of(0, 2, 3, 4, 7, 8);

        //Set<Integer> oldSet = randomNumSet(40, 0, 50);
        //Set<Integer> newSet = randomNumSet(40, 0, 50);

        Diff<Integer>[] diffs = getDiffs(oldSet, newSet);

        //System.out.println(Arrays.deepToString(diffs));
        System.out.println(partitionDiffs(oldSet, newSet));
    }

    private KeyDiff() {}

    // TODO: remove after testing
    private static Set<Integer> randomNumSet(int count, int minInclusive, int maxExclusive) {
        return new Random().ints(count, minInclusive, maxExclusive).boxed().collect(Collectors.toSet());
    }

    private static DiffType getDiffType(boolean inOld, boolean inNew) {
        //return TYPES[bool2Int(inOld) | (bool2Int(inNew) << 1)]; // this handles it properly
        return TYPES[bool2Int(inOld) + (bool2Int(inNew) << 1)]; // hacky, but has the same effect
    }

    public static <T> Diff<T>[] getDiffs(Set<T> oldSet, Set<T> newSet) {
        Set<T> allView = Sets.union(oldSet, newSet);
        Diff<T>[] diffs = new Diff[allView.size()];

        int i = 0;
        for(T obj : allView) {
            DiffType type = getDiffType(oldSet.contains(obj), newSet.contains(obj));
            diffs[i] = new Diff<>(obj, type);
            i++;
        }
        return diffs;
    }

    public static <T> Map<DiffType, List<T>> partitionDiffs(Set<T> oldSet, Set<T> newSet) {
        Set<T> allView = Sets.union(oldSet, newSet);
        EnumMap<DiffType, List<T>> diffs = new EnumMap<>(DiffType.class);

        for(T obj : allView) {
            DiffType type = getDiffType(oldSet.contains(obj), newSet.contains(obj));
            List<T> typeObjs = diffs.computeIfAbsent(type, (ignore) -> new ArrayList<>());
            typeObjs.add(obj);
        }
        return diffs;
    }

    @Deprecated
    public static <T> Diff<T>[] getDiffsAlternate(Set<T> oldSet, Set<T> newSet) {
        Set<T> allView = Sets.union(oldSet, newSet);
        Diff<T>[] diffs = new Diff[allView.size()];

        int i = 0;
        for(T obj : allView) {
            boolean inOld = oldSet.contains(obj);
            boolean inNew = newSet.contains(obj);
            if(inOld && inNew) {
                diffs[i] = new Diff<>(obj, DiffType.UPDATED);
            } else if(inOld) {
                diffs[i] = new Diff<>(obj, DiffType.DELETED);
            } else if(inNew) {
                diffs[i] = new Diff<>(obj, DiffType.INSERTED);
            } else {
                diffs[i] = new Diff<>(obj, DiffType.NOT_EXISTS);
            }
            i++;
        }

        return diffs;
    }

    public static class Diff<T> {
        public final T obj;
        public final DiffType diff;

        protected Diff(T obj, DiffType diff) {
            this.obj = obj;
            this.diff = diff;
        }

        @Override
        public String toString() {
            return "[ obj: " + obj + ", diff: " + diff + " ]";
        }
    }

    public enum DiffType {
        NOT_EXISTS,     // 00, if element doesn't exist in either set
        DELETED,        // 01, if element is in old set, but not new set
        INSERTED,       // 10, if element is in new set, but not old set
        UPDATED,        // 11, if element is in both sets
    }
}
