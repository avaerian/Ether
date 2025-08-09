package org.minerift.ether.config.islandspecs;

import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class IslandSpec {

    private String islandName;
    private List<String> desc;
    private ItemStack<?> icon; // minecraft /give command style (think "minecraft:bedrock")
    private String schemFileName;
    private Vec3i defaultSpawnLoc;

    public IslandSpec() {
        this("", Collections.emptyList(), "", "", Vec3i.ZERO);
    }

    public IslandSpec(String islandName, List<String> desc, String iconData, String schemFileName, Vec3i defaultSpawnLoc) {
        this(islandName, desc, ItemStack.of(iconData), schemFileName, defaultSpawnLoc);
    }

    public IslandSpec(String islandName, List<String> desc, ItemStack<?> icon, String schemFileName, Vec3i defaultSpawnLoc) {
        this.islandName = islandName;
        this.desc = desc;
        this.icon = icon;
        this.schemFileName = schemFileName;
        this.defaultSpawnLoc = defaultSpawnLoc;
    }

    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public List<String> getDescription() {
        return desc;
    }

    public void setDescription(List<String> desc) {
        this.desc = desc;
    }

    public ItemStack<?> getIcon() {
        return icon;
    }

    public String getIconData() {
        return icon.getAsString(); // TODO: test + review
    }

    public void setIcon(ItemStack<?> icon) {
        this.icon = icon;
    }

    public void setIconData(String iconData) {
        /*try {
            this.icon = Bukkit.getItemFactory().createItemStack(iconData);
        } catch (IllegalArgumentException ex) {
            Ether.getLogger().warning("Failed to set invalid icon data: " + iconData);
            Ether.getLogger().warning("Invalid icon replaced with ..."); // TODO
        }*/
        this.icon = ItemStack.of(iconData);
    }

    public String getSchemFileName() {
        return schemFileName;
    }

    public File getSchemFile() {
        return Ether.getPluginFile(Ether.Directory.SCHEMATICS, schemFileName);
    }

    public void setSchemFile(String schemFileName) {
        this.schemFileName = schemFileName;
    }

    public Vec3i getDefaultSpawnLoc() {
        return defaultSpawnLoc;
    }

    public void setDefaultSpawnLoc(Vec3i defaultSpawnLoc) {
        this.defaultSpawnLoc = defaultSpawnLoc;
    }
}
