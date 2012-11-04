/* Copyright (c) 2009 Lukas Lalinsky
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

package org.musicbrainz.search.servlet;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read an input file containing each search NOT encoded , and then parses each one runs a lucene search on each one.
 * The server is self contained not run within a servlet which make ist much easier to profile the lucene code
 * if you need to.
 *
 * Also necessary to provide the location of the indexes, you can optionally specify where to read the indexes using
 * memory mapped option.
 *
 * Currently only supports the ws/1 namespace
 */
public class TestSearchOnly {

    final static String WS_VERSION_1 = "1";
    final static Logger log = Logger.getLogger(TestSearchOnly.class.getName());

    final static int DEFAULT_OFFSET = 0;
    final static int DEFAULT_MATCHES_LIMIT = 25;
    final static int MAX_MATCHES_LIMIT = 100;

    final static String CHARSET = "UTF-8";
    private static EnumMap<ResourceType, AbstractSearchServer> searchers = new EnumMap<ResourceType, AbstractSearchServer>(ResourceType.class);
    private String initMessage = null;
    static long totalQueryTime = 0;
    static Map<URL, Integer> map = new LinkedHashMap<URL, Integer>();

    public static void main(String[] args) throws Exception {

        Thread.sleep(30000);
        Options options = new Options();
        CmdLineParser parser = new CmdLineParser(options);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            System.err.println("Couldn't parse command line parameters");
            parser.printUsage(System.out);
            System.exit(1);
        }

        if ((options.getIndexesDir().equals("")) || (options.getTestFile().equals(""))) {
            System.err.println("Require both Index Dir and Test File");
            System.exit(1);
        }


        //Init
        boolean useMMapDirectory = options.isMMap();
        File indexDir = new File(options.getIndexesDir());
        File urlFile = new File(options.getTestFile());


        // Initialize all search indexes
        for (ResourceType resourceType : ResourceType.values()) {

            File indexFileDir = new File(indexDir + System.getProperty("file.separator") + resourceType.getIndexName() + "_index");

            AbstractSearchServer searchServer = null;
            try {

                Directory directory = useMMapDirectory ? new MMapDirectory(indexFileDir) : new NIOFSDirectory(indexFileDir);
                IndexSearcher indexSearcher = new IndexSearcher(IndexReader.open(directory));
                searchServer = resourceType.getSearchServerClass().getConstructor(IndexSearcher.class).newInstance(indexSearcher);

            } catch (CorruptIndexException e) {
                log.warning("Could not load " + resourceType.getIndexName() + " index, index is corrupted: " + e.getMessage());
            } catch (IOException e) {
                log.warning("Could not load " + resourceType.getIndexName() + " index: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchers.put(resourceType, searchServer);
        }

        processUrls(urlFile);
    }


    private static void processUrls(File urlFile) throws Exception {
        String input;
        BufferedReader in = new BufferedReader(new FileReader(urlFile));
        while ((input = in.readLine()) != null) {
            map.put(new URL(input), null);
        }
        in.close();

        for (URL url : map.keySet()) {
            processURL(url);
        }
        System.out.println("Total Query TIme was:"+totalQueryTime + "Ms");
    }


    protected static void processURL(URL url)
            throws Exception {


        Pattern p = Pattern.compile("/ws/1/(.*)/");
        Matcher m = p.matcher(url.getPath());
        m.find();
        if (!m.matches()) {
            return;
        }
        String type = m.group(1);
        // V1 Compatability
        if (type.equals("track")) {
            type = ResourceType.RECORDING.getName();
        }
        ResourceType resourceType = ResourceType.getValue(type);

        Pattern p2 = Pattern.compile("query=(.*)&offset.*");
        Matcher m2 = p2.matcher(url.getQuery());
        m2.find();
        if (!m2.matches()) {
            return;
        }
        String query = m2.group(1);

        String responseVersion = WS_VERSION_1;
        Integer offset = 0;
        Integer limit = DEFAULT_MATCHES_LIMIT;


        // Make the search
        long start = System.nanoTime();
        SearchServer searchServer = searchers.get(resourceType);
        Results results = searchServer.search(query, offset, limit);
        org.musicbrainz.search.servlet.ResultsWriter writer = searchServer.getWriter(responseVersion);
        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new NullOutputStream(), CHARSET)));
        writer.write(out, results, "xml", false);
        out.close();
        long end = System.nanoTime();
        long queryInMs = ((end - start) / 1000000 );
        totalQueryTime+=queryInMs;
        //System.out.println(URLDecoder.decode(url.toString(),"UTF8"));
        System.out.println("url ok :" + URLDecoder.decode(url.toString(), "UTF8") + ":" + results.totalHits + ":" + queryInMs + " ms");
        //System.out.println(((end - start) / 1000000));

    }

    static class NullOutputStream extends OutputStream {
        public void write(int i) throws java.io.IOException {

        }

        public void write(byte[] bytes) throws java.io.IOException {

        }

        public void write(byte[] bytes, int i, int i1) throws java.io.IOException {

        }
    }

    static class Options {

        // Indexes directory
        @Option(name = "--indexes-dir", aliases = {"-d"}, usage = "The directory storing the indexes")
        private String indexesDir = "";

        public String getIndexesDir() {
            return indexesDir;
        }

        // File containing Test urls
        @Option(name = "--testfile", aliases = {"-f"}, usage = "Test file containing one search url each line")
        private String testFile = "";

        public String getTestFile() {
            return testFile;
        }

        // Test mode
        @Option(name = "--mmap", aliases = {"-m"}, usage = "Memory Map Indexes")
        private boolean mmap = false;

        public boolean isMMap() {
            return mmap;
        }


    }

}




