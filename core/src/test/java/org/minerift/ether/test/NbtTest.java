package org.minerift.ether.test;

//FIXME: netty Unpooled import
import io.netty.buf.ByteBuf; //FIXME
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class NbtTest {

    protected Stream<ByteBuf> onReadNoOptionsTest() {
        
        // TODO: read a few local (resource) files, add to Stream

        // TODO: create Tag's of all types (TagTypes + NuTagTypes)
        //  Assert NuTagTypes aren't readable

        Stream.of(
            
        );
    }

    @ParameterizedTest
    @MethodSource
    public void onReadNoOptionsTest(ByteBuf buf) {
        
    }

}