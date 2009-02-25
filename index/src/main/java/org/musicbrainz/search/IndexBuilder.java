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

package org.musicbrainz.search;

import java.util.Properties;
import java.sql.*;
import java.io.*;
import org.apache.lucene.analysis.Analyzer;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

public class IndexBuilder
{
    public static void main(String[] args) throws SQLException, IOException
    {
		String usage = "java -jar index.jar org.musicbrainz.search.IndexBuilder <root_directory>";
		if (args.length == 0) {
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		try {
			Class.forName("org.postgresql.Driver");
		}
		catch (ClassNotFoundException e) {
			System.err.println("Couldn't load org.postgresql.Driver");
			System.exit(1);
		}

		String url = "jdbc:postgresql://localhost/musicbrainz_db_slave";
		Properties props = new Properties();
		props.setProperty("user", "musicbrainz_user");
		props.setProperty("password", "");
		Connection conn = DriverManager.getConnection(url, props);

		Index[] indexes = {
			new TrackIndex(),
			new ReleaseIndex(),
			new ArtistIndex(),
			new LabelIndex(),
		};

		Analyzer analyzer = new StandardUnaccentAnalyzer();

		for (int i = 0; i < indexes.length; i++) {
			Index index = indexes[i];
			IndexWriter indexWriter;
			String path = "data/" + index.getName() + "_index";
			System.out.println("Building index: " + path);
			try {
				indexWriter = new IndexWriter(FSDirectory.getDirectory(path), analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
			}
			catch (java.io.IOException e) {
				return;
				//System.exit(1);
			}
			indexWriter.setMaxBufferedDocs(10000);
			indexWriter.setMergeFactor(3000);

			int maxId = index.getMaxId(conn);
			int j = 0;
			while (j < maxId) {
				System.out.print("  Indexing " + j + "..." + (j + 5000) + " / " + maxId + " (" + (100*j/maxId) + "%)\r");
				index.indexData(indexWriter, conn, j, j + 5000);
				j += 5000;
			}
			System.out.println("\n  Optimizing");
			indexWriter.optimize();
			indexWriter.close();
			System.out.println("  Finished!");
		}



    }
}
