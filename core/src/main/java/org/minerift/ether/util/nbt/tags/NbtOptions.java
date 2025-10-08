package org.minerift.ether.util.nbt.tags;

import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.NbtOption;
import org.minerift.ether.util.nbt.nunbt.NuTagTypes;

import java.io.IOException;

public enum NbtOptions implements NbtOption {

    // The NbtOption API is done in this way to allow for both extendable flags
    // as well as more complex options with configurable settings. This is also designed
    // in a beautiful way so that when creating an NbtReader or NbtWriter if additional
    // tag type sets are implemented, we can set the flag in the NbtTraverser, which will
    // load the class containing the extended tag types; this handles the case of attempting
    // to read the extended tag types before the types are registered. The main goal for the
    // extended tag types as well was to allow for these additional sets without having
    // to register them explicitly; the types are only loaded when needed, which is beautiful.

    USE_NUNBT_IO {
        @Override
        protected void load() {
            try {
                Class.forName(NuTagTypes.class.getName());
            } catch (ClassNotFoundException e) {
                throw new UnreachableException("NuTagTypes class not found?", e);
            }
        }
    },

    TEST,
    TEST2,

    ;

    NbtOptions() {
        load();
    }

    // for if initial loading is needed
    protected void load() {
        // no-op
    }

    @Debug
    public static void main(String[] args) throws IOException {
        var test = USE_NUNBT_IO;
        NbtOption[] os = new NbtOption[]{ TEST2, USE_NUNBT_IO, new NbtOption3(5) };
        System.out.println(test(USE_NUNBT_IO, os));
        System.out.println(test(TEST2, os));
        System.out.println(test(TEST, os));
    }

    @Debug
    public static boolean test(Enum<? extends NbtOption> option, NbtOption[] os) {
        for(NbtOption o : os) {
            if(option == o) {
                return true;
            }
        }
        return false;
    }
}
