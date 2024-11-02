package org.minerift.ether.database.nubin;

import java.io.File;
import java.io.IOException;

public class BinaryPlayground {

    public static void main(String[] args) throws IOException {

        File file = new File("C:\\Users\\avaer\\Desktop\\nuetherdb.dat");
        if(!file.exists()) {
            file.createNewFile();
        }



    }

}
