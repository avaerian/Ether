package org.minerift.ether.util;

import java.lang.annotation.*;

@Target({ ElementType.FIELD }) // TODO: review
@Retention(RetentionPolicy.RUNTIME)
public @interface Default {
}
