package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ArtistType;
import org.musicbrainz.search.index.DatabaseIndex;

import java.io.IOException;

public class ReloadIndexesTest extends TestCase {

    private SearchServer ss;
    private RAMDirectory ramDir;

    @Override
    protected void setUp() throws Exception {
        ramDir = new RAMDirectory();
        addArtist1();
        ss = new ArtistSearch(new IndexSearcher(ramDir, true));
    }

    private void addArtist1() throws Exception {
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
        IndexWriterConfig  writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);
        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST, "Farming Incident");
            doc.addField(ArtistIndexField.SORTNAME, "Incident, Farming");
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
    }

    private void addArtist2() throws Exception {
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
        IndexWriterConfig  writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);
        //General Purpose Artist
        {
             MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "ccd4879c-5e88-4385-b131-bf65296bf245");
            doc.addField(ArtistIndexField.ARTIST, "Echo & The Bunnymen");
            doc.addField(ArtistIndexField.SORTNAME, "Echo & The Bunnymen");
            doc.addField(ArtistIndexField.BEGIN, "1978");
            doc.addField(ArtistIndexField.COUNTRY, "unknown");
            doc.addField(ArtistIndexField.TYPE, ArtistType.GROUP.getName());
            doc.addField(ArtistIndexField.ALIAS, "Echo And The Bunnymen");
            doc.addField(ArtistIndexField.ALIAS, "Echo & The Bunnyman");
            doc.addField(ArtistIndexField.ALIAS, "Echo and The Bunymen");
            doc.addField(ArtistIndexField.ALIAS, "Echo & The Bunymen");
            writer.addDocument(doc.getLuceneDocument());
        }
        writer.commit();
        writer.close();
    }


    public void testReloadDoesNothingIfIndexNotChanged() throws Exception {

        IndexReader irStart = ss.getIndexSearcher().getIndexReader();
        ss.reloadIndex();
        IndexReader   irOld = ss.getIndexSearcher().getIndexReader();
        assertTrue(irStart==irOld) ;
        Results res = ss.searchLucene("type:\"group\"", 0, 10);
        assertEquals(1, res.totalHits);
    }

     public void testReloadUpdatesReaderIfIndexChanged() throws Exception {

        IndexReader irStart = ss.getIndexSearcher().getIndexReader();
        addArtist2();
        ss.reloadIndex();
        IndexReader   irOld = ss.getIndexSearcher().getIndexReader();
        assertFalse(irStart==irOld) ;
        Results res = ss.searchLucene("type:\"group\"", 0, 10);
        //Not the result we wanted
        assertEquals(2, res.totalHits);
    }

    public void testInitUpdatesReaderIfIndexChanged() throws Exception {

        IndexReader irStart = ss.getIndexSearcher().getIndexReader();
        addArtist2();
        ss = new ArtistSearch(new IndexSearcher(ramDir, true));
        IndexReader   irOld = ss.getIndexSearcher().getIndexReader();
        assertFalse(irStart==irOld) ;
        Results res = ss.searchLucene("type:\"group\"", 0, 10);
        assertEquals(2, res.totalHits);
    }
}
