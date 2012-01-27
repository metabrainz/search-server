package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ArtistType;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.servlet.mmd1.ArtistMmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd1.Mmd1XmlWriter;
import org.musicbrainz.search.servlet.mmd2.ArtistWriter;
import org.musicbrainz.search.servlet.mmd2.ResultsWriter;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Test retrieving artist from index and Outputting as Xml
 */
public class IssueSearch163Test extends TestCase {

    private SearchServer sd;

    public IssueSearch163Test(String testName) {
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
        writer.close();
        sd = new ArtistDismaxSearch(new IndexSearcher(ramDir, true));
    }


    public void testFindArtistDismax1() throws Exception {
        Results res = sd.searchLucene("Republica", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("11111111-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals(1f,result.score);
        result = res.results.get(1);
        doc = result.doc;
        assertEquals("22222222-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertTrue(1f > result.score);

    }

    public void testFindArtistDismax2() throws Exception {
        Results res = sd.searchLucene("República", 0, 10);
        assertEquals(2, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("22222222-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertEquals(1f,result.score);
        result = res.results.get(1);
        doc = result.doc;
        assertEquals("11111111-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID));
        assertTrue(1f > result.score);

    }
}