package org.minerift.ether.nms.v1_20_R2.data;

public interface AttributeCodec<T, NT extends Comparable<NT>> {
    T fromNative(NT nType);
    NT toNative(T type);
}
