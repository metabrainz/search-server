package org.musicbrainz.search.analysis;

import junit.framework.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;
import org.apache.lucene.queryParser.*;

public class AccentFilterTest extends TestCase {

    private Analyzer analyzer = new StandardUnaccentAnalyzer();
    private RAMDirectory dir = new RAMDirectory();

    public AccentFilterTest(String testName) {
        super(testName);
    }

	@Override
    protected void setUp() throws Exception {
        IndexWriter writer = new IndexWriter(dir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        Document doc = new Document();
        doc.add(new Field("name", "test", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "t\u00E9st", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "qwe 1", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("name", "qwe 2", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        doc = new Document();
        doc.add(new Field("name", "qwee 2", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("name", "aaa", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("name", "aaa", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        writer.close();
    }

    public void testSearchUnaccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir);
        Query q = new QueryParser("name", analyzer).parse("test");
        Hits hits = searcher.search(q);
        assertEquals(2, hits.length());
        assertEquals("test", hits.doc(0).getField("name").stringValue());
        assertEquals("t\u00E9st", hits.doc(1).getField("name").stringValue());
    }

    public void testSearchAccented() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir);
        Query q = new QueryParser("name", analyzer).parse("t\u00E9st");
        Hits hits = searcher.search(q);
        assertEquals(2, hits.length());
        assertEquals("test", hits.doc(0).getField("name").stringValue());
        assertEquals("t\u00E9st", hits.doc(1).getField("name").stringValue());
    }

    public void testSearchQe() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir);
        Query q = new QueryParser("name", analyzer).parse("qwe");
        Hits hits = searcher.search(q);
        assertEquals(1, hits.length());
        Document doc = hits.doc(0);
        String[] values = doc.getValues("name");
        assertEquals("qwe 1", values[0]);
        assertEquals("qwe 2", values[1]);
    }

    public void testSearchQe2() throws Exception {
        IndexSearcher searcher = new IndexSearcher(dir);
        Query q = new QueryParser("name", analyzer).parse("qwee");
        Hits hits = searcher.search(q);
        assertEquals(1, hits.length());
        Document doc = hits.doc(0);
        String[] values = doc.getValues("name");
        assertEquals("qwee 2", values[0]);
        assertEquals("aaa", values[1]);
        assertEquals("aaa", values[2]);
    }

}
