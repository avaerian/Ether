package org.minerift.ether.config.islandspecs;

import com.google.common.base.Preconditions;
import org.minerift.ether.config.ConfigCodec;
import org.minerift.ether.config.ConfigFileReadException;
import org.minerift.ether.config.ConfigFileWriteException;
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

import static org.minerift.ether.util.nbt.tags.NbtOptions.USE_NUNBT_IO;

public class IslandSpecsCodec extends ConfigCodec<IslandSpecsConfig> {

    public static final IslandSpecsCodec CODEC = new IslandSpecsCodec();

    private IslandSpecsCodec() {
        super(TYPE_DIR);
    }

    @Override
    protected IslandSpecsConfig readIt(File dir) throws ConfigFileReadException {
        Preconditions.checkArgument(dir.exists(), dir.getName() + " does not exist");
        Preconditions.checkArgument(dir.isDirectory(), dir.getName() + " must be a directory");
        IslandSpecsConfig config = new IslandSpecsConfig();

        //  parallelize by queuing tasks and waiting for all tasks to complete? (CompletableFuture/Scheduler )<- shits and giggles
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir.toPath(), "*.spec")) {
            for(Path p : stream) {
                try {
                    File specFile = p.toFile();
                    NbtReader nbt = NbtReader.from(specFile, Compression.GZIP, USE_NUNBT_IO);

                    CompoundTag root = nbt.readNextTag(TagTypes.COMPOUND); //FIXME: review positioning reader/writer indices

                    IslandSpec spec = IslandSpec.of(root);
                    config.add(spec);
                } catch (IslandSpecLoadException | NbtReadException | NoTagTypeFoundException e) {
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

        /*IslandSpec spec = new IslandSpec();
        spec.setIslandName("Default");
        spec.setDescription(List.of("Hello, world!", "Goodbye, world!"));
        spec.setIconData("SNBT data here or something idk");

        IslandSpecsConfig config = new IslandSpecsConfig();
        config.islandSpecs.add(spec);

        IslandSpecsCodec writer = new IslandSpecsCodec();
        // TODO: update reference
        /*writer.writeIt(config, Secrets.LOCAL_ISLAND_SPECS_DIR.transform(File::new));*/

    }

    @Override
    protected void writeIt(IslandSpecsConfig config, File dir) throws ConfigFileWriteException {

        final Path dirPath = dir.toPath();
        if(!dir.exists()) {
            try {
                Files.createDirectories(dirPath);
            } catch (IOException ex) {
                throw new ConfigFileWriteException("Failed to create directories", ex);
            }
        }

        for(IslandSpec spec : config) {
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
                throw new ConfigFileWriteException("Failed to write NbtWriter buffer to file", e);
            }
        }

    }
}
