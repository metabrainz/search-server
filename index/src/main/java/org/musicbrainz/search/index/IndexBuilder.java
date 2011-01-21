/* Copyright (c) 2009 Lukas Lalinsky
 * Copyright (c) 2009 Aurelien Mino
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.index;

import org.apache.commons.lang.time.StopWatch;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IndexBuilder
{

    public static void main(String[] args) throws SQLException, IOException
    {

        IndexOptions options = new IndexOptions();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println("Couldn't parse command line parameters");
            parser.printUsage(System.out);
            System.exit(1);
        }

        // On request, print command line usage
        if (options.isHelp()) {
            parser.printUsage(System.out);
            System.exit(1);
        }
        
        if (options.isTest()) { System.out.println("Running in test mode."); }

        // At least one index should have been selected 
        ArrayList<String> selectedIndexes = options.selectedIndexes();
        if (selectedIndexes.size() == 0 
              || (selectedIndexes.size() == 1 && selectedIndexes.contains(""))) { 
            System.out.println("No indexes selected. Exiting.");
            System.exit(1);
        }

        Connection mainDbConn = null;
        Connection rawDbConn = null; 
        
        // Check that FreeDB is not the only index requested for build
        if (options.selectedIndexes().size() > 1 || !options.buildIndex("freedb")) {

            // Try loading PostgreSql driver
            try {
                Class.forName("org.postgresql.Driver");
            }
            catch (ClassNotFoundException e) {
                System.err.println("Couldn't load org.postgresql.Driver");
                System.exit(1);
            }

            // Connect to main database
            mainDbConn = options.getMainDatabaseConnection();

            // Connect to raw database
			rawDbConn = options.getRawDatabaseConnection();
        }
    
        StopWatch clock = new StopWatch();

        // MusicBrainz data indexing
        DatabaseIndex[] indexes = {
                new ArtistIndex(mainDbConn),
                new ReleaseIndex(mainDbConn,options.getCacheType()),
                new ReleaseGroupIndex(mainDbConn),
                new RecordingIndex(mainDbConn, options.getCacheType()),
                new LabelIndex(mainDbConn),
                new WorkIndex(mainDbConn),
                new AnnotationIndex(mainDbConn),
                new TagIndex(mainDbConn),
                new CDStubIndex(rawDbConn), //Note different db
        };

        List<String> indexesToBeBuilt = new ArrayList<String>();
        for (DatabaseIndex index : indexes) {

            // Check if this index should be built
            if (options.buildIndex(index.getName())) {
                indexesToBeBuilt.add(index.getName());
            }
        }

        //Create temporary tables used by multiple indexes
        CommonTables commonTables = new CommonTables(mainDbConn, options.getCacheType(), indexesToBeBuilt);
        commonTables.createTemporaryTables();

        for (DatabaseIndex index : indexes) {

            // Check if this index should be built
            if (!options.buildIndex(index.getName())) {
                System.out.println("Skipping index: " + index.getName());
                continue;
            }

            clock.start();
            buildDatabaseIndex(index, options);
            clock.stop();
            System.out.println("  Finished in " + Float.toString(clock.getTime()/1000) + " seconds");
            clock.reset();
        }

        // FreeDB data indexing
        if(options.buildIndex("freedb")) {

            File dumpFile = new File(options.getFreeDBDump());
            //If they have set freedbdump file 
            if (options.getFreeDBDump() != null && options.getFreeDBDump().length()!=0)  {
                if( dumpFile.isFile()) {
                    clock.start();
                    buildFreeDBIndex(dumpFile, options);
                    clock.stop();
                    System.out.println("  Finished in " + Float.toString(clock.getTime()/1000) + " seconds");
                } else {
                    System.out.println("  Can't build FreeDB index: invalid file "+options.getFreeDBDump());
                }
            }
        }
    }

    /**
     * Build an index from database
     * 
     * @param options
     * @throws IOException 
     * @throws SQLException 
     */
    private static void buildDatabaseIndex(DatabaseIndex index, IndexOptions options) throws IOException, SQLException
    {
        IndexWriter indexWriter;
        String path = options.getIndexesDir() + index.getFilename();
        System.out.println("Started Building index: " + path + " at "+new Date());

        /* All addDocuments request are put on a queue to allow another query to be made to database without waiting
         * for all added documents to be analysed, queue is serviced by available processer no of threads.
         * If the max query outperforms the lucene analysis then analysis will switch to main thread because
         * the pool queue size cannot be larger than the max number of documents returned from one query.
         * Will get best results on multicpu systems accessing database on another system.
         */
        indexWriter = new ThreadedIndexWriter(FSDirectory.open(new File(path)),
                index.getAnalyzer(),
                true,
                Runtime.getRuntime().availableProcessors(),
                options.getDatabaseChunkSize(),
                IndexWriter.MaxFieldLength.LIMITED);

        indexWriter.setMaxBufferedDocs(IndexOptions.MAX_BUFFERED_DOCS);
        indexWriter.setMergeFactor(IndexOptions.MERGE_FACTOR);

        index.init(indexWriter, false);
        index.addMetaInformation(indexWriter);
        int maxId = index.getMaxId();
        if (options.isTest() && options.getTestIndexSize() < maxId)
            maxId = options.getTestIndexSize();
        int j = 0;
        while (j <= maxId) {
        	int k = Math.min(j + options.getDatabaseChunkSize() - 1, maxId);
            System.out.print("  Indexing " + j + "..." + k + " / " + maxId + " (" + (100*k/maxId) + "%)\r");
            index.indexData(indexWriter, j, k);
            j += options.getDatabaseChunkSize();
        }

        index.destroy();
        System.out.println("\n  Started Optimizing at "+new Date());
        indexWriter.optimize();
        indexWriter.close();

        // For debugging to check sql is not creating too few/many rows
        if(true) {
            int dbRows = index.getNoOfRows(maxId);
            IndexReader reader = IndexReader.open(FSDirectory.open(new File(path)),true);
            System.out.println("  Indexed "+dbRows+" rows, creating "+(reader.maxDoc() - 1)+" lucene documents");
            reader.close();
        }
        System.out.println("  Completed Optimizing at "+new Date());

    }
    
    /**
     * Build a FreeDB index from a FreeDB dump
     * 
     * @param dumpFile FreeDB dump file
     * @param options
     * @throws IOException 
     */
    private static void buildFreeDBIndex(File dumpFile, IndexOptions options) throws IOException
    {
        FreeDBIndex index = new FreeDBIndex();
        index.setDumpFile(dumpFile);

        IndexWriter indexWriter;
        String path = options.getIndexesDir() + index.getFilename();
        System.out.println("Building index: " + path);
        indexWriter = new IndexWriter(FSDirectory.open(new File(path)), index.getAnalyzer() , true, IndexWriter.MaxFieldLength.LIMITED);
        indexWriter.setMaxBufferedDocs(IndexOptions.MAX_BUFFERED_DOCS);
        indexWriter.setMergeFactor(IndexOptions.MERGE_FACTOR);

        index.addMetaInformation(indexWriter);
        index.indexData(indexWriter);

        System.out.println("  Optimizing");
        indexWriter.optimize();
        indexWriter.close();
    }
    
}
