package org.minerift.ether.schematic.types;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.pasters.WESchematicPaster;
import org.minerift.ether.util.math.Vec3i;

public class WorldEditSchematic implements Schematic {

    private final Clipboard clipboard;

    public WorldEditSchematic(Clipboard clipboard) {
        this.clipboard = clipboard;
    }

    public Clipboard getClipboard() {
        return clipboard;
    }

    @Override
    public SchematicType getType() {
        return SchematicType.WORLDEDIT;
    }

    @Override
    public void paste(Vec3i pos, String worldName, SchematicPasteOptions options) {
        getType().getPaster(WESchematicPaster.class).paste(this, pos, worldName, options);
    }

    @Override
    public int getWidth() {
        return clipboard.getDimensions().getX();
    }

    @Override
    public int getHeight() {
        return clipboard.getDimensions().getY();
    }

    @Override
    public int getLength() {
        return clipboard.getDimensions().getZ();
    }

    @Override
    public Vec3i getOffset() {
        final BlockVector3 offset = clipboard.getOrigin();
        return new Vec3i(offset.getX(), offset.getY(), offset.getZ());
    }
}
