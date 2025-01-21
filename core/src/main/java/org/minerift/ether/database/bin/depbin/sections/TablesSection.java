package org.minerift.ether.database.bin.depbin.sections;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.bin.depbin.reader.ReaderContext;
import org.minerift.ether.database.bin.depbin.sections.data.TableMetadata;
import org.minerift.ether.database.bin.depbin.writer.WriterContext;

import java.util.LinkedHashMap;
import java.util.Map;

public class TablesSection implements Section {

    public Map<String, TableMetadata> tables;

    @Override
    public void visit(WriterContext ctx) {
        this.tables = new LinkedHashMap<>(ctx.db.getModels().size());
        for(var entry : ctx.db.getModels()) {
            TableMetadata table = new TableMetadata();
            tables.put(table.name, table);


            // TODO: for IO stuffs, research Reader and Writer std java classes (reference Gson) and use BufferedWriter/Reader (look into)
            // TODO: should probably only allow fixed size types to be written later
            ctx.write(DataType.INT, table.size); // section size
            //ctx.write(DataType.INT, table.);
            ctx.write(DataType.INT, table.fieldCount);
            // TODO: write fields now
        }
    }

    @Override
    public void visit(ReaderContext ctx) {

    }
}
