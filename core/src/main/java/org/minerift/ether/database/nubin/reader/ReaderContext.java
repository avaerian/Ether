package org.minerift.ether.database.nubin.reader;

import org.minerift.ether.database.nubin.Context;
import org.minerift.ether.database.nubin.sections.Section;

public class ReaderContext implements Context {
    @Override
    public void accept(Section section) {
        section.visit(this);
    }
}
