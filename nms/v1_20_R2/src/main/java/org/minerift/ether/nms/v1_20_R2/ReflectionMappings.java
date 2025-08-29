package org.minerift.ether.nms.v1_20_R2;

import it.unimi.dsi.fastutil.shorts.ShortArraySet;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.server.level.ChunkHolder;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.NeedsTesting;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public final class ReflectionMappings {

    public static final String FROZEN_REGISTRY_FIELD_NAME;
    public static final String HAS_SECTIONS_CHANGED_FIELD_NAME;
    public static final String CHANGED_BLOCKS_PER_SECTION_FIELD_NAME;

    public static final VarHandle HAS_SECTIONS_CHANGED;
    public static final VarHandle CHANGED_BLOCKS_PER_SECTION;
    public static final VarHandle CHANGED_BLOCKS_PER_SECTION_ARRAY_TYPE;

    static {
        ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
        FROZEN_REGISTRY_FIELD_NAME = remapper.remapFieldName(MappedRegistry.class, "frozen");

        // Chunk + section field remappings
        HAS_SECTIONS_CHANGED_FIELD_NAME = remapper.remapFieldName(ChunkHolder.class, "hasChangedSections");
        CHANGED_BLOCKS_PER_SECTION_FIELD_NAME = remapper.remapFieldName(ChunkHolder.class, "changedBlocksPerSection");

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            MethodHandles.Lookup chunkLookup = MethodHandles.privateLookupIn(ChunkHolder.class, lookup);
            HAS_SECTIONS_CHANGED = chunkLookup
                    .findVarHandle(ChunkHolder.class, HAS_SECTIONS_CHANGED_FIELD_NAME, boolean.class);
            CHANGED_BLOCKS_PER_SECTION = chunkLookup
                    .findVarHandle(ChunkHolder.class, CHANGED_BLOCKS_PER_SECTION_FIELD_NAME, ShortSet[].class);
            CHANGED_BLOCKS_PER_SECTION_ARRAY_TYPE = MethodHandles.arrayElementVarHandle(ShortSet[].class);


        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // REVIEW: remap changedBlocksPerSection and use to update blocks properly for chunk
    }

    @NeedsTesting
    @Experimental
    public static void setSectionsHaveChanged(ChunkHolder chunk, boolean haveSectionsChanged) {
        HAS_SECTIONS_CHANGED.set(chunk, haveSectionsChanged);
    }

    @NeedsTesting
    @Experimental
    public static void addSectionBlockChanges(ChunkHolder chunk, int sectionIndex, short[] blocksChanged) {
        ShortSet[] sectionChanges = (ShortSet[]) CHANGED_BLOCKS_PER_SECTION.get(chunk);
        if(sectionChanges[sectionIndex] == null) {
            setSectionBlockChanges(chunk, sectionIndex, new ShortOpenHashSet(blocksChanged));
        } else {
            sectionChanges[sectionIndex].addAll(new ShortArraySet(blocksChanged));
        }
    }

    @NeedsTesting
    @Experimental
    public static void setSectionBlockChanges(ChunkHolder chunk, int sectionIndex, ShortSet blockChanges) {
        CHANGED_BLOCKS_PER_SECTION_ARRAY_TYPE.set(
                (ShortSet[])CHANGED_BLOCKS_PER_SECTION.get(chunk),
                sectionIndex, blockChanges);
    }

    private ReflectionMappings() {}
}
