package org.minerift.ether.database.sql.diff;

public enum DiffType {
    NOT_EXISTS,     // 00, if element doesn't exist in either set
    DELETED,        // 01, if element is in old set, but not new set
    INSERTED,       // 10, if element is in new set, but not old set
    UPDATED,        // 11, if element is in both sets
}
