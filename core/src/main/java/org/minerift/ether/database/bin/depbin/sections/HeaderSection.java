package org.minerift.ether.database.bin.depbin.sections;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.bin.depbin.reader.ReaderContext;
import org.minerift.ether.database.bin.depbin.writer.WriterContext;
import org.minerift.ether.database.Ref;

import java.util.Map;

import static org.minerift.ether.util.Utils.enumerateMap;

public class HeaderSection implements Section {

    public static final byte[] MAGIC = new byte[] { 69, 85, 82, 69, 75, 65, 33 }; // EUREKA!

    @Override
    public void visit(WriterContext ctx) {
        ctx.write(DataType.BINARY(MAGIC.length), MAGIC);

        Data data = new Data(ctx.db.getName(), ctx.db.getModelsMap());

        ctx.write(DataType.SHORT, data.size).referenceSize(data.size);
        ctx.write(DataType.VARCHAR, data.dbName).referenceSize(data.size);
        ctx.write(DataType.SHORT, data.tableCount).referenceSize(data.size);
        ctx.write(DataType.VARCHAR.array(), data.tableNames, data.tableCount).referenceSize(data.size);
        //ctx.write(DataType.INT, data.tablesStart);
        ctx.write(DataType.INTS, data.tableOffsets, data.tableCount).referenceSize(data.size);

        ctx.submit();
    }

    @Override
    public void visit(ReaderContext ctx) {

    }

    public static class Data {
        public Ref<Short> size; // Ref because this is resolved after it is queued to be written
        public String dbName;
        public Ref<Short> tableCount;
        public String[] tableNames;
        public Ref<Integer> tablesStart;
        public Ref<int[]> tableOffsets;

        public Data() {
            this.size = new Ref<>((short)0);
            this.dbName = null;
            this.tableCount = new Ref<>();
            this.tableNames = null;
            this.tablesStart = new Ref<>();
            this.tableOffsets = new Ref<>();
        }

        public Data(String dbName, Map<Class<? extends Model>, Model<?, ?>> models) {
            this.size = new Ref<>();
            this.dbName = dbName;
            this.tableCount = new Ref<>((short) models.size());
            this.tableNames = new String[tableCount.get()];
            this.tablesStart = new Ref<>();
            this.tableOffsets = new Ref<>();
            enumerateMap(models, (i, e) -> tableNames[i] = e.getValue().getTableName());
        }
    }
}
