package org.minerift.ether.database.nubin;

import org.minerift.ether.database.nubin.sections.Section;

public interface Context {
    void accept(Section section);
}
