package org.minerift.ether.database.sql.operations;

import org.minerift.ether.database.sql.SQLDialect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SupportedBy {
    SQLDialect[] dialects() default {};
}
