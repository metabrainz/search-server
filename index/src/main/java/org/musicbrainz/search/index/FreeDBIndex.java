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
import org.apache.lucene.util.NumericUtils;
import org.mozilla.universalchardet.UniversalDetector;
import org.musicbrainz.search.MbDocument;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.*;

public class FreeDBIndex implements Index {

    private int emptyCount=0;
    private int failedCount=0;


    private Set<String>                 unknownCharsets = new HashSet<String>();
    private Map<String,CharsetDecoder>  decoderMap      = new HashMap<String,CharsetDecoder> ();
    private Map<String,Integer>         countMap        = new TreeMap<String,Integer> ();

    private void initDecoders() {
        CharsetDecoder decoder;
        decoder = Charset.forName("UTF8").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        decoderMap.put("UTF8",decoder);
        countMap.put("UTF8",0);

        decoder = Charset.forName("ISO-8859-1").newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        decoderMap.put("ISO-8859-1", decoder);
        countMap.put("ISO-8859-1",0);

    }

    public FreeDBIndex() {
        initDecoders();
    }

	@Override
	public void addMetaInformation(IndexWriter indexWriter) throws IOException {
    	MbDocument doc = new MbDocument();
        doc.addField(MetaIndexField.LAST_UPDATED, NumericUtils.longToPrefixCoded(new Date().getTime()));
        indexWriter.addDocument(doc.getLuceneDocument());
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
        return DatabaseIndex.getAnalyzer(FreeDBIndexField.class);
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

        for(Map.Entry<String,Integer> charsetCounter:countMap.entrySet())
        {
            System.out.println("No of "+charsetCounter.getKey()+ " entries "+charsetCounter.getValue());
        }
        System.out.println("  No of empty entries " + emptyCount);
        System.out.println("  No of failed entries " + failedCount);

    }


    /**
     * Uses heuristics to identify the most likely encoding for the data
     *
     * @param content
     * @return
     */
    private String detectCharset(byte[] content)
    {
        UniversalDetector detector = new UniversalDetector(null);
        detector.handleData(content, 0, content.length);
        detector.dataEnd();
        String charSet = detector.getDetectedCharset();
        detector.reset();
        return charSet;
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
    private Document parseEntryAndCreateDocument(String entryName, String category, byte[] content, CharsetDecoder cd, String charsetName) {
        MbDocument doc = new MbDocument();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content), cd));

        cd.reset();

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
                    if(discid.contains(","))
                    {
                        discid=discid.substring(0,discid.indexOf(",") - 1);
                    }
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
            if(tmp[0].length()>=7) {
                numTracks = new Integer(tmp[0].substring(6)) + 1;
            }
            else {
                System.err.println("Value of lastTrack cannot be parsed is:"+lastTrack);
            }
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
        countMap.put(charsetName, Integer.valueOf(countMap.get(charsetName).intValue()+1));
        return doc.getLuceneDocument();
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

        CharsetDecoder decoder=null;
        String charsetName = detectCharset(content);
        if(charsetName!=null)
        {
            decoder = decoderMap.get(charsetName);
            if(decoder==null)
            {
                if(!unknownCharsets.contains(charsetName))
                {
                    Charset charset = Charset.forName(charsetName);
                    if(charset!=null)
                    {
                        CharsetDecoder charsetDecoder = charset.newDecoder();
                        charsetDecoder.onMalformedInput(CodingErrorAction.REPORT);
                        charsetDecoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                        decoderMap.put(charsetName,charsetDecoder);
                        countMap.put(charsetName,0);
                        decoder=charsetDecoder;
                    }
                }
            }
        }

        if(decoder!=null)
        {
            Document doc = parseEntryAndCreateDocument(entryName, category, content, decoder,charsetName);
            if (doc != null) {
                return doc;
            }
            return doc;
        }


        //Is it UTF8 then
        charsetName="UTF8";
        Document doc = parseEntryAndCreateDocument(entryName, category, content, decoderMap.get(charsetName),charsetName);
        if (doc != null) {
            return doc;
        }

        //If not try ISO-8859-1
        charsetName="ISO-8859-1";
        doc = parseEntryAndCreateDocument(entryName, category, content, decoderMap.get(charsetName),charsetName);
        if (doc != null) {
            return doc;
        }

        failedCount++;
        return null;
    }

}
