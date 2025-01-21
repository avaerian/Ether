package org.minerift.ether.database.nubin;

public interface Region<T extends Region<T>> {

    T read(Chunk chunk);
    int write(Chunk chunk);

}
