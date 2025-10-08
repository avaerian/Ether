package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.nbt.NbtOption;

@Debug
@Deprecated
public class NbtOption3 implements NbtOption {

    int bytes;
    public NbtOption3(int bytes) {
        this.bytes = bytes;
    }

}
