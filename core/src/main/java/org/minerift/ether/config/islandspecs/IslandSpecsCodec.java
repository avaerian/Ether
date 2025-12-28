package org.minerift.ether.config.islandspecs;

import com.google.common.base.Preconditions;
import org.minerift.ether.config.ConfigCodec;
import org.minerift.ether.config.ConfigReadException;
import org.minerift.ether.config.ConfigWriteException;
import org.minerift.ether.config.source.DirectorySource;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.nbt.*;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.NoTagTypeFoundException;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.minerift.ether.util.nbt.tags.NbtOptions.USE_NUNBT_IO;

public class IslandSpecsCodec extends ConfigCodec<IslandSpecsConfig, DirectorySource> {

    public static final IslandSpecsCodec INST = new IslandSpecsCodec();

    @Override
    protected void readIt(IslandSpecsConfig cfg, DirectorySource src) throws ConfigReadException {
        /* START MOVE THIS OUT OF HERE */
        final File dir = src.getDirectory();
        Preconditions.checkArgument(dir.exists(), dir.getName() + " does not exist");
        Preconditions.checkArgument(dir.isDirectory(), dir.getName() + " must be a directory");
        /* END MOVE THIS OUT OF HERE */

        List<IslandSpec> specs = new ArrayList<>(16);
        // parallelize by queuing tasks and waiting for all tasks to complete? (CompletableFuture/Scheduler)<- shits and giggles
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), "*.spec")) {
            for(Path p : stream) {
                try {
                    File specFile = p.toFile();
                    NbtReader nbt = NbtReader.from(specFile, Compression.GZIP, USE_NUNBT_IO);

                    CompoundTag root = nbt.readNextTag(TagTypes.COMPOUND); //FIXME: review positioning reader/writer indices

                    IslandSpec spec = IslandSpec.of(root);
                    specs.add(spec);
                } catch (IslandSpecLoadException | NbtReadException | NoTagTypeFoundException e) {
                    // TODO: logger
                    // skip this file and log
                    System.out.printf("Failed to load %s (%s): %s\n", p.getFileName(), p, e.getMessage()); // consider e.getLocalizedMessage()
                }
            }
        } catch (IOException ex) {
            throw new ConfigReadException(ex);
        }
        cfg.islandSpecs = specs;
    }

    @Override
    protected void writeIt(IslandSpecsConfig cfg, DirectorySource src) throws ConfigWriteException {
        final File dir = src.getDirectory();
        final Path dirPath = dir.toPath();
        if(!dir.exists()) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException ex) {
                throw new ConfigWriteException("Failed to create directories", ex);
            }
        }

        for(IslandSpec spec : cfg) {
            CompoundTag tag = spec.serializeNbt();

            // first, write to buffer
            NbtWriter nbt = NbtWriter.withOptions()
                    .options(USE_NUNBT_IO)
                    .build();// use default buffer
            nbt.writeTag(tag);
            try {
                nbt.compress(Compression.GZIP);
            } catch (IOException e) {
                // TODO: logger
                System.out.println("Skipping IslandSpec " + spec.getIslandName() + ": failed to compress schematic");
                continue;
            }

            /*if(!spec.getFilePath().startsWith(dirPath)) {
                // for this case, I can't think of a way that would work best:
                // throw exception? resolve? this will just lead to undefined behavior for now
            }*/

            // dump buffer to file
            // TODO: refactor file path from IslandSpec ????
            try(FileChannel out = FileChannel.open(spec.getFilePath())) {
                nbt.dump(out);
            } catch (IOException e) {
                throw new ConfigWriteException("Failed to write NbtWriter buffer to file", e);
            }
        }

    }
}
