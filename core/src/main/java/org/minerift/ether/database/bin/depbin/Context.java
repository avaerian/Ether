package org.minerift.ether.database.bin.depbin;

import org.minerift.ether.database.bin.depbin.sections.Section;

public interface Context {
    void accept(Section section);
}
