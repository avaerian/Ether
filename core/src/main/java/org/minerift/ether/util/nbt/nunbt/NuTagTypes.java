package org.minerift.ether.util.nbt.nunbt;

import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.util.nbt.tags.TagTypes;
import org.minerift.ether.util.nbt.transmute.MapTags;

public class NuTagTypes {

    public static final TagType<DoubleArrayNuTag> DOUBLE_ARRAY;
    public static final TagType<FloatArrayNuTag> FLOAT_ARRAY;
    public static final TagType<ShortArrayNuTag> SHORT_ARRAY;

    public static final MapTags EXTENDED_TO_PRIMITIVE;

    static {
        // For extended type ids, we could either make a seperate static function
        // to register extended types, or we could provide the type id explicitly like so.
        // Personally, I like the explicit id registering for clarity instead of
        // hiding the impl under the hood.
        DOUBLE_ARRAY = TagTypes.register(~0, "Double_Array", DoubleArrayNuTag.class, DoubleArrayNuTag.Codec::new);
        FLOAT_ARRAY = TagTypes.register(~1, "Float_Array", FloatArrayNuTag.class, FloatArrayNuTag.Codec::new);
        SHORT_ARRAY = TagTypes.register(~2, "Short_Array", ShortArrayNuTag.class, ShortArrayNuTag.Codec::new);

        EXTENDED_TO_PRIMITIVE = MapTags.of()
                .add(DoubleArrayNuTag.class, (t) -> t.is(DOUBLE_ARRAY), DoubleArrayNuTag::toPrimitive)
                .add(FloatArrayNuTag.class, (t) -> t.is(FLOAT_ARRAY), FloatArrayNuTag::toPrimitive)
                .add(ShortArrayNuTag.class, (t) -> t.is(SHORT_ARRAY), ShortArrayNuTag::toPrimitive)
                .immutable();

    }

}
