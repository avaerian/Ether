package org.minerift.ether.debug;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE, ElementType.LOCAL_VARIABLE})
public @interface Debug {
    String what() default "null";
}
