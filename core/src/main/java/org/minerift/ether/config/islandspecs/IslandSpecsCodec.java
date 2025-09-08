package org.minerift.ether.config.islandspecs;

import com.google.common.base.Preconditions;
import org.minerift.ether.Secrets;
import org.minerift.ether.config.ConfigCodec;
import org.minerift.ether.config.ConfigFileReadException;
import org.minerift.ether.config.ConfigFileWriteException;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.nbt.*;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.container.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.minerift.ether.util.Utils.ensure;
import static org.minerift.ether.util.nbt.tags.TagTypes.STRING;

public class IslandSpecsCodec extends ConfigCodec<IslandSpecsConfig> {

    public static final IslandSpecsCodec CODEC = new IslandSpecsCodec();

    private IslandSpecsCodec() {
        super(TYPE_DIR | NO_FLAGS);
    }

    @Override
    protected IslandSpecsConfig readIt(File dir) throws ConfigFileReadException {
        Preconditions.checkArgument(dir.isDirectory(), dir.getName() + " must be a directory");
        Preconditions.checkArgument(dir.exists(), dir.getName() + " does not exist!");
        IslandSpecsConfig config = new IslandSpecsConfig();

        //  parallelize by queuing tasks and waiting for all tasks to complete?
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), "*.spec")) {
            for(Path p : stream) {

                try {
                    File specFile = p.toFile();
                    IslandSpec spec = new IslandSpec();

                    NbtReader reader = NbtReader.from(specFile, Compression.GZIP);
                    CompoundTag root = (CompoundTag) reader.readNextTag();

                    spec.setIslandName(root.getString("IslandName"));
                    spec.setDescription(root.getList("IslandDesc", StringTag.class).unwrapTags(StringTag::getStrVal));
                    spec.setIconData(root.getString("IslandIcon")); // snbt data

                    CompoundTag iconData = (CompoundTag) Snbt.readTag(spec.getIconData()); // DEBUG; just messing around

                    int[] loc = root.getIntArray("IslandPlayerSpawn");
                    ensure(loc.length == 3, () -> new ConfigFileReadException("IslandPlayerSpawn must only be XYZ"));

                    spec.setDefaultSpawnLoc(new Vec3i(loc));

                    config.add(spec);
                } catch (NbtException e) {
                    // TODO: logger
                    // skip this file and log
                    System.out.printf("Failed to load %s (%s): %s\n", p.getFileName(), p, e.getMessage()); // consider e.getLocalizedMessage()
                }
            }
        } catch (IOException ex) {
            throw new ConfigFileReadException(ex);
        }

        return config;
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

        IslandSpecsCodec writer = new IslandSpecsCodec();
        writer.writeIt(config, Secrets.LOCAL_ISLAND_SPECS_DIR.transform(File::new));

    }

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
            tag.addTag(new ListTag<>("IslandDesc", STRING, desc));
            tag.addTag(new IntArrayTag("IslandPlayerSpawn", spec.getDefaultSpawnLoc().getXYZ()));

            writer.writeTag(tag);

            /* DEBUG START */
            System.out.println(writer);
            System.out.println(Arrays.toString(writer.buffer.array()));
            CompoundTag test;
            try {
                test = (CompoundTag) new NbtReader(writer.buffer.flip(), true).readNextTag();
                System.out.println(test);
            } catch (NbtReadException e) {
                System.out.println("DEBUG: Failed to read nbt buffer");
                e.printStackTrace();
            }
            /* DEBUG END */
        }

    }
}
