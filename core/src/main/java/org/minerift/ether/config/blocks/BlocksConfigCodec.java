package org.minerift.ether.config.blocks;

import org.minerift.ether.config.ConfigCodec;
import org.minerift.ether.config.ConfigReadException;
import org.minerift.ether.config.ConfigWriteException;
import org.minerift.ether.config.YamlConfigView;
import org.minerift.ether.config.source.FileSource;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;

import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.StandardOpenOption;

public class BlocksConfigCodec extends ConfigCodec<BlocksConfig, FileSource> {

    public static final BlocksConfigCodec INST = new BlocksConfigCodec();

    @Override
    protected void readIt(BlocksConfig cfg, FileSource src) throws ConfigReadException {

        try (FileChannel fc = src.openFileChannel(StandardOpenOption.READ);
            FileLock lock = fc.lock()) {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setProcessComments(true);
            Yaml yaml = new Yaml(dumperOptions);
        } catch (IOException e) {
            throw new ConfigReadException("Failed to open file channel for file " + src.getFile());
        }
    }

    @Override
    protected void writeIt(BlocksConfig cfg, FileSource src) throws ConfigWriteException {

    }
}
