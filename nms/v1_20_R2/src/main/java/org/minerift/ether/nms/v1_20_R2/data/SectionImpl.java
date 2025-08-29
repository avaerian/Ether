package org.minerift.ether.nms.v1_20_R2.data;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.nms.v1_20_R2.NativeTypeConversionsImpl;
import org.minerift.ether.nms.v1_20_R2.ReflectionMappings;
import org.minerift.ether.nms.world.ChunkSectionChanges;
import org.minerift.ether.nms.world.Section;

public class SectionImpl implements Section<BlockState, LevelChunk, LevelChunkSection, Holder<Biome>> {

    public final LevelChunkSection section;
    public final int index;

    public SectionImpl(LevelChunkSection section, int index) {
        this.section = section;
        this.index = index;
    }

    @Override
    public void acquire() {
        section.acquire();
    }

    @Override
    public void release() {
        section.release();
    }

    @Override
    public BlockState getNativeBlockState(int x, int y, int z) {
        return section.getBlockState(x, y, z);
    }

    @Override
    public BlockState setBlockState(int x, int y, int z, BlockState state) {
        return section.setBlockState(x, y, z, state);
    }

    @Override
    public Holder<Biome> getNativeBiome(int biomeX, int biomeY, int biomeZ) {
        return section.getNoiseBiome(biomeX, biomeY, biomeZ);
    }

    @Override
    public void setBiome(int biomeX, int biomeY, int biomeZ, Holder<Biome> biome) {
        section.setBiome(biomeX, biomeY, biomeZ, biome);
    }

    @Deprecated
    @NeedsTesting
    @Override
    public void updateSectionChanges(int sectionIndex, ChunkSectionChanges changes) {
        LevelChunk nativeChunk = (LevelChunk) changes.chunk.asNative();
        ChunkHolder nativeChunkHolder = nativeChunk.playerChunk;

        ReflectionMappings.addSectionBlockChanges(nativeChunkHolder, sectionIndex, changes.positions);
        ReflectionMappings.setSectionsHaveChanged(nativeChunkHolder, true);
    }

    private ClientboundSectionBlocksUpdatePacket getPacket(ChunkSectionChanges changes) {

        final int minHeight = changes.chunk.getWorld().getMinHeight();
        SectionPos sectionPos = SectionPos.of(changes.chunk.getX(),
                Section.sectionRealFromIdx(index, minHeight), changes.chunk.getZ());

        ShortSet positions = new ShortArraySet(changes.positions);
        BlockState[] nativeStates = new BlockState[changes.states.length];
        for(int i = 0; i < changes.states.length; i++) {
            nativeStates[i] = (BlockState) changes.states[i].asNative();
        }
        return new ClientboundSectionBlocksUpdatePacket(sectionPos, positions, nativeStates);
    }

    @Override
    public void sendSectionUpdatesPacket(Player plr, ChunkSectionChanges changes, boolean modifyBlocks) {
        ClientboundSectionBlocksUpdatePacket packet = getPacket(changes);
        ((CraftPlayer)plr).getHandle().connection.send(packet);
    }

    @Override
    public void broadcastSectionUpdatesPacket(ChunkSectionChanges changes, boolean modifyBlocks) {
        //System.out.println("Broadcasting section updates packet");
        ClientboundSectionBlocksUpdatePacket packet = getPacket(changes);
        LevelChunk chunk = ((LevelChunk)changes.chunk.asNative());

        /*List<ServerPlayer> playersToSend = chunk.getChunkHolder()
                .vanillaChunkHolder
                .playerProvider
                .getPlayers(new ChunkPos(changes.chunk.getX(), changes.chunk.getZ()), false);

        playersToSend.forEach(plr -> plr.connection.send(packet));*/
        chunk.getChunkHolder().vanillaChunkHolder.broadcast(packet, false);
    }

    @Override
    public LevelChunkSection asNative() {
        return section;
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }

    @Override
    public boolean hasOnlyAir() {
        return section.hasOnlyAir();
    }

    @Override
    public boolean isRandomlyTicking() {
        return section.isRandomlyTicking();
    }

    @Override
    public boolean isRandomlyTickingBlocks() {
        return section.isRandomlyTickingBlocks();
    }

    @Override
    public boolean isRandomlyTickingFluids() {
        return section.isRandomlyTickingFluids();
    }

    @Override
    public int getSpecialCollidingBlocks() {
        return section.getSpecialCollidingBlocks();
    }

    @Override
    public int bottomBlockY() {
        return (index << 4);
    }
}
