package org.minerift.ether.database;

import java.util.UUID;

public class DataType<T> {

    public static final DataType<UUID> UUID = new DataType<>(PrimitiveType.UUID, UUID.class);
    public static final DataType<Boolean> BOOL = new DataType<>(PrimitiveType.BOOLEAN, Boolean.class);
    public static final DataType<Byte> BYTE = new DataType<>(PrimitiveType.BYTE, Byte.class);
    public static final DataType<Short> SHORT = new DataType<>(PrimitiveType.SHORT, Short.class);
    public static final DataType<Integer> INT = new DataType<>(PrimitiveType.INTEGER, Integer.class);
    public static final DataType<Long> BIGINT = new DataType<>(PrimitiveType.BIGINT, Long.class);
    public static final DataType<Float> FLOAT = new DataType<>(PrimitiveType.FLOAT, Float.class);
    public static final DataType<Double> DOUBLE = new DataType<>(PrimitiveType.DOUBLE, Double.class);
    public static final DataType<String> CHAR = new DataType<>(PrimitiveType.CHAR, String.class);
    public static final DataType<String> VARCHAR = new DataType<>(PrimitiveType.VARCHAR, String.class);
    public static final DataType<byte[]> BINARY = new DataType<>(PrimitiveType.BINARY, byte[].class);
    public static final DataType<byte[]> VARBINARY = new DataType<>(PrimitiveType.VARBINARY, byte[].class);

    // UNBOXED PRIMITIVE ARRAY TYPES
    public static final DataType<short[]> SHORTS = new DataType<>(PrimitiveType.SHORT, short[].class);
    public static final DataType<int[]> INTS = new DataType<>(PrimitiveType.INTEGER, int[].class);
    public static final DataType<long[]> BIGINTS = new DataType<>(PrimitiveType.BIGINT, long[].class);
    public static final DataType<float[]> FLOATS = new DataType<>(PrimitiveType.FLOAT, float[].class);
    public static final DataType<double[]> DOUBLES = new DataType<>(PrimitiveType.DOUBLE, double[].class);


    public static DataType<short[]> SHORTS(int arrayLength) {
        return SHORTS.arrayLength(arrayLength);
    }

    public static DataType<int[]> INTS(int arrayLength) {
        return INTS.arrayLength(arrayLength);
    }

    public static DataType<long[]> BIGINTS(int arrayLength) {
        return BIGINTS.arrayLength(arrayLength);
    }

    public static DataType<float[]> FLOATS(int arrayLength) {
        return FLOATS.arrayLength(arrayLength);
    }

    public static DataType<double[]> DOUBLES(int arrayLength) {
        return DOUBLES.arrayLength(arrayLength);
    }


    public static DataType<String> CHAR(int length) {
        return CHAR.length(length);
    }

    public static DataType<String> VARCHAR(int length) {
        return VARCHAR.length(length);
    }

    public static <E extends Enum<E>> DataType<E> ENUM_ORDINAL(Class<E> enumClazz) {
        return new DataType<>(PrimitiveType.ENUM_ORDINAL, enumClazz);
    }

    public static <E extends Enum<E>> DataType<E> ENUM_STR(Class<E> enumClazz) {
        return new DataType<>(PrimitiveType.ENUM_STR, enumClazz, false, 255);
    }

    @Deprecated
    public static DataType<String> STRING(int length) {
        return VARCHAR(length);
    }

    public static DataType<byte[]> BINARY(int length) {
        return BINARY.length(length);
    }

    public static DataType<byte[]> VARBINARY(int length) {
        return VARBINARY.length(length);
    }


    public static final int MAX = 8000; // TODO: review and see if improvements can be made for storing large binary data
    protected static final int VALUE_NOT_SET = -1;

    protected final PrimitiveType primitiveType;
    protected final Class<?> baseTypeClazz; // for arrays; the base type of an array
    protected final Class<T> typeClazz; // the actual type
    protected final boolean nullable;
    protected final int length;
    protected final int arrayLength;

    public DataType(PrimitiveType primitive, Class<T> clazz) {
        this(primitive, clazz, false, VALUE_NOT_SET);
    }

    public DataType(PrimitiveType primitive, Class<T> clazz, boolean nullable) {
        this(primitive, clazz, nullable, VALUE_NOT_SET);
    }

    public DataType(PrimitiveType primitive, Class<T> clazz, boolean nullable, int length) {
        this(primitive, clazz, nullable, length, VALUE_NOT_SET);
    }

    public DataType(PrimitiveType primitive, Class<T> clazz, boolean nullable, int length, int arrayLength) {
        this.primitiveType = primitive;
        this.typeClazz = clazz;
        this.baseTypeClazz = typeClazz.componentType();
        this.nullable = nullable;
        this.length = length; // TODO: validate
        this.arrayLength = arrayLength; // TODO: validate
    }

    public DataType<T> nullable(boolean newVal) {
        return nullable == newVal ? this : new DataType<>(primitiveType, typeClazz, newVal, length, arrayLength);
    }

    public boolean isNullable() {
        return nullable;
    }

    public DataType<T> notNull() {
        return nullable(false);
    }

    public DataType<T> length(int newVal) {
        if(newVal <= 0) {
            throw new IllegalArgumentException("New length is invalid: " + newVal);
        }
        return length == newVal ? this : new DataType<>(primitiveType, typeClazz, nullable, newVal, arrayLength);
    }

    public int length() {
        return length;
    }

    public boolean hasLength() {
        return length != VALUE_NOT_SET;
    }

    public DataType<T> arrayLength(int newVal) {
        if(newVal <= 0) {
            throw new IllegalArgumentException("New array length is invalid: " + newVal);
        }
        return arrayLength == newVal ? this : new DataType<>(primitiveType, typeClazz, nullable, length, newVal);
    }

    public int arrayLength() {
        return arrayLength;
    }

    public boolean hasDynamicArrayLength() {
        return arrayLength == VALUE_NOT_SET;
    }

    public DataType<T[]> array(int arrayLength) {
        if(isArrayType()) {
            throw new UnsupportedOperationException("Type is already an array!");
        }
        //System.out.println(typeClazz.arrayType().getTypeName()); // debug
        return new DataType<>(primitiveType, (Class<T[]>) typeClazz.arrayType(), nullable, length, arrayLength);
    }

    public DataType<T[]> array() {
        return array(VALUE_NOT_SET);
    }

    public Class<T> getType() {
        return typeClazz;
    }

    // Return base type if array, otherwise null
    public Class<?> getArrayBaseType() {
        return baseTypeClazz;
    }

    public PrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    public String getTypeName() {
        return primitiveType.getTypeName();
    }

    // Identifies whether the type is an extended binary type (not single byte, but byte[])
    public boolean isExtendedBinaryType() {
        return primitiveType == PrimitiveType.BINARY || primitiveType == PrimitiveType.VARBINARY;
    }

    public boolean isEnumType() {
        return primitiveType == PrimitiveType.ENUM_ORDINAL
                || primitiveType == PrimitiveType.ENUM_STR;
    }

    /**
     * Identify whether the data type is an integer type.
     * This identifies any math integer type, not just an I32.
     * @return if type is an math integer
     */
    public boolean isIntegerType() {
        return primitiveType == PrimitiveType.BYTE
                || primitiveType == PrimitiveType.SHORT
                || primitiveType == PrimitiveType.INTEGER
                || primitiveType == PrimitiveType.BIGINT;
    }

    public boolean isDecimalType() {
        return primitiveType == PrimitiveType.FLOAT || primitiveType == PrimitiveType.DOUBLE;
    }

    public boolean isStringType() {
        return primitiveType == PrimitiveType.CHAR || primitiveType == PrimitiveType.VARCHAR;
    }

    public boolean isArrayType() {
        return typeClazz.isArray();
    }

    // TODO: documentation
    public boolean isDynamicallySizedType() {
        return primitiveType == PrimitiveType.VARCHAR || primitiveType == PrimitiveType.VARBINARY;
    }

    // TODO: review this
    @Deprecated
    public int getByteSize(T data) {
        return switch (primitiveType) {
            case UUID -> 4;
            case BOOLEAN, BYTE -> 1;
            case SHORT -> Short.BYTES;
            case INTEGER -> Integer.BYTES;
            case BIGINT -> Long.BYTES;
            case FLOAT -> Float.BYTES;
            case DOUBLE -> Double.BYTES;

            // TODO: review char and varchar
            case CHAR -> this.length * Character.BYTES;
            case VARCHAR -> Short.BYTES + ((String)data).getBytes().length;

            case BINARY -> this.length;
            case VARBINARY -> Integer.BYTES + ((byte[])data).length;

            // TODO: handle these later
            case ENUM_ORDINAL -> 0;
            case ENUM_STR -> 0;
            case CLOB -> 0;
            case BLOB -> 0;
            case DATE -> 0;
            case TIME -> 0;
            case TIMESTAMP -> 0;
        };
    }
}
