package org.minerift.ether.schematic.worldedit;

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.schematic.transform.Rotate;
import org.minerift.ether.schematic.transform.Transform;
import org.minerift.ether.schematic.transform.Transforms;
import org.minerift.ether.util.UnreachableException;

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
    public Vec3i getDimensions() {
        return new Vec3i(getWidth(), getHeight(), getLength());
    }

    @Override
    public Vec3i getOffset() {
        final BlockVector3 offset = clipboard.getOrigin();
        return new Vec3i(offset.getX(), offset.getY(), offset.getZ());
    }

    // TODO: needs a lot of work
    @Override
    public WorldEditSchematic transform(Transforms ts) {
        for (Transform t : ts) {
            if(t instanceof Rotate r) {
                if(r.isIdentity()) {
                    continue;
                }

                AffineTransform wt = new AffineTransform();
            } else {
                throw new IllegalStateException("unexpected transform: " + t);
            }
        }
        return this;
    }

    /*@Override // uses java 21 switch pattern
    public WorldEditSchematic transform(Transforms ts) {
        for (Transform t : ts) {
            switch (t) {
                case Rotate r -> {
                    if(r.isIdentity()) {
                        continue;
                    }

                    AffineTransform wt = new AffineTransform();
                    //switch ()
                }
                default -> throw new IllegalStateException("Unexpected value: " + t);
            }
        }

        return this;
    }*/

    @Override
    public Schematic transformMut(Transforms ts) {
        throw new UnreachableException("unimplemented");
    }
}
