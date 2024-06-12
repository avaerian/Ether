package org.minerift.ether.util;

public interface Array3DAccessor<T> {

    static <T> Array3DAccessor<T> xyz() {
        return new Array3DAccessor<>() {
            @Override
            public T get(T[][][] array, int x, int y, int z) {
                return array[x][y][z];
            }

            @Override
            public void set(T[][][] array, T obj, int x, int y, int z) {
                array[x][y][z] = obj;
            }
        };
    }

    static <T> Array3DAccessor<T> yzx() {
        return new Array3DAccessor<>() {
            @Override
            public T get(T[][][] array, int x, int y, int z) {
                return array[y][z][x];
            }

            @Override
            public void set(T[][][] array, T obj, int x, int y, int z) {
                array[y][z][x] = obj;
            }
        };
    }

    static <T> Array3DAccessor<T> zyx() {
        return new Array3DAccessor<>() {
            @Override
            public T get(T[][][] array, int x, int y, int z) {
                return array[z][y][x];
            }

            @Override
            public void set(T[][][] array, T obj, int x, int y, int z) {
                array[z][y][x] = obj;
            }
        };
    }

    T get(T[][][] array, int x, int y, int z);
    void set(T[][][] array, T obj, int x, int y, int z);

}
