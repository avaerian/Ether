package org.minerift.ether.config.source;

import java.io.IOException;

public interface Source {

    // returns false if already exists, or if creation fails
    default boolean createIfNotExists() throws IOException {
        /*if(!exists()) {
            return create();
        }
        return false;*/
        return !exists() && create();
    }

    boolean create() throws IOException;
    boolean exists();
    //boolean 

}
