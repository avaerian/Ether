package org.minerift.ether.schematic;

import org.minerift.ether.math.Vec3i;

import java.util.concurrent.CompletableFuture;

public interface SchematicPaster<S extends Schematic> {
    /**
     * Pastes the schematic in the world at the specified position with some additional options
     * indicating how the schematic should be pasted. A few {@link SchematicPasteOptions}
     * configurations already exist, but can be also built using {@link SchematicPasteOptions#builder()}.
     *
     * @param schem the schematic to be pasted.
     * @param pos the position in the world to be paste at.
     * @param worldName the name of the world to paste in.
     * @param options additional options specifying how the schematic should be pasted.
     * @return future indicating when the operation has completed.
     */
    CompletableFuture<Void> paste(S schem, Vec3i pos, String worldName, SchematicPasteOptions options);
}
