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
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.mmd2.NameCredit;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.helper.ArtistCreditHelper;

import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Search fails when Artist credit contained no space in join phrase, and some tests for Chinese chars
 *
 */
public class IssueSearch328Test
{
    @Test
    public void testArtistCreditHelper() throws Exception
    {
        ObjectFactory of = new ObjectFactory();
        ArtistCredit ac = of.createArtistCredit();
        NameCredit nc1 = of.createNameCredit();
        nc1.setName("Revolution");
        nc1.setJoinphrase("×");
        NameCredit nc2 = of.createNameCredit();
        nc2.setName("水樹奈々");
        ac.getNameCredit().add(nc1);
        ac.getNameCredit().add(nc2);
        assertEquals("Revolution×水樹奈々",ArtistCreditHelper.buildFullArtistCreditName(ac));
    }

    @Test
    public void testXchar() throws Exception
    {
        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
        tokenizer.setReader(new StringReader("Revolution×"));
        tokenizer.reset();
        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
        System.out.println("Name is:" + term);
        assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
        assertEquals("Revolution×", new String(term.buffer(), 0, term.length()));

        Analyzer analyzer = new MusicbrainzWithPosGapAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "Revolution×", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexReader ir = DirectoryReader.open(dir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms("name");
        TermsEnum termsEnum = terms.iterator();
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("revolution", termsEnum.term().utf8ToString());
        assertNull(termsEnum.next());
    }

    @Test
    public void testArtistCreditFail() throws Exception
    {

        //Show token is kept intact
       {
            Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION);
            tokenizer.setReader(new StringReader("T.M.Revolution×水樹奈々"));
            tokenizer.reset();
            assertTrue(tokenizer.incrementToken());
            CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
            TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
            OffsetAttribute offset = tokenizer.addAttribute(OffsetAttribute.class);
            System.out.println("Name is:"+term);
            assertEquals("<ALPHANUMANDPUNCTUATION>", type.type());
            assertEquals("T.M.Revolution×", new String(term.buffer(), 0, term.length()));

           assertTrue(tokenizer.incrementToken());
            term = tokenizer.addAttribute(CharTermAttribute.class);
            type = tokenizer.addAttribute(TypeAttribute.class);
            offset = tokenizer.addAttribute(OffsetAttribute.class);
            System.out.println("Name is:"+term);
           assertEquals("<CJ>", type.type());
           assertEquals("水", new String(term.buffer(), 0, term.length()));


           assertTrue(tokenizer.incrementToken());
           term = tokenizer.addAttribute(CharTermAttribute.class);
           type = tokenizer.addAttribute(TypeAttribute.class);
           offset = tokenizer.addAttribute(OffsetAttribute.class);
           System.out.println("Name is:"+term);
           assertEquals("<CJ>", type.type());
           assertEquals("樹", new String(term.buffer(), 0, term.length()));

           assertTrue(tokenizer.incrementToken());
           term = tokenizer.addAttribute(CharTermAttribute.class);
           type = tokenizer.addAttribute(TypeAttribute.class);
           offset = tokenizer.addAttribute(OffsetAttribute.class);
           System.out.println("Name is:"+term);
           assertEquals("<CJ>", type.type());
           assertEquals("奈", new String(term.buffer(), 0, term.length()));

           assertTrue(tokenizer.incrementToken());
           term = tokenizer.addAttribute(CharTermAttribute.class);
           type = tokenizer.addAttribute(TypeAttribute.class);
           offset = tokenizer.addAttribute(OffsetAttribute.class);
           System.out.println("Name is:"+term);
           assertEquals("<ALPHANUM>", type.type());
           assertEquals("々", new String(term.buffer(), 0, term.length()));
        }

        Analyzer analyzer = new MusicbrainzWithPosGapAnalyzer();
        RAMDirectory dir = new RAMDirectory();
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(dir, writerConfig);
        Document doc = new Document();
        doc.add(new Field("name", "T.M.Revolution×水樹奈々", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        //Show how it has been converted
        IndexReader ir = DirectoryReader.open(dir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms("name");
        TermsEnum termsEnum = terms.iterator();
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("", termsEnum.term().utf8ToString());
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("m", termsEnum.term().utf8ToString());
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("revolution", termsEnum.term().utf8ToString());
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("t", termsEnum.term().utf8ToString());
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("树奈", termsEnum.term().utf8ToString());
        termsEnum.next();
        assertEquals(1, termsEnum.docFreq());
        assertEquals("水树", termsEnum.term().utf8ToString());
        termsEnum.next();

        //Now add another document without the cross, this still matches because we remove punctuation
        writerConfig = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(dir, writerConfig);
        doc = new Document();
        doc.add(new Field("name", "T.M.Revolution水樹奈", TextField.TYPE_STORED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("name:\"T.M.Revolution×\"");
            assertEquals(1, searcher.search(q, 10).totalHits);
        }

        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("name:\"T.M.Revolution×水樹奈々\"");
            assertEquals(1, searcher.search(q, 10).totalHits);
        }

        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("name:T.M.Revolution×");
            assertEquals(1, searcher.search(q, 10).totalHits);
        }

        //Because Chinese chars are basically ORED so matches both docs
        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("水樹奈々");
            assertEquals(2, searcher.search(q, 10).totalHits);
        }

        //unless make a phrase search
        searcher = new IndexSearcher(DirectoryReader.open(dir));
        {
            Query q = new QueryParser("name", analyzer).parse("\"水樹奈々\"");
            assertEquals(1, searcher.search(q, 10).totalHits);
        }

    }

}
