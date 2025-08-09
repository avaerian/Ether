package org.minerift.ether.config.islandspecs;

import com.google.common.base.Preconditions;
import org.minerift.ether.config.IConfigReader;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.util.nbt.Compression;
import org.minerift.ether.util.nbt.NbtReader;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.minerift.ether.util.Utils.ensure;

public class IslandSpecsReader extends IConfigReader<IslandSpecsConfig> {

    @Override
    protected IslandSpecsConfig readIt(File dir) throws ConfigFileReadException {
        Preconditions.checkArgument(dir.isDirectory(), dir.getName() + " must be a directory");
        Preconditions.checkArgument(dir.exists(), dir.getName() + " does not exist!");
        IslandSpecsConfig config = new IslandSpecsConfig();

        // TODO: parallelize by queuing tasks and waiting for all tasks to complete?
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), "*.spec")) {
            for(Path p : stream) {

                File specFile = p.toFile();
                IslandSpec spec = new IslandSpec();
                // TODO: do reading of files here

                NbtReader reader = NbtReader.from(specFile, Compression.GZIP);
                CompoundTag root = (CompoundTag) reader.readNextTag();

                spec.setIslandName(root.getString("IslandName").orElseThrow());
                spec.setDescription(root.getList("IslandDesc", StringTag.class).orElseThrow().unwrapTags(StringTag::getValue));
                spec.setIconData(root.getString("IslandIcon").orElseThrow()); // snbt data

                CompoundTag iconData = (CompoundTag) Snbt.readTag(spec.getIconData()); // DEBUG; just messing around

                int[] loc = root.getIntArray("IslandPlayerSpawn").orElseThrow();
                ensure(loc.length == 3, () -> new ConfigFileReadException("IslandPlayerSpawn must only be XYZ"));

                spec.setDefaultSpawnLoc(new Vec3i(loc));

                config.islandSpecs.add(spec);
            }
        } catch (IOException | UnexpectedTokenException ex) {
            throw new RuntimeException(ex);
        }

        return config;
    }
}
