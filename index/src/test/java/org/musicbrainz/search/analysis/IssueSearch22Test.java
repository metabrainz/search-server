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

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;


/** Test that analyser treats No.x and No. x the same (where x can be any number) , because both forms are found
 * in the database.
 */
public class IssueSearch22Test extends TestCase {


    public void testShowIncorrectPhraseQueriesWithNonPosGapAnalyzer() throws Exception {

        Analyzer analyzer = new TitleAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "it's time to pay income tax", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "return library books on time", Field.Store.YES, Field.Index.ANALYZED));
                
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser(Version.LUCENE_CURRENT,"name", analyzer).parse("\"pay income tax return\"");
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    public void testShowCorrectPhraseQueriesWithPosGapAnalyzer() throws Exception {

        Analyzer analyzer = new TitleWithPosGapAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "it's time to pay income tax", Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("name", "return library books on time", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser(Version.LUCENE_CURRENT,"name", analyzer).parse("\"pay income tax return\"");
            assertEquals(0, searcher.search(q,10).totalHits);
        }

    }

}