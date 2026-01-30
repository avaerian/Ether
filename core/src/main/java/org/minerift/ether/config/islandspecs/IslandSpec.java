package org.minerift.ether.config.islandspecs;

import org.minerift.ether.island.spawn.*;
import org.minerift.ether.nms.world.ItemStack;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.util.nbt.NbtSerializable;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.SnbtReadException;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.StringTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.minerift.ether.schematic.SchematicCodec.*;
import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;
import static org.minerift.ether.util.nbt.tags.TagTypes.STRING;

// Immutable, with builder that can copy spec to mutate before finalizing.
// If two threads try to read IslandSpec and spec is updated between reading in threads,
// the old spec will be read in the first thread and second thread will see copied, updated spec
public class IslandSpec implements NbtSerializable {

    public static final String ISLAND_NAME = "IslandName";
    public static final String ISLAND_DESC = "Desc";
    public static final String ISLAND_SCHEMATIC = "Schematic";
    public static final String ISLAND_ICON = "Icon";
    public static final String ISLAND_SPAWNS_CLASS = "SpawnsClass";
    public static final String ISLAND_SPAWNS_DATA = "Spawns";

    public static IslandSpec of(CompoundTag nbt, Path fpath) throws IslandSpecLoadException {
        String islandName = nbt.getString(ISLAND_NAME, (e) -> new IslandSpecLoadException("Failed to read island name", e));
        List<String> desc = nbt.getList(ISLAND_DESC, STRING, (e) -> new IslandSpecLoadException("Failed to read island desc", e))
                .unwrapTags(StringTag::getStrVal);

        // icon
        String iconSnbt = nbt.getString(ISLAND_ICON, (e) -> new IslandSpecLoadException("Failed to read icon"));
        CompoundTag iconTag;
        try {
            iconTag = Snbt.readTag(iconSnbt, COMPOUND);
        } catch (SnbtReadException | UnexpectedTokenException e) {
            throw new IslandSpecLoadException("Failed to load icon", e);
        }
        ItemStack<?> icon = ItemStack.of(iconTag); // TODO: review ItemStack loading, may throw exception from invalid input

        // schematic
        CompoundTag schemTag = nbt.getCompound(ISLAND_SCHEMATIC, (e) -> new IslandSpecLoadException("Failed to read schematic"));
        Schematic schem;
        try {
            schem = Schematic.fromTyped(SchematicType.SPONGE, schemTag);
        } catch (SchematicReadException e) {
            throw new IslandSpecLoadException("Failed to load schematic", e);
        }

        Class<? extends SpawnStrategy> spawnsClazz;
        try {
            String clazzPath = nbt.getString(ISLAND_SPAWNS_CLASS,
                    (e) -> new IslandSpecLoadException("Failed to read island spawns class"));
            // each SpawnStrategy class will be loaded, containing its own registration info
            spawnsClazz = Class.forName(clazzPath).asSubclass(SpawnStrategy.class);
        } catch (ClassNotFoundException e) {
            throw new IslandSpecLoadException("SpawnStrategy class for IslandSpec not found", e);
        } catch (ClassCastException e) {
            throw new IslandSpecLoadException("Island spawns class is not a SpawnStrategy", e);
        }

        SpawnStrategy spawn;
        try {
            Tag spawnNbt = nbt.getTag(ISLAND_SPAWNS_DATA,
                    (e) -> new IslandSpecLoadException("Failed to read island spawns data"));
            spawn = SpawnStrategy.of(spawnsClazz, spawnNbt);
        } catch (SpawnStrategyLoadException e) {
            throw new IslandSpecLoadException("Spawn strategy failed to load", e);
        }

        return new IslandSpec(islandName, desc, icon, schem, spawn, fpath);
    }

    public static IslandSpec of(CompoundTag nbt) throws IslandSpecLoadException {
        return of(nbt, null);
    }

    public static IslandSpec.Builder ofBuilder(CompoundTag nbt) throws IslandSpecLoadException {
        // kinda hacky? lazy approach
        IslandSpec data = of(nbt);
        return new Builder(data);
    }

    public static IslandSpec.Builder builder() {
        return new IslandSpec.Builder();
    }

    public static IslandSpec.Builder builder(IslandSpec spec) {
        return new IslandSpec.Builder(spec);
    }

    private final String islandName;
    private final List<String> desc;
    private final ItemStack<?> icon; // snbt -> minecraft /give command style (think "minecraft:bedrock")
    private final Schematic schem; // if need be, can be lazy by wrapping actual Schematic with some "LazySchematic"
    private final SpawnStrategy spawns;
    @Deprecated private final Path fpath;

    public IslandSpec(String islandName, List<String> desc, ItemStack<?> icon, Schematic schem, SpawnStrategy spawns, Path fpath) {
        this.islandName = islandName;
        this.desc = desc;
        this.icon = icon;
        this.schem = schem;
        this.spawns = spawns;
        this.fpath = fpath;
    }

    public String getIslandName() {
        return islandName;
    }

    public List<String> getDescription() {
        return desc;
    }

    public ItemStack<?> getIcon() {
        return icon;
    }

    public String getIconData() {
        return icon.getAsString(); // TODO: test + review
    }

    public Schematic getSchematic() {
        return schem;
    }

    public SpawnStrategy getSpawns() {
        return spawns;
    }

    public Path getFilePath() {
        return fpath;
    }

    @Override
    public CompoundTag serializeNbt() {
        CompoundTag root = new CompoundTag("Data");
        root.addTag(new StringTag(ISLAND_NAME, islandName));

        // description
        ListTag<StringTag> descTag = new ListTag<>(ISLAND_DESC, STRING,
                desc.isEmpty() ? Collections.emptyList() : new ArrayList<>(desc.size()));
        for(String line : desc) {
            descTag.addTag(StringTag.valueOf(line));
        }
        root.addTag(descTag);

        String iconSnbt = Snbt.writeTag(icon.getNbtTag());
        root.addTag(new StringTag(ISLAND_ICON, iconSnbt));

        // schematic
        CompoundTag schemTag = schem.writeAsNbt(EXCLUDE_BIOMES | WRITE_ENTITIES_ALWAYS);
        schemTag.setName(ISLAND_SCHEMATIC);
        root.addTag(schemTag);

        // spawns
        root.addTag(new StringTag(ISLAND_SPAWNS_CLASS, spawns.getClass().getName()));
        Tag spawnsTag = spawns.serializeNbt();
        spawnsTag.setName(ISLAND_SPAWNS_DATA);
        root.addTag(spawnsTag);

        return root;
    }

    public static class Builder /*implements IBuilder<IslandSpec> // include? */ {

        protected String islandName;
        protected List<String> desc;
        protected ItemStack<?> icon;
        protected Schematic schem;
        protected SpawnStrategy spawns;
        protected Path fpath;

        public Builder() {
            this.islandName = "";
            this.desc = Collections.emptyList();
            //this.icon = ItemStack.of("minecraft:grass_block"); // FIXME: this isn't working <-----------
            this.icon = ItemStack.of("{id:\"minecraft:grass_block\"}"); // temp until resolved for above format
            this.schem = null; // FIXME: review default
            this.spawns = null; // FIXME: create proper default (single spawn)
            this.fpath = null;
        }

        public Builder(IslandSpec spec) {
            this.islandName = spec.islandName;
            this.desc = spec.desc;
            this.icon = spec.icon;
            this.schem = spec.schem;
            this.spawns = spec.spawns;
            this.fpath = spec.fpath;
        }

        public IslandSpec build() {
            // TODO: validate/set defaults for settings
            return new IslandSpec(islandName, desc, icon, schem, spawns, fpath);
        }

        public Builder setIslandName(String islandName) {
            this.islandName = islandName;
            return this;
        }

        public String getIslandName() {
            return islandName;
        }

        public List<String> getDesc() {
            return desc;
        }

        public Builder setDesc(List<String> desc) {
            this.desc = desc;
            return this;
        }

        public ItemStack<?> getIcon() {
            return icon;
        }

        public Builder setIcon(ItemStack<?> icon) {
            this.icon = icon;
            return this;
        }

        public Schematic getSchematic() {
            return schem;
        }

        public Builder setSchematic(Schematic schem) {
            this.schem = schem;
            return this;
        }

        public SpawnStrategy getSpawns() {
            return spawns;
        }

        public Builder setSpawns(SpawnStrategy spawns) {
            this.spawns = spawns;
            return this;
        }

        public Path getFile() {
            return fpath;
        }

        public Builder setFile(Path fpath) {
            this.fpath = fpath;
            return this;
        }

        public Builder setFile(File f) {
            this.fpath = f.toPath();
            return this;
        }
    }
}
