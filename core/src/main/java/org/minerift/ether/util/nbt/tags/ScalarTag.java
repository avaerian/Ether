package org.minerift.ether.util.nbt.tags;

public interface ScalarTag {

    byte getAsByte();
    short getAsShort();
    int getAsInt();
    long getAsLong();
    float getAsFloat();
    double getAsDouble();
    Number getAsNumber();

}
