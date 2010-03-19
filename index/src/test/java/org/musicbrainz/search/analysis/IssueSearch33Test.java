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
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;


/** Test that analyser treats No.x and No. x the same (where x can be any number) , because both forms are found
 * in the database.
 */
public class IssueSearch33Test extends TestCase {


    public void testGetNoMatchWithStandardAnalyzer() throws Exception {

        //Show no token generated
        {
            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("!!!"));
            assertFalse(tokenizer.incrementToken());
        }

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "!!!", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!!!"));
            assertEquals(0, searcher.search(q,10).totalHits);
        }

    }

    public void testGetMatchWithLabelNameAnalyzer() throws Exception {


        //Show token generated
        {
            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("ApostropheApostropheApostrophe"));
            assertTrue(tokenizer.incrementToken());
            TermAttribute term = tokenizer.addAttribute(TermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUM>", type.type());
            assertEquals("ApostropheApostropheApostrophe", term.term());
            assertEquals(0, offset.startOffset());
            assertEquals(30, offset.endOffset());
        }

        Analyzer analyzer = new ArtistNameAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "!!!", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!!!"));
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }


    public void testGetNoMatchWithStandardAnalyzer2() throws Exception {

        //Show no token generated
        {
            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("!\"@.*!%"));
            assertFalse(tokenizer.incrementToken());
        }

        Analyzer analyzer = new StandardUnaccentAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "!\"@*!%", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!\"@*!%"));
            assertEquals(0, searcher.search(q,10).totalHits);
        }

    }

    public void testGetMatchWithLabelNameAnalyzer2() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("ApostropheDoubleQuoteAtAsteriskExclamationPercentage"));
            assertTrue(tokenizer.incrementToken());
            TermAttribute term = tokenizer.addAttribute(TermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUM>", type.type());
            assertEquals("ApostropheDoubleQuoteAtAsteriskExclamationPercentage", term.term());
            assertEquals(0, offset.startOffset());
            assertEquals(52, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Analyse field
        Analyzer analyzer = new LabelNameAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "!\"@*!%", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("apostrophedoublequoteatasteriskexclamationpercentage", tr.term().text());
        assertFalse(tr.next());


        IndexSearcher searcher = new IndexSearcher(dir,true);
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!\"@*!%"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }
}