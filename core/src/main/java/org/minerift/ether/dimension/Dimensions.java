package org.minerift.ether.dimension;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.config.blocks.BlocksConfig;
import org.minerift.ether.island.CobblestoneGen;
import org.minerift.ether.util.UnreachableException;

import java.util.*;

public class Dimensions implements Iterable<Dimension> {

    private final Map<String, Dimension> dimsByName;

    public static Dimensions byName(Map<String, Dimension> dimsByName) {
        Map<String, Dimension> dimsByResLoc = new HashMap<>(dimsByName.size());
        for(Dimension dim : dimsByName.values()) {
            dimsByResLoc.put(dim.getResourceLocation(), dim);
        }
        return new Dimensions(dimsByName);
    }

    public static Dimensions from(Collection<Dimension> dims) {
        Map<String, Dimension> named = new HashMap<>(dims.size());
        for(Dimension dim : dims) {
            named.put(dim.getName(), dim);
        }
        return new Dimensions(named);
    }

    public static Dimensions minecraft() {
        return from(List.of(
                new Dimension("overworld", new NamespacedKey("minecraft", "overworld"), CobblestoneGen.overworld(), 24, 192, 90),
                new Dimension("nether", new NamespacedKey("minecraft", "nether"), CobblestoneGen.defaults(), 32, 384, 90),
                new Dimension("end", new NamespacedKey("minecraft", "end"), CobblestoneGen.defaults(), 32, 384, 90)
        ));
    }

    public Dimensions(Map<String, Dimension> dimsByName) {
        this.dimsByName = dimsByName;
    }

    public Map<String, Dimension> getDimensionsByName() {
        return Collections.unmodifiableMap(dimsByName);
    }

    public Collection<Dimension> getDimensions() {
        return dimsByName.values();
    }

    public Dimension getByName(String name) {
        return dimsByName.get(name);
    }

    public Dimension getByResource(String resource) {
        throw new UnreachableException("unimplemented");
        //return dimsByResLoc.get(resource);
    }

    public int size() {
        return dimsByName.size();
    }

    @Override
    public @NotNull Iterator<Dimension> iterator() {
        return dimsByName.values().iterator();
    }

    @Override
    public String toString() {
        return "Dimensions{" +
                "dimsByName=" + dimsByName +
                //", dimsByResLoc=" + dimsByResLoc +
                '}';
    }
}
