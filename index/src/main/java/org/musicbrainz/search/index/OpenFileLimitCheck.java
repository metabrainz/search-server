package org.musicbrainz.search.index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Counts how many files can be opened concurrently on the OS
 */
public class OpenFileLimitCheck {
    public static void checkOpenFileLimit() throws IOException {
        List<RandomAccessFile> files = new ArrayList<RandomAccessFile>();
        try
        {
            while(true)
            {
                files.add(new RandomAccessFile("tmp" + files.size(),"rw"));
            }

        }
        catch(IOException ioe)
        {
            System.out.println("IOException after "+ files.size() + " open files:");
            ioe.printStackTrace(System.out);
            int i=0;
            for(RandomAccessFile raf : files)
            {
                raf.close();
                new File("tmp" + i++).delete();
            }
        }

    }
}
