package org.minerift.ether.nms.v1_20_R2;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import org.minerift.ether.world.BlockArchetype;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// TODO: refactor into core code instead of version-specific NMS impl
@Deprecated
public class ChunkSectionChanges {

    public static final int BLOCKS_PER_SECTION = 16 * 16 * 16; // 4096

    public final List<BlockArchetype> blocks; // block states to be applied in section
    public final int sectionIdx;

    public static class PacketData {
        public final ShortSet positions;
        public final BlockState[] states;

        private PacketData(ChunkSectionChanges data) {
            short[] positions = new short[data.blocks.size()];
            BlockState[] states = new BlockState[data.blocks.size()];

            int index = 0;
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (Iterator<BlockArchetype> it = data.blocks.iterator(); it.hasNext(); index++) {
                BlockArchetype block = it.next();
                mutableBlockPos.set(block.getX(), block.getY(), block.getZ());

                positions[index] = SectionPos.sectionRelativePos(mutableBlockPos);
                states[index] = (BlockState) block.getState().asNative();
            }

            this.positions = new ShortArraySet(positions);
            this.states = states;
        }
    }

    public ChunkSectionChanges(int sectionIdx, int expectedBlockCount) {
        this.blocks = new ArrayList<>(expectedBlockCount >= BLOCKS_PER_SECTION
                ? BLOCKS_PER_SECTION
                : BLOCKS_PER_SECTION / 4);
        this.sectionIdx = sectionIdx;
    }

    public PacketData computePacketData() {
        return new PacketData(this);
    }

}