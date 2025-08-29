package org.minerift.ether.nms.world.block;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.schematic.transform.Direction;

import java.util.*;
import java.util.function.Predicate;

public class EnumAttribute<E extends Enum<E>> extends Attribute<E> {

    protected final byte[] universe;

    // Branchless implementation to set x bits to 1
    // Made for shits and giggles
    private static byte[] allOf(final int enums) {
        int x = enums;
        int y = enums >> 3 << 3;

        int add = ~((enums & 7) - (enums - 1 & 7)) >>> 31;
        int zero = enums - 1 >>> 31;

        byte[] set = new byte[(enums >> 3) + add + zero];
        for(int i = set.length - 1; i >= 0; i--) {
            int res = ((x - y - 1) & 7) + 1;
            int bits = (~(0xFF << res) & 0xFF) - (zero * ((zero << 8) - 1));
            set[i] = (byte)bits;

            //System.out.println(res + " " + bits + " " + Integer.toBinaryString(bits) + " " + test);

            y -= 8;
            x = y;
        }
        return set;
    }

    @Debug
    public static void main(String[] args) {
        //byte[] uni = create(0, Token.class, Token.AS, Token.CASCADE, Token.CONSTRAINT, Token.DEFERRABLE, Token.DEFAULT).universe;
        byte[] uni = create(0, "", Direction.class, Direction.NORTH, Direction.UP, Direction.WEST, Direction.DOWN).universe;
        for(byte b : uni) {
            System.out.println("b " + Integer.toBinaryString(b&0xFF));
        }
    }

    public static <E extends Enum<E>> EnumAttribute<E> create(int id, String name, Class<E> clazz) {
        byte[] universe = allOf(clazz.getEnumConstants().length);
        return new EnumAttribute<>(id, name, clazz, universe);
    }

    public static <E extends Enum<E>> EnumAttribute<E> create(int id, String name, Class<E> clazz, E...allowedValues) {
        final int enums = clazz.getEnumConstants().length;
        final int add = ~((enums & 7) - (enums - 1 & 7)) >>> 31;
        byte[] universe = new byte[(enums >> 3) + add]; // TODO: review; zero var needed to account 0 values???
        for(E e : allowedValues) {
            universe[e.ordinal() >> 3] |= (byte) (1 << (e.ordinal() & 7));
        }
        return new EnumAttribute<>(id, name, clazz, universe);
    }

    public static <E extends Enum<E>> EnumAttribute<E> create(int id, String name, Class<E> clazz, Predicate<E> filter) {
        byte[] universe = new byte[(clazz.getEnumConstants().length >> 3)]; //FIXME
        Arrays.stream(clazz.getEnumConstants())
                .filter(filter)
                .forEach( e -> universe[e.ordinal() >> 3] |= (byte) (1 << (e.ordinal() & 7)) );
        return new EnumAttribute<>(id, name, clazz, universe);
    }

    protected EnumAttribute(int id, String name, Class<E> clazz, byte[] universe) {
        super(id, name, clazz);
        this.universe = universe;
    }

    @Override
    public boolean isAcceptableValue(E val) {
        return (universe[val.ordinal() >> 3] & (val.ordinal() & 7)) != 0;
    }

    @Override
    public Collection<E> getAcceptableValues() {
        E[] enums = clazz.getEnumConstants();
        EnumSet<E> set = EnumSet.noneOf(clazz);
        for(int i = 0; i < universe.length; i++) {
            byte b = universe[i];
            if(b == 0) {
                continue;
            }
            for(int bit = 0; bit < 8; bit++) {
                b >>= 1;
                if((b & 1) != 0) {
                    set.add(enums[(i * 8) + bit]);
                }
            }
        }
        return set;
    }
}
