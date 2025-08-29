package org.minerift.ether.config.islandspecs;

import org.minerift.ether.Secrets;
import org.minerift.ether.config.IConfigWriter;
import org.minerift.ether.config.exceptions.ConfigFileWriteException;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.nbt.NbtReader;
import org.minerift.ether.util.nbt.NbtWriter;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.TagTypes;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class IslandSpecsWriter extends IConfigWriter<IslandSpecsConfig> {
    @Override
    protected void writeIt(IslandSpecsConfig config, File dir) throws ConfigFileWriteException {

        if(!dir.exists()) {
            try {
                Files.createDirectories(dir.toPath());
            } catch (IOException ex) {
                throw new ConfigFileWriteException("Failed to create dir(s)", ex);
            }
        }

        for(IslandSpec spec : config) {
            NbtWriter writer = new NbtWriter();
            // TODO: review buffer creation when creating NbtWriter

            CompoundTag tag = new CompoundTag("Data");

            tag.addTag(new StringTag("IslandName", spec.getIslandName()));

            List<StringTag> desc = spec.getDescription().stream()
                    .map((line) -> new StringTag("", line))
                    .toList();
            tag.addTag(new ListTag<>("IslandDesc", TagTypes.STRING, desc));
            tag.addTag(new IntArrayTag("IslandPlayerSpawn", spec.getDefaultSpawnLoc().getXYZ()));

            writer.writeTag(tag);

            // Debug
            System.out.println(writer);
            System.out.println(Arrays.toString(writer.buffer.array()));
            CompoundTag test = (CompoundTag) new NbtReader(writer.buffer.flip(), true).readNextTag();
            System.out.println(test);
        }

    }

    @Debug
    public static void main(String[] args) throws ConfigFileWriteException {

        IslandSpec spec = new IslandSpec();
        spec.setIslandName("Default");
        spec.setDescription(List.of("Hello, world!", "Goodbye, world!"));
        spec.setIconData("SNBT data here or something idk");
        spec.setDefaultSpawnLoc(new Vec3i(420, 69, 1738));

        IslandSpecsConfig config = new IslandSpecsConfig();
        config.islandSpecs.add(spec);

        IslandSpecsWriter writer = new IslandSpecsWriter();
        writer.writeIt(config, Secrets.LOCAL_ISLAND_SPECS_DIR.transform(File::new));

    }
}
