package org.minerift.ether.schematic.transform;

import com.google.common.base.Preconditions;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.data.Array3DOrder;
import org.minerift.ether.util.UnreachableException;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

public class Rotate implements Transform {

    public static final byte SWAP_IN_BUFFER;

    static {
        byte bit = 0;
        SWAP_IN_BUFFER = (byte) (1 << bit++);
    }

    public enum Angle {
        DG_90,
        DG_180, // flip on both coords
        DG_NEG_90,
    }

    private Axis axis;
    private Angle angle;
    private byte options;

    public Rotate(Axis axis, Angle angle) {
        this(axis, angle, (byte) 0);
    }

    public Rotate(Axis axis, Angle angle, byte options) {
        this.axis = axis;
        this.angle = angle;
        this.options = options;
    }

    @Override
    public byte[] transform(Array3DOrder order, int width, int height, int len, byte[] src) {
        Preconditions.checkArgument(width * height * len == src.length);

        // AXIS (rotating on X and Z, layer Y) ->
        // Precedence for coords is determined by order (?)
        // NOTE: YZX ordering used for this example
        // Rotate Y axis -> rotate Z,X (x is inner-most loop)
        // Rotate X axis -> rotate Y,Z (z is inner-most loop)
        // Rotate Z axis -> rotate Y,X (x is inner-most loop)

        Vec3i dim = new Vec3i(width, height, len);
        switch (angle) {
            case DG_90 -> {
                int rotX = axis.getRotatingX(dim);
                int rotZ = axis.getRotatingZ(dim);
                if(rotX == rotZ) {

                    if((options & SWAP_IN_BUFFER) != 0) { // if swap in buffer
                        // rotate in-place
                        return rotateSquare90Swap(axis, order, rotX, axis.getLayers(dim), src);
                    } else {
                        // copy and rotate
                        // TODO: review algorithm for this
                        byte[] copy = new byte[src.length];
                        System.arraycopy(src, 0, copy, 0, src.length);
                        return rotateSquare90Swap(axis, order, rotX, axis.getLayers(dim), copy);
                    }

                } else {

                    return rotateRect90(axis, order, dim, src);
                }
            }

            case DG_180 -> {
                byte[] buf;
                // TODO: review algorithm for swapping vs copy
                if((options & SWAP_IN_BUFFER) == 0) { // if not swap in buffer (copy buffer)
                    buf = new byte[src.length];
                    System.arraycopy(src, 0, buf, 0, src.length);
                } else {
                    buf = src;
                }
                return rotateRect180Swap(axis, order, dim, buf);
            }

        }

        throw new UnreachableException("TODO");
    }

    private static byte[] rotateRect180Swap(Axis axis, Array3DOrder order, Vec3i dim, byte[] src) {

        final int width = dim.getX();
        final int length = dim.getZ();
        
        final int rotZ = axis.getRotatingZ(dim);
        final int rotX = axis.getRotatingX(dim);
        
        final int layers = axis.getLayers(dim);
        final int iterZ = axis.getRotatingZ(dim);
        final int iterX = (axis.getRotatingX(dim) + 1) / 2;

        System.out.println("iterX " + iterX + ", iterZ " + iterZ);

        for(int layer = 0; layer < layers; layer++) {
            for(int z = 0; z < iterZ; z++) {
                for(int x = 0; x < iterX; x++) {

                    int currFlat = axis.flatten(order, width, length, x, layer, z);
                    int transFlat = axis.flatten(order, width, length,
                            rotX - 1 - x, layer,  rotZ - 1 - z);

                    System.out.printf("(%d,%d,%d) %d -> (%d,%d,%d) %d\n",
                            x, layer, z, currFlat, width - 1 - x, layer, length - 1 - z, transFlat);

                    byte swap = src[transFlat];
                    src[transFlat] = src[currFlat];
                    src[currFlat] = swap;

                }
            }
        }

        return src;

    }

    @Debug(what = "Algorithms for 90deg rotations")
    public static void main(String[] args) {
        Array3DOrder order = Array3DOrder.YZX;
        final int width = 4;
        final int length = 3;
        //final int len = 3;
        final int layers = 7; // y

        //final int[] ibuf = IntStream.range(0, len * layers * len).toArray();
        byte[] buf = new byte[width * length * layers/*ibuf.length*/];

        for(int l = 0; l < layers; l++) {
            for (int i = 0; i < (width * length); i++) {
                //System.out.println(i + (l * width * length));
                //buf[i + (l * width * length)] = (byte) (3 + l);
                buf[i + (l * width * length)] = (byte) (i + (l * width * length));
            }
        }

        /*for(int i = 0; i < ibuf.length; i++) {
            buf[i] = (byte)ibuf[i];
        }*/

        Vec3i dim = new Vec3i(width, layers, length);
        System.out.println(dim);

        Axis axis = Axis.Y;
        /*int rotX = axis.getRotatingX(dim);
        int rotZ = axis.getRotatingZ(dim);
        if(rotX != rotZ) {
            throw new RuntimeException(rotX + ", " + rotZ);
        }*/

        System.out.println("Buf: " + Arrays.toString(buf));
        /*for(int rots = 0; rots < 1; rots++) {
            //rotateSquare90Swap(axis, order, len, layers, buf);
            buf = rotateRect90(axis, order, dim, buf);
        }*/
        rotateRect180Swap(axis, order, dim, buf);

        System.out.println("buf: " + Arrays.toString(buf));

        /*for(int z = 0; z < len; z++) {
            for(int x = 0; x < len; x++) {
                int i = order.flatten(len, len, x, 0, z);
                System.out.println("i " + i + " = " + buf[i]);
            }
        }*/
    }

    private static byte[] rotateRect90(Axis axis, Array3DOrder order, Vec3i dim, byte[] src) {
        final int rotX = axis.getRotatingX(dim);
        final int rotZ = axis.getRotatingZ(dim);

        System.out.println("rotX " + rotX + ", rotZ " + rotZ);

        byte[] out = new byte[src.length];

        final int gridLen = Math.max(rotX, rotZ); // max(x, z)
        final int difference = rotX >= rotZ ? rotX - rotZ : rotZ - rotX;
        System.out.println("gridLen " + gridLen + ", diff " + difference);
        //System.out.println(order.flatten(dim.getX(), dim.getZ(), 0, 3, 0));

        Vec3i rotatedDim = new Vec3i(axis.getRotatingX(dim),
                axis.getLayers(dim), axis.getRotatingZ(dim)); // TODO: swap X and Z??
        System.out.println("dim: " + dim + ", rotated: " + rotatedDim);

        // (x, z) -> ([len - 1] - z, x)
        Vec2i trRotated = new Vec2i(gridLen - 1, rotatedDim.getX() - 1); // original: (dimX,0)
        //Vec2i blRotated = new Vec2i(gridLen - 1 - rotatedDim.getZ(), 0); // original: (0, dimZ)
        //System.out.println(dim.getX() + " 0");
        //System.out.println(trRotated);

        // check if top-left correction is needed
        IntUnaryOperator transform;
        if(trRotated.getX() == gridLen - 1 && trRotated.getZ() == gridLen - 1) { // TODO: review
            System.out.println("with diff");
            transform = (a) -> (gridLen - 1 - a) - difference;
        } else {
            transform = (a) -> gridLen - 1 - a;
        }

        final int layers = axis.getLayers(dim);

        //System.out.println(dim);
        for(int layer = 0; layer < layers; layer++) {
            for(int z = 0; z < rotZ; z++) {
                for(int x = 0; x < rotX; x++) {

                    int currFlat = axis.flatten(order, dim.getX(), dim.getZ(), x, layer, z);
                    int transFlat = order.flatten(rotatedDim.getZ(), rotatedDim.getX(),
                            transform.applyAsInt(z), layer, x); // 90deg rotation
                    //System.out.printf("(%d,%d,%d) %d -> (%d,%d,%d) %d\n", x, z, layer, currFlat, gridLen - 1 - z - difference, x, layer, transFlat);
                    //System.out.println(currFlat + ", " + transFlat);

                    out[transFlat] = src[currFlat];
                }
            }
        }

        return out;
    }

    private static byte[] rotateSquare90Swap(Axis axis, Array3DOrder order, int len, int layers, byte[] buf) {

        final int points = len; // redundant; for clarity
        final int shells = (points + 1) / 2; // z

        for(int layer = 0; layer < layers; layer++) {
            for(int shell = 0; shell < shells; shell++) {

                //System.out.println("shell " + shell);
                for(int point = shell; point < points - shell; point++) {

                    // TODO: switch from mut Vec2i to two ints?
                    // TODO: move these two Vec2i's out of loop to avoid reallocating
                    Vec2i.Mutable curr = new Vec2i.Mutable(point, shell);
                    Vec2i.Mutable lookAhead = new Vec2i.Mutable(0,0);
                    int nextSwapVal = buf[axis.flatten(order, len, curr.getX(), layer, curr.getZ())]; // current
                    for(int i = 0; i < 4; i++) {
                        // TODO: to allow -90 rotations, change this to Consumer<Vec3i>
                        lookAhead.set(len - 1 - curr.getZ(), curr.getX()); // 90deg rotation

                        int currFlat = axis.flatten(order, len, curr.getX(), layer, curr.getZ()); // unused
                        int aheadFlat = axis.flatten(order, len, lookAhead.getX(), layer, lookAhead.getZ());

                        //System.out.println(point + ", curr " + curr + ", next " + lookAhead);
                        //System.out.println(Arrays.toString(buf));

                        // swap
                        int carry = buf[aheadFlat];
                        buf[aheadFlat] = (byte) nextSwapVal;
                        nextSwapVal = carry;

                        curr.set(lookAhead.getX(), lookAhead.getZ());
                    }
                }

            }
        }
        // ROTATE SQUARE END
        return buf;
    }

}
