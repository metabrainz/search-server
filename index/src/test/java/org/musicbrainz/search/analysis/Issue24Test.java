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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.musicbrainz.search.LuceneVersion;


/**
 * Test scoring
 */
public class Issue24Test extends TestCase {


    /* Without alias similarity fix , match on docs with multiple matching aliases does better than just one
       matching alias. This is a particular problem for non-latin artists who have a single latin alias amongst
       a large number of non-latin aliases.
     */
     public void testAliasMatchWhenOneOffMany() throws Exception {

        {
            Analyzer analyzer = new StandardUnaccentAnalyzer();

            RAMDirectory dir = new RAMDirectory();
            IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
            Document doc = new Document();
            doc.add(new Field("artist", "fred", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 1", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 2", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 3", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 4", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 5", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 6", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 7", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 8", Field.Store.YES, Field.Index.ANALYZED));

            writer.addDocument(doc);
            doc = new Document();
            doc.add(new Field("artist", "rod", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 1", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 2", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 3", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 4", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 5", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 6", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 7", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 8", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
            writer.close();

            IndexSearcher searcher = new IndexSearcher(dir, true);
            {
                Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "artist", analyzer).parse("alias:rod");

                TopDocs topDocs = searcher.search(q, 10);
                assertEquals(2, topDocs.totalHits);

                System.out.println("Without Fix: Diff "+((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) +"%");
                System.out.println(topDocs.scoreDocs[0].score+":"+topDocs.scoreDocs[0].doc);
                System.out.println(topDocs.scoreDocs[1].score+":"+topDocs.scoreDocs[1].doc);
                System.out.println(searcher.explain(q,topDocs.scoreDocs[0].doc));
                System.out.println(searcher.explain(q,topDocs.scoreDocs[1].doc));
                assertTrue(((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) > 50);

            }
        }

        //With fix, both documents give similar score
        {
            Analyzer analyzer = new StandardUnaccentAnalyzer();

            RAMDirectory dir = new RAMDirectory();
            IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
            writer.setSimilarity(new MusicbrainzSimilarity());
            Document doc = new Document();
            doc.add(new Field("artist", "fred", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 1", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 2", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 3", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 4", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 5", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 6", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 7", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod 8", Field.Store.YES, Field.Index.ANALYZED));

            writer.addDocument(doc);
            doc = new Document();
            doc.add(new Field("artist", "rod", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "rod", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 1", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 2", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 3", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 4", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 5", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 6", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 7", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "fred 8", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
            writer.close();

            IndexSearcher searcher = new IndexSearcher(dir, true);
            searcher.setSimilarity(new MusicbrainzSimilarity());
            {
                Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "artist", analyzer).parse("alias:rod");
                TopDocs topDocs = searcher.search(q, 10);
                assertEquals(2, topDocs.totalHits);
                System.out.println("With Fix: Diff "+((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) +"%");
                System.out.println(topDocs.scoreDocs[0].score+":"+topDocs.scoreDocs[0].doc);
                System.out.println(topDocs.scoreDocs[1].score+":"+topDocs.scoreDocs[1].doc);
                System.out.println(searcher.explain(q,topDocs.scoreDocs[0].doc));
                System.out.println(searcher.explain(q,topDocs.scoreDocs[1].doc));
                assertTrue(((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) < 50);
            }
        }

    }


    /**
     * Without fix, a search on both artist and alias will be distorted by number of aliases - we want to simplify so it
     * is more of a boolean match, a match on an alias field should act as you would expect to act if there was just one alias
     * and there was a match
     * @throws Exception
     */
    public void testAliasesPreferredToArtistHandling() throws Exception {

        //Without alias similarity fix , match on alias fields give much higher score
        {
            Analyzer analyzer = new StandardUnaccentAnalyzer();

            RAMDirectory dir = new RAMDirectory();
            IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
            Document doc = new Document();
            doc.add(new Field("artist", "Vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen and Vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen featuring Vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen feat, Vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen & vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen _ Vanguard", Field.Store.YES, Field.Index.ANALYZED));

            writer.addDocument(doc);
            doc = new Document();
            doc.add(new Field("artist", "queen", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
            writer.close();

            IndexSearcher searcher = new IndexSearcher(dir, true);
            {
                Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "artist", analyzer).parse("artist:queen OR alias:queen");
                
                TopDocs topDocs = searcher.search(q, 10);
                assertEquals(2, topDocs.totalHits);
                System.out.println(topDocs.scoreDocs[0].score+":"+topDocs.scoreDocs[0].doc);
                System.out.println(topDocs.scoreDocs[1].score+":"+topDocs.scoreDocs[1].doc);
                assertTrue(((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) > 50);
                System.out.println(searcher.explain(q,topDocs.scoreDocs[0].doc));
                System.out.println(searcher.explain(q,topDocs.scoreDocs[1].doc));
                System.out.println("Without Fix: Diff "+((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) +"%");


            }
        }

        //With fix, both documents give similar score
        {
            Analyzer analyzer = new StandardUnaccentAnalyzer();

            RAMDirectory dir = new RAMDirectory();
            IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
            writer.setSimilarity(new MusicbrainzSimilarity());
            Document doc = new Document();
            doc.add(new Field("alias", "Queen and Vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen featuring Vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen feat, Vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen & vanguard", Field.Store.YES, Field.Index.ANALYZED));
            doc.add(new Field("alias", "Queen _ Vanguard", Field.Store.YES, Field.Index.ANALYZED));

            writer.addDocument(doc);
            doc = new Document();
            doc.add(new Field("artist", "queen", Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
            writer.close();

            IndexSearcher searcher = new IndexSearcher(dir, true);
            searcher.setSimilarity(new MusicbrainzSimilarity());
            {
                Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "artist", analyzer).parse("artist:queen OR alias:queen");
                TopDocs topDocs = searcher.search(q, 10);
                assertEquals(2, topDocs.totalHits);
                System.out.println(topDocs.scoreDocs[0].score+":"+topDocs.scoreDocs[0].doc);
                System.out.println(topDocs.scoreDocs[1].score+":"+topDocs.scoreDocs[1].doc);
                System.out.println(searcher.explain(q,topDocs.scoreDocs[0].doc));
                System.out.println(searcher.explain(q,topDocs.scoreDocs[1].doc));
                assertTrue(((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) < 50);
                System.out.println("With Fix: Diff "+((topDocs.scoreDocs[0].score / topDocs.scoreDocs[1].score) * 100 - 100) +"%");


            }
        }


    }

}