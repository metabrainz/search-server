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
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;

import java.io.StringReader;

import static org.junit.Assert.*;

/**
 * Turn Han characters into Bigrams
 *
 */
public class IssueSearch386Test
{

    @Test
    public void convertHanToBigram() throws Exception
    {
        Tokenizer tokenizer = new MusicbrainzTokenizer(LuceneVersion.LUCENE_VERSION, new StringReader("陪著"));
        assertTrue(tokenizer.incrementToken());
        CharTermAttribute term = tokenizer.addAttribute(CharTermAttribute.class);
        TypeAttribute type = tokenizer.addAttribute(TypeAttribute.class);
        assertEquals("<CJ>", type.type());
        assertEquals("陪", new String(term.buffer(), 0, term.length()));
        assertTrue(tokenizer.incrementToken());
        term = tokenizer.addAttribute(CharTermAttribute.class);
        type = tokenizer.addAttribute(TypeAttribute.class);
        assertEquals("<CJ>", type.type());
        assertEquals("著", new String(term.buffer(), 0, term.length()));

        {
            Analyzer analyzer = new MusicbrainzAnalyzer();
            RAMDirectory dir = new RAMDirectory();
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            IndexWriter writer = new IndexWriter(dir, writerConfig);
            Document doc = new Document();
            doc.add(new Field("name", "陪著", TextField.TYPE_STORED));
            writer.addDocument(doc);
            writer.close();

            IndexReader ir = DirectoryReader.open(dir);
            Fields fields = MultiFields.getFields(ir);
            Terms terms = fields.terms("name");
            TermsEnum termsEnum = terms.iterator(null);
            termsEnum.next();
            assertEquals(1, termsEnum.docFreq());
            assertEquals("陪著", termsEnum.term().utf8ToString());
            assertNull(termsEnum.next());
        }

        {
            Analyzer analyzer = new TitleAnalyzer();
            RAMDirectory dir = new RAMDirectory();
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            IndexWriter writer = new IndexWriter(dir, writerConfig);
            Document doc = new Document();
            doc.add(new Field("name", "陪著", TextField.TYPE_STORED));
            writer.addDocument(doc);
            writer.close();

            IndexReader ir = DirectoryReader.open(dir);
            Fields fields = MultiFields.getFields(ir);
            Terms terms = fields.terms("name");
            TermsEnum termsEnum = terms.iterator(null);
            termsEnum.next();
            assertEquals(1, termsEnum.docFreq());
            assertEquals("陪著", termsEnum.term().utf8ToString());
            assertNull(termsEnum.next());
        }

        {
            Analyzer analyzer = new MusicbrainzKeepAccentsAnalyzer();
            RAMDirectory dir = new RAMDirectory();
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            IndexWriter writer = new IndexWriter(dir, writerConfig);
            Document doc = new Document();
            doc.add(new Field("name", "陪著", TextField.TYPE_STORED));
            writer.addDocument(doc);
            writer.close();

            IndexReader ir = DirectoryReader.open(dir);
            Fields fields = MultiFields.getFields(ir);
            Terms terms = fields.terms("name");
            TermsEnum termsEnum = terms.iterator(null);
            termsEnum.next();
            assertEquals(1, termsEnum.docFreq());
            assertEquals("陪著", termsEnum.term().utf8ToString());
            assertNull(termsEnum.next());
        }
    }
}