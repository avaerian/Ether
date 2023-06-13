package org.minerift.ether.nms;

import it.unimi.dsi.fastutil.ints.IntIntPair;
import org.bukkit.Chunk;

import java.util.HashSet;

public interface NMSBridge {

    void fastClearChunks(HashSet<Chunk> chunks);

    //void fastSetBlock();

}
