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
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;

import static org.junit.Assert.*;

/** Test that analyser treats No.x and No. x the same (where x can be any number) , because both forms are found
 * in the database.
 */
public class IssueSearch33Test  {

    @Test
    public void testGetNoMatchWithStandardAnalyzer() throws Exception {

        //Show no token generated with Lucene Standard Tokenizer
        {
            Tokenizer tokenizer = new org.apache.lucene.analysis.standard.StandardTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("!!!"));
            assertFalse(tokenizer.incrementToken());
        }

        //But token is generated with Musicbrainz modification gramar
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("!!!"));
            assertTrue(tokenizer.incrementToken());
        }

        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
            IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "!!!", Field.Store.YES, Field.Index.ANALYZED));

        writer.addDocument(doc);
        writer.close();

        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertNotNull(tr);
        assertNotNull(tr.term());
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("!!!", tr.term().text());

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!!!"));
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    @Test
    public void testGetMatchofPunctuationOnlyField() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("!\"@*!%"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<CONTROLANDPUNCTUATION>", type.type());
            assertEquals("!\"@*!%", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(6, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Another example to show is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("♠!"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<CONTROLANDPUNCTUATION>", type.type());
            assertEquals("♠!", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(2, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }


        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
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
        assertEquals("!\"@*!%", tr.term().text());
        assertFalse(tr.next());


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!\"@*!%"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    @Test
    public void testNormalBehaviourWhenPunctuationAndLetterField() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("fred!!"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
            assertEquals("fred!!", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(6, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "fred!!", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("fred", tr.term().text());
        assertFalse(tr.next());

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("fred"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    @Test
    public void testJavaThaiDefinition()
    {
        assertTrue(Character.isLetterOrDigit('ก'));
        System.out.println(new Integer(new Character('♠')).toString());
    }

    @Test
    public void testGidBehaviour() throws Exception {

        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("bdb24cb5-404b-4f60-bba4-7b730325ae47"));
        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        assertEquals("<NUM>", type.type());
        assertEquals("bdb24cb5-404b-4f60-bba4-7b730325ae47", new String(term.buffer(),0,term.length()));
        assertEquals(0, offset.startOffset());
        assertEquals(36, offset.endOffset());
        assertFalse(tokenizer.incrementToken());

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "bdb24cb5-404b-4f60-bba4-7b730325ae47", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("bdb24cb5-404b-4f60-bba4-7b730325ae47", tr.term().text());
        assertFalse(tr.next());

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer)
                    .parse(QueryParser.escape("bdb24cb5-404b-4f60-bba4-7b730325ae47"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    @Test
    public void testNormalBehaviourWhenPunctuationAndThaiLetterField() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("กข!!"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
            assertEquals("กข!!", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(4, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "กข!!", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("กข", tr.term().text());
        assertFalse(tr.next());

        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("กข"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }


    @Test
    public void testGetMatchMultipleWordPunctuationOnly() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("!\"@* !%"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<CONTROLANDPUNCTUATION>", type.type());
            assertEquals("!\"@*", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(4, offset.endOffset());
            assertTrue(tokenizer.incrementToken());
            assertEquals("<CONTROLANDPUNCTUATION>", type.type());
            assertEquals("!%", new String(term.buffer(),0,term.length()));
            assertEquals(5, offset.startOffset());
            assertEquals(7, offset.endOffset());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "!\"@* !%", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("!\"@*", tr.term().text());
        assertTrue(tr.next());
        assertEquals("name", tr.term().field());
        assertEquals("!%", tr.term().text());


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!\"@* !%"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    @Test
    public void testGetMatchMultipleWordPunctuationOnlyForFirstWord() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("!\"@* fred"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<CONTROLANDPUNCTUATION>", type.type());
            assertEquals("!\"@*", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(4, offset.endOffset());
            assertTrue(tokenizer.incrementToken());
            assertEquals("<ALPHANUM>", type.type());
            assertEquals("fred", new String(term.buffer(),0,term.length()));
            assertEquals(5, offset.startOffset());
            assertEquals(9, offset.endOffset());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "!\"@* fred", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("!\"@*", tr.term().text());
        assertTrue(tr.next());
        assertEquals("name", tr.term().field());
        assertEquals("fred", tr.term().text());


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("!\"@* !%"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    @Test
    public void testAlphaNumPunc1() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("This_is"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
            assertEquals("This_is", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(7, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "This_is", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("is", tr.term().text());
        assertTrue(tr.next());
        assertEquals("this", tr.term().text());
        assertFalse(tr.next());


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("This is"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }


    @Test
    public void testAlphaNumPunc2() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("This___is"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
            assertEquals("This___is", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(9, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "This_is", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("is", tr.term().text());
        assertTrue(tr.next());
        assertEquals("this", tr.term().text());
        assertFalse(tr.next());


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("This is"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }

    }

    @Test
    public void testAlphaNumPunc3() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("__This___is_"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
            assertEquals("__This___is_", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(12, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "This_is", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertEquals("name", tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("name", tr.term().field());
        assertEquals("is", tr.term().text());
        assertTrue(tr.next());
        assertEquals("this", tr.term().text());
        assertFalse(tr.next());


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("This is"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);
        }
    }

    @Test
    public void testIssue158() throws Exception {

        //Show token is kept intact
        {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("3OH!3"));
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
            assertEquals("3OH!3", new String(term.buffer(),0,term.length()));
            assertEquals(0, offset.startOffset());
            assertEquals(5, offset.endOffset());
            assertFalse(tokenizer.incrementToken());
        }

        //Analyse field
        Analyzer analyzer = new MusicbrainzAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "3Oh!j", Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = IndexReader.open(dir);
        TermEnum tr = ir.terms(new Term("name",""));
        assertNotNull(tr);
        Term term = tr.term();
        assertEquals("name", term.field());
        assertEquals(1, tr.docFreq());
        assertEquals("3oh", term.text());
        tr.next();
        term = tr.term();
        assertEquals(1, tr.docFreq());
        assertEquals("j", term.text());


        IndexSearcher searcher = new IndexSearcher(IndexReader.open(dir));
        {
            Query q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("j"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);

            q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("3OH"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);

            q = new QueryParser(LuceneVersion.LUCENE_VERSION, "name", analyzer).parse(QueryParser.escape("3OH!j"));
            System.out.println(q.toString());
            assertEquals(1, searcher.search(q,10).totalHits);

        }

    }
}