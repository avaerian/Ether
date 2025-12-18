package org.minerift.ether.schematic;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.transform.Transforms;
import org.minerift.ether.util.nbt.NbtSerializable;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.io.File;
import java.io.IOException;

public interface Schematic extends NbtSerializable {

    static <S extends Schematic> S fromFile(SchematicType<S> type, File file) throws SchematicReadException {
        Preconditions.checkNotNull(file, "File cannot be null");
        return type.codec().read(file);
    }

    static Schematic fromFile(File file) throws SchematicReadException {
        /*final SchematicType type = Ether.isUsingWorldEdit()
                ? SchematicType.WORLDEDIT
                : SchematicType.SPONGE;*/
        SchematicType type = Ether.inst().getConfig(ConfigType.MAIN).getDefaultSchemType();
        return fromFile(type, file);
    }

    static <S extends Schematic> S fromTyped(SchematicType<S> type, ByteBuf buf) throws SchematicReadException {
        return type.codec().read(buf);
    }

    static Schematic from(ByteBuf buf) throws SchematicReadException {
        SchematicType type = Ether.inst().getConfig(ConfigType.MAIN).getDefaultSchemType();
        return fromTyped(type, buf);
    }

    static <S extends Schematic> S fromTyped(SchematicType<S> type, CompoundTag nbt) throws SchematicReadException {
        return type.codec().read(nbt);
    }

    static Schematic from(CompoundTag nbt) throws SchematicReadException {
        SchematicType type = Ether.inst().getConfig(ConfigType.MAIN).getDefaultSchemType();
        return fromTyped(type, nbt);
    }

    SchematicType type();

    default SchematicCodec codec() {
        return type().codec();
    }

    default int write(ByteBuf buf) {
        return codec().write(this, buf);
    }

    default int write(ByteBuf buf, int flags) {
        return codec().write(this, buf, flags);
    }

    default int write(File f) throws IOException {
        return codec().write(this, f);
    }

    default int write(File f, int flags) throws IOException {
        return codec().write(this, f, flags);
    }

    default CompoundTag serializeNbt() {
        return codec().writeAsNbt(this, SchematicCodec.NO_FLAGS);
    }

    default CompoundTag writeAsNbt(int flags) {
        return codec().writeAsNbt(this, flags);
    }

    void paste(Vec3i pos, String worldName, SchematicPasteOptions options);

    int getWidth();
    int getHeight();
    int getLength();

    Vec3i getDimensions();
    Vec3i getOffset();

    Schematic transform(Transforms ts);
    Schematic transformMut(Transforms ts);

}
