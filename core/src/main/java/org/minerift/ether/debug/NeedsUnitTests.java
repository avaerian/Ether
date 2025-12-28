package org.minerift.ether.debug;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.PACKAGE, ElementType.MODULE})
public @interface NeedsUnitTests {
}
