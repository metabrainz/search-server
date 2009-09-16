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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

public class FreeDBIndex {

	protected static String[] CATEGORIES = {"data", "folk", "jazz", "misc", "rock", "country", 
		"blues", "newage", "reggae", "classical", "soundtrack"};

	protected File dumpFile;
	
	public File getDumpFile() {
		return dumpFile;
	}

	public void setDumpFile(File dumpFile) {
		this.dumpFile = dumpFile;
	}

	public String getName() {
		return "freedb";
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
				if(indexCategory) {
					System.out.println("  Indexing category: " + category);
				} else if (!".".equals(category)){
					System.out.println("  Skipping category: " + category);
				}
			} else {
				if(!indexCategory) continue;
				
				byte[] content = new byte[(int) entry.getSize()];
				tarIn.read(content, 0, (int) entry.getSize());
				Document doc = documentFromFreeDBEntry(category, content);
				if(doc != null) {
					indexWriter.addDocument(doc);
				}
			}
		}
	}
	
	protected Document documentFromFreeDBEntry(String category, byte[] content) throws IOException {
		Document doc = new Document();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
		
		String title = "";
		String artist = "";
		String release = "";
		String discid = "";
		String year = "";
		String lastTrack = "";
		Integer numTracks = 0;
		
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.startsWith("DTITLE=")) title += line.substring(7);
			if (line.startsWith("DISCID=")) discid = line.substring(7);
			if (line.startsWith("DYEAR=")) year = line.substring(6);
			if (line.startsWith("TTITLE")) lastTrack = line;
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
		} catch(Exception e) {
			return null;
		}
		
		Index.addFieldToDocument(doc, FreeDBIndexField.ARTIST, artist);
		Index.addFieldToDocument(doc, FreeDBIndexField.TITLE, release);
		Index.addFieldToDocument(doc, FreeDBIndexField.DISCID, discid);
		Index.addFieldToDocument(doc, FreeDBIndexField.CATEGORY, category);
		Index.addFieldToDocument(doc, FreeDBIndexField.YEAR, year);
		Index.addFieldToDocument(doc, FreeDBIndexField.TRACKS, numTracks.toString());
		
		return doc;
	}
}
