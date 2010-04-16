/* Copyright (c) 2009 Aurélien Mino
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

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;

public class FreeDBIndex implements Index {

    private int emptyCount=0;
    private int failedCount=0;
    private int utfCount =0;
    private int iso8859Count =0;


    private CharsetDecoder utf8Decoder;
    private CharsetDecoder iso8859Decoder;

    private void initDecoders() {

        //UTF8 Format
        utf8Decoder = Charset.forName("UTF8").newDecoder();
        utf8Decoder.onMalformedInput(CodingErrorAction.REPORT);
        utf8Decoder.onUnmappableCharacter(CodingErrorAction.REPORT);

        //ISO8859 Format
        iso8859Decoder = Charset.forName("ISO-8859-1").newDecoder();
        iso8859Decoder.onMalformedInput(CodingErrorAction.REPORT);
        iso8859Decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    public FreeDBIndex() {
        initDecoders();
    }

    /* This is appended to the getName() method of each index to create the index folder  */
    private static final String INDEX_SUFFIX = "_index";


    protected static String[] CATEGORIES = {"data", "folk", "jazz", "misc", "rock", "country",
            "blues", "newage", "reggae", "classical", "soundtrack"};

    protected File dumpFile;

    public File getDumpFile() {
        return dumpFile;
    }

    public void setDumpFile(File dumpFile) {
        this.dumpFile = dumpFile;
    }

    public Analyzer getAnalyzer() {
        return new PerFieldEntityAnalyzer(FreeDBIndexField.class);
    }

    public String getName() {
        return "freedb";
    }

    public String getFilename() {
        return getName() + INDEX_SUFFIX;
    }

    public void indexData(IndexWriter indexWriter) throws IOException {

        // Create the archive input stream from the dump, assuming it's a tar.bz2 file
        BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(dumpFile));
        BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(fileInput);
        TarArchiveInputStream tarIn = new TarArchiveInputStream(bzIn);

        String category = "";
        boolean indexCategory = false;
        ArchiveEntry entry;
        while ((entry = tarIn.getNextEntry()) != null) {
            if (entry.isDirectory()) {
                category = entry.getName().replace("/", "");

                // Determine if this category should be indexed
                indexCategory = Arrays.asList(CATEGORIES).contains(category);
                if (indexCategory) {
                    System.out.println("  Indexing category: " + category);
                } else if (!".".equals(category)) {
                    System.out.println("  Skipping category: " + category);
                }
            } else {
                if (!indexCategory) continue;

                byte[] content = new byte[(int) entry.getSize()];
                int numBytesRead = tarIn.read(content, 0, (int) entry.getSize());
                if(numBytesRead!=entry.getSize())
                {
                    emptyCount++;
                    //System.out.println("  " + entry.getName() + " Skipping because unable to read bytes");
                }
                else
                {
                    Document doc = documentFromFreeDBEntry(entry.getName(), category, content);
                    if (doc != null) {
                        indexWriter.addDocument(doc);
                    }
                }
            }
        }

        System.out.println("  No of UTF8 entries " + utfCount);
        System.out.println("  No of ISO-8859-1 entries " + iso8859Count);
        System.out.println("  No of empty entries " + emptyCount);
        System.out.println("  No of failed entries " + failedCount);

    }

    /**
     * Parse release using specified charset decoder
     *
     * @param entryName
     * @param category
     * @param content
     * @param cd
     * @return
     */
    private MbDocument parseEntryAndCreateDocument(String entryName, String category, byte[] content, CharsetDecoder cd) {
        MbDocument doc = new MbDocument();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content), cd));

        String title = "";
        String artist = "";
        String release = "";
        String discid = "";
        String year = "";
        String lastTrack = "";
        Integer numTracks = 0;


        String line;

        try {
            while ((line = reader.readLine()) != null) {

                if (line.startsWith("DTITLE=")) {
                    title += line.substring(7);
                }
                if (line.startsWith("DISCID=")) {
                    discid = line.substring(7);
                }
                if (line.startsWith("DYEAR=")) {
                    year = line.substring(6);
                }
                if (line.startsWith("TTITLE")) {
                    lastTrack = line;
                }
            }
        }
        catch (IOException e) {
            return null;
        }

        try {
            String[] tmp;
            // Extract artist and release title
            tmp = title.split(" / ");
            if (tmp != null && tmp.length >= 2) {
                artist = tmp[0].trim();
                release = tmp[1].trim();
            }

            // Determine number of tracks
            tmp = lastTrack.split("=");
            numTracks = new Integer(tmp[0].substring(6)) + 1;
        } catch (Exception e) {
            System.err.println("  "+entryName+ " Unable to determine no of tracks from " + lastTrack);
            e.printStackTrace();
            return null;
        }



        doc.addField(FreeDBIndexField.ARTIST, artist);
        doc.addField(FreeDBIndexField.TITLE, release);
        doc.addField(FreeDBIndexField.DISCID, discid);
        doc.addField(FreeDBIndexField.CATEGORY, category);
        doc.addField(FreeDBIndexField.YEAR, year);

        //TODO should really index as number
        doc.addField(FreeDBIndexField.TRACKS, numTracks.toString());
        return doc;
    }

    /**
     * Process one release
     *
     * @param category
     * @param content
     * @return
     * @throws IOException
     */
    protected Document documentFromFreeDBEntry(String entryName, String category, byte[] content) {

        //Is it UTF8
        utf8Decoder.reset();
        MbDocument doc = parseEntryAndCreateDocument(entryName, category, content, utf8Decoder);
        if (doc != null) {
            //System.out.println("  Processed entry as utf8:" +entryName);
            utfCount++;
            return doc.getLuceneDocument();
        }

        //If not try ISO-8859-1
        iso8859Decoder.reset();
        doc = parseEntryAndCreateDocument(entryName, category, content, iso8859Decoder);
        if (doc != null) {
            //System.out.println("  Processed entry as iso8859-1:"+entryName);
            iso8859Count++;
            return doc.getLuceneDocument();
        }


        //System.out.println("  " + entryName + " Skipping because unable to decode");
        failedCount++;
        return null;
    }

}
