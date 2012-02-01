package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;

/**
 * Test retrieving artist from index and Outputting as Xml
 */
public class IssueSearch167Test extends TestCase {

    private SearchServer sd;

    public IssueSearch167Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

                
        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "11111111-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "Republica");
            doc.addField(ArtistIndexField.SORTNAME, "Republica");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAGCOUNT, "5");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.TAGCOUNT, "11");
            doc.addField(ArtistIndexField.IPI,"1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "22222222-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "República");
            doc.addField(ArtistIndexField.SORTNAME, "República");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAGCOUNT, "5");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.TAGCOUNT, "11");
            doc.addField(ArtistIndexField.IPI,"1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "33333333-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "Repúblice");
            doc.addField(ArtistIndexField.SORTNAME, "Repúblice");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAGCOUNT, "5");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.TAGCOUNT, "11");
            doc.addField(ArtistIndexField.IPI,"1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "44444444-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "Repúblic");
            doc.addField(ArtistIndexField.SORTNAME, "Repúblic");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAGCOUNT, "5");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.TAGCOUNT, "11");
            doc.addField(ArtistIndexField.IPI,"1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        //For Phrase Searching Tests
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "55555555-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "echo & the bunnymen");
            doc.addField(ArtistIndexField.SORTNAME, "bunnymen, echo");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAGCOUNT, "5");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.TAGCOUNT, "11");
            doc.addField(ArtistIndexField.IPI,"1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        //For Phrase Searching Tests
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "66666666-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "echobelly");
            doc.addField(ArtistIndexField.SORTNAME, "echobelly");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAGCOUNT, "5");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.TAGCOUNT, "11");
            doc.addField(ArtistIndexField.IPI,"1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        sd = new ArtistDismaxSearch(new IndexSearcher(ramDir, true));
    }


    public void testFindArtistDismax1() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Republica");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(4, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }

        org.apache.lucene.document.Document doc  = searcher.doc(topdocs.scoreDocs[0].doc);
        assertEquals("11111111-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        doc  = searcher.doc(topdocs.scoreDocs[1].doc);
        assertEquals("22222222-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

    }

    public void testFindArtistDismax2() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("República");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(4, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }

        org.apache.lucene.document.Document doc  = searcher.doc(topdocs.scoreDocs[0].doc);
        assertEquals("22222222-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        doc  = searcher.doc(topdocs.scoreDocs[1].doc);
        assertEquals("11111111-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        doc  = searcher.doc(topdocs.scoreDocs[2].doc);
        assertEquals("33333333-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        doc  = searcher.doc(topdocs.scoreDocs[3].doc);
        assertEquals("44444444-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

    }

    public void testFindArtistDismaxFuzzy() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Repúblic");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(4, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }

        org.apache.lucene.document.Document doc  = searcher.doc(topdocs.scoreDocs[0].doc);
        assertEquals("44444444-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

    }

    public void testFindArtistDismaxWildcard() throws Exception {
        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Repúb");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(4, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }

    }

    public void testFindArtistDismaxTooShortForWildcard() throws Exception {
        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Rep");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(0, topdocs.scoreDocs.length);

    }

    public void testFindArtistDismaxPhraseFuzzy() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Echo bunneymen");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(2, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }

        org.apache.lucene.document.Document doc  = searcher.doc(topdocs.scoreDocs[0].doc);
        assertEquals("55555555-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

        doc  = searcher.doc(topdocs.scoreDocs[1].doc);
        assertEquals("66666666-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

    }

    public void testFindArtistDismaxPhraseFuzzy2() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Echo & the bunnymen");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(2, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }

        org.apache.lucene.document.Document doc  = searcher.doc(topdocs.scoreDocs[0].doc);
        assertEquals("55555555-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

        doc  = searcher.doc(topdocs.scoreDocs[1].doc);
        assertEquals("66666666-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

    }

    public void testFindArtistDismaxWildcard2() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Echo");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(2, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }

        org.apache.lucene.document.Document doc  = searcher.doc(topdocs.scoreDocs[0].doc);
        assertEquals("55555555-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

        doc  = searcher.doc(topdocs.scoreDocs[1].doc);
        assertEquals("66666666-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

    }

    public void testFindArtistDismaxSpecialChars() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Republica/DC");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(2, topdocs.scoreDocs.length);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("arid").stringValue()+":"+explain);
        }
    }

    public void testFindArtistDismaxSpecialChars2() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Republica-DC");
        TopDocs topdocs = searcher.search(q, 10);
        assertEquals(2, topdocs.scoreDocs.length);
    }
}