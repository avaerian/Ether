package org.minerift.ether.util.newnbt.io;

import org.minerift.ether.util.newnbt.NBT;

public class NBTReaderContext extends NBTContext {

    private String currentTagName;
    private NBT.TagType currentTagType;

    public NBTReaderContext() {
        super();
        this.currentTagName = "";
        this.currentTagType = null;
    }

    @Override
    public String getCurrentTagName() {
        return currentTagName;
    }

    @Override
    public NBT.TagType getCurrentTagType() {
        return currentTagType;
    }

    public void setTagName(String name) {
        this.currentTagName = name;
    }

    public void setTagType(NBT.TagType type) {
        this.currentTagType = type;
    }

}
