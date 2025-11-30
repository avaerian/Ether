package org.minerift.ether.config;

// TODO
public interface Source {

    // returns false if already exists, or if creation fails
    default boolean createIfNotExists() {
        /*if(!exists()) {
            return create();
        }
        return false;*/
        return !exists() && create();
    }

    boolean create();
    boolean exists();
    //boolean 

}
