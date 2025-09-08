package org.minerift.ether.database.bin;

import java.io.File;
import java.io.IOException;

public class BinaryPlayground {

    public static void main(String[] args) throws IOException {

        File file = new File("C:\\Users\\avaer\\Desktop\\nuetherdb.dat");
        if(!file.exists()) {
            file.createNewFile();
        }

        /*

            Anatomy of the database:
            - Each table is a separate file
            - Database name is the directory name
            - Each db file has:
                - header (magic, file version, contents (no section name))
                - jump table for sections? (EXCLUDE THIS)
                - table info (name, columns, etc.)
                - jump table for rows
                - btrees
                - rows (objects)

            - each section will store section size + section contents, excluding header



            Section<SectionDataType> contains size, extended to contain actual section data
            SectionDataType -> Header extends SectionData, JumpTable extends SectionData, etc.

            - Each section will have an Order defining the order to be read/written
            - ~~(DataType, (val) -> what to do with val?)~~

            Order of Associations:
            - RefAssociation
            - FieldAssociation

            These associations will allow for getting/setting of whatever is needed 


         */

    }

}
