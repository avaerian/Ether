package org.minerift.ether.nms.world;

import org.minerift.ether.nms.world.block.BlockState;

public class ChunkSectionChanges {

    public final Chunk chunk;
    public final short[] positions;
    public final BlockState<?>[] states;
    private int index;

    public ChunkSectionChanges(Chunk chunk, int blockCount) {
        this.chunk = chunk;
        this.positions = new short[blockCount];
        this.states = new BlockState[blockCount];
        this.index = 0;
    }

    public void add(int x, int y, int z, BlockState<?> state) {
        /*if(index >= positions.length) {
            // throw exception
        }*/
        positions[index] = Section.sectionRelativePos(x, y, z);
        states[index++] = state;
    }
}
