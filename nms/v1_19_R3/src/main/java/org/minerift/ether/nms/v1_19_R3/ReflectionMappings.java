package org.minerift.ether.nms.v1_19_R3;

import net.minecraft.core.MappedRegistry;
import xyz.jpenilla.reflectionremapper.ReflectionRemapper;

public final class ReflectionMappings {

    public static final String FROZEN_REGISTRY_FIELD;

    static {
        ReflectionRemapper remapper = ReflectionRemapper.forReobfMappingsInPaperJar();
        FROZEN_REGISTRY_FIELD = remapper.remapFieldName(MappedRegistry.class, "frozen");
    }

    private ReflectionMappings() {}
}
