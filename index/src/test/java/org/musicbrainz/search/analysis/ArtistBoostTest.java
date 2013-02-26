/*
 Copyright (c) 2010 Paul Taylor
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. Neither the name of the MusicBrainz project nor the names of the
     contributors may be used to endorse or promote products derived from
     this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.index.ArtistBoostDoc;
import org.musicbrainz.search.index.ArtistIndexField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test wildcard search cshold match if search/field contains exclamation mark
 *
 */
public class ArtistBoostTest
{

    @Test
    public void testArtistBoostWorkingForComposers() throws Exception {


        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);

        //THis field has anid that means it gets doc boost
        Document doc = new Document();
        doc.add(new Field(ArtistIndexField.ARTIST_ID.getName(), "24f1766e-9635-4d58-a4d4-9413f9f98a4c", Field.Store.YES,	Field.Index.ANALYZED));
        doc.add(new Field(ArtistIndexField.ARTIST.getName(), "Johann Sebastian Bach", Field.Store.YES,	Field.Index.ANALYZED));
        doc.add(new Field(ArtistIndexField.ALIAS.getName(), "Bach", Field.Store.YES,	Field.Index.ANALYZED));
        ArtistBoostDoc.boost("24f1766e-9635-4d58-a4d4-9413f9f98a4c",doc);
        writer.addDocument(doc);

        //this does not
        doc = new Document();
        doc.add(new Field(ArtistIndexField.ARTIST_ID.getName(), "9cefb3f2-763c-47a3-bc1e-86f1f35206f0", Field.Store.YES,	Field.Index.ANALYZED));
        doc.add(new Field(ArtistIndexField.ARTIST.getName(), "bach", Field.Store.YES,	Field.Index.ANALYZED));
        doc.add(new Field(ArtistIndexField.ALIAS.getName(), "bach", Field.Store.YES,	Field.Index.ANALYZED));
        ArtistBoostDoc.boost("9cefb3f2-763c-47a3-bc1e-86f1f35206f0",doc);
        writer.addDocument(doc);

        writer.close();

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, ArtistIndexField.ALIAS.getName(), analyzer).parse("Bach");
            TopDocs td = searcher.search(q,10);
            assertEquals(2, td.totalHits);
            for(ScoreDoc sd:td.scoreDocs)
            {
                System.out.println(sd);
            }
            assertTrue(td.scoreDocs[0].score - (td.scoreDocs[1].score * 2) >  -0.2f);

        }

    }
}