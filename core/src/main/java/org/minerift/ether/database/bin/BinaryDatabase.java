package org.minerift.ether.database.bin;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.bin.sections.FlatSection;
import org.minerift.ether.database.bin.sections.Header;
import org.minerift.ether.database.bin.sections.Section;
import org.minerift.ether.database.bin.sections.Table;
import org.minerift.ether.database.bin.writer.BinaryWriterContext;

import java.io.File;
import java.io.IOException;

import static org.minerift.ether.database.bin.Ref.ref;

public class BinaryDatabase {

    // TODO: on binary db init, initialize all codecs for data types
    // NOTE: for binary db, no data types should ever need fallbacks; that is intended for handling SQL dialect differences

    public static final byte[] FILE_MAGIC = new byte[] { 69, 85, 82, 69, 75, 65, 33 }; // EUREKA!

    /**
     *  - Table:
     *      - Size
     *      - Name
     *      - Field: name, type, flags, (optional: bitmap)
     *      - Records/Objects:
     *
     */

    public static void main(String[] args) throws IOException {
        File file = new File("C:\\Users\\avaer\\Desktop\\etherdb.dat");
        if(!file.exists()) {
            file.createNewFile();
        }
        BinaryContext ctx = new BinaryWriterContext(file);

        /*
            Header: magic (separate method to handle specifically), header size (excluding magic), database name, tables count, tables start, table names, table offsets
            Table: fields subsect byte size, field count, Field(field size, field name, field type, field flags, field bitmap size, field bitmap (only if nulls allowed)),
                   btrees subsect byte size, btrees count, btrees indexed field idxs, btrees start, btrees offsets,
                   entries subsect byte size, entry count, entry start, entry offsets
            BTree: (TBD)
            Records: (based on fields; if field is nullable and null bitmap indicates null field entry, skip)
        */

        /*
            In the scenario where we are reading a whole table, read magic + header
            - Based on table name, iterate through tables and find offset
            - Reposition buffer to tables start + offset
            - Load fields subsection from Table section
            - Load btrees byte size, but skip
            - Load entries subsection from Table section
        */

        // TODO: handle Header creation based on BinaryContext type (reader will have an empty header, writer will have a header to
        Header header = new Header();
        if(ctx.isWriter()) { // TODO: temporary until we can remove this
            header.magic.set(FILE_MAGIC);
            header.dbName.set("Ether");
            header.tableNames.set(new String[] { "Islands", "Users", "UpgradeData" });
            header.tableCount.set((short) header.tableNames.get().length);
            header.tableOffsets.set(new int[] { 420, 69, 1738 });
        }

        Table table = new Table();
        Section tableSec = new FlatSection("tables");
        tableSec.contents(() -> {
            tableSec.add(DataType.INT, table.fieldCount);
            /*if(ctx.isReader() && table.entries.get().size() != table.entryCount()) {
                tableSec.add(); // read entry offsets
            }*/
        });

        /*
            For handling record writing, each record to be written should be a subsection that is then submitted to be written.
            For handling record reading, each record to be read should be a subsection ...



        */

        Section recordsSec = new FlatSection("records");
        recordsSec.contents(() -> {
            /*for(var record : ctx.records.submittedRecords) {
                Section recSec = new FlatSection();
                /*for(record) {

                }//
                recordsSec.addSubsection(recSec);
            }*/
        });


        ctx.beginSection("header");
        ctx.bin(DataType.BINARY(FILE_MAGIC.length), header.magic);
        if(header.magic.get() != FILE_MAGIC) {
            throw new RuntimeException("Incorrect file format: magic does not match");
        }

        // TODO: for lazy initialization of sections, submit a Supplier<Section> to the context (some sections may not need to be created)

        ctx.markSectionSize();
        ctx.bin(DataType.SHORT, header.size);
        ctx.bin(DataType.VARCHAR, header.dbName);
        ctx.bin(DataType.SHORT, header.tableCount);
        ctx.bin(DataType.VARCHAR.array(), header.tableNames, header.tableCount);
        ctx.bin(DataType.INTS, header.tableOffsets, header.tableCount);
        ctx.markSectionDone();
        // TODO: for table offsets, we will need to know the section sizes beforehand, so a Section structure is necessary for storing this data.
        // TODO: because everything will be written together, markSectionDone will have to add the section to a LinkedList that can be used later for compiling everything after.
        // TODO: each record/object stored will be its own section


        // TODO: LinkedHashMap for storing Sections in order, with each Section storing the types/refs in a LinkedList.
        // TODO: Each section will also be able to store subsections (there will also be a FlatSection type for storing sections more simply (no need for subsections))
        // TODO: Each section can calculate its byte size for use for size or offset storage



        // DEBUG
        ctx.bin(DataType.INT, ref(5));
        ctx.bin(DataType.BOOL, ref(true));
        ctx.markSectionDone();

        ctx.bin(DataType.BIGINT, ref(Long.MAX_VALUE));
        ctx.bin(DataType.VARCHAR, ref("Goodbye, world!"));
        ctx.markSectionDone();

        ctx.markComplete();
    }

}
