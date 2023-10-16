package org.minerift.ether.database.sql.operations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Indicates that a method is an SQL operation procedure.
// This means that the method is one of many procedures for
// supporting across multiple SQL dialects.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SQLOpProc {
}
