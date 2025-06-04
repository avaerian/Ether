package org.minerift.ether.database.bin.depbin.reader;

import org.minerift.ether.database.bin.depbin.sections.Section;
import org.minerift.ether.database.bin.depbin.Context;

public class ReaderContext implements Context {
    @Override
    public void accept(Section section) {
        section.visit(this);
    }
}
