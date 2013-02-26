package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.ArtistType;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MetaIndexField;

public class ReloadIndexesTest {

  private AbstractSearchServer ss;
  private AbstractDismaxSearchServer sd;
  private RAMDirectory ramDir;

  @Before
  public void setUp() throws Exception {
    ramDir = new RAMDirectory();
    addArtist1();
    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.ARTIST));
    ss = new ArtistSearch(searcherManager);
    sd = new ArtistDismaxSearch(ss);
  }

  private void addArtist1() throws Exception {
    Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
    writerConfig.setSimilarity(new MusicbrainzSimilarity());
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);
    // General Purpose Artist
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
      doc.addField(ArtistIndexField.IPI, "1001");
      writer.addDocument(doc.getLuceneDocument());
    }

    {
      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
      doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.close();
  }

  private void addArtist2() throws Exception {
    Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
    writerConfig.setSimilarity(new MusicbrainzSimilarity());
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);
    // General Purpose Artist
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

    {
      Term term = new Term(MetaIndexField.META.getName(), MetaIndexField.META_VALUE);
      TermQuery query = new TermQuery(term);
      writer.deleteDocuments(query);

      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
      doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.commit();
    writer.close();
  }

  private void updateIndexMetadata() throws Exception {
    Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
    writerConfig.setSimilarity(new MusicbrainzSimilarity());
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);
    // General Purpose Artist
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
      doc.addField(ArtistIndexField.IPI, "1001");
      writer.addDocument(doc.getLuceneDocument());
    }

    {
      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
      doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.close();
  }

  @Test
  public void testReloadDoesNothingIfIndexNotChanged() throws Exception {
    ss.reloadIndex();
    Results res = ss.search("type:\"group\"", 0, 10);
    assertEquals(1, res.totalHits);
  }

  @Test
  public void testReloadUpdatesReaderIfIndexChanged() throws Exception {

    Results res;
    res = ss.search("type:\"group\"", 0, 10);
    assertEquals(1, res.totalHits);

    // Testing reloading if there are no changes
    ss.reloadIndex();

    res = ss.search("type:\"group\"", 0, 10);
    assertEquals(1, res.totalHits);

    addArtist2();
    ss.reloadIndex();

    res = ss.search("type:\"group\"", 0, 10);
    assertEquals(2, res.totalHits);
  }

  @Test
  public void testDismaxSearchServerUpdatesToo() throws Exception {

    Results res;
    res = sd.search("Bunnymen", 0, 10);
    assertEquals(0, res.totalHits);

    addArtist2();
    // this should also reload index for the 2nd searchserver sd, since they share the same
    // underlying searchmanager
    ss.reloadIndex();

    res = sd.search("Bunnymen", 0, 10);
    assertEquals(1, res.totalHits);
  }

  @Test
  public void testDismaxSearchUsesSameResultWriter() throws Exception {

    final String WS_VERSION_1 = "1";
    final String WS_VERSION_2 = "2";

    // Writers should be the same between SearchServer and DismaxSearchServer
    assertEquals(ss.getWriter(WS_VERSION_1), sd.getWriter(WS_VERSION_1));
    assertEquals(ss.getWriter(WS_VERSION_2), sd.getWriter(WS_VERSION_2));

    // So last update date should be identical
    Date initialDate = ss.getWriter(WS_VERSION_2).getLastUpdateDate();
    assertEquals(initialDate, sd.getWriter(WS_VERSION_2).getLastUpdateDate());

    // Reloading with no changes
    ss.reloadIndex();
    assertEquals(initialDate, sd.getWriter(WS_VERSION_2).getLastUpdateDate());
    assertEquals(ss.getWriter(WS_VERSION_2).getLastUpdateDate(), sd.getWriter(WS_VERSION_2).getLastUpdateDate());

    // Reloading no changes
    addArtist2();
    ss.reloadIndex();
    // Last update date should be still be identical between both searchers, but has changed since index reload
    assertEquals(ss.getWriter(WS_VERSION_2).getLastUpdateDate(), sd.getWriter(WS_VERSION_2).getLastUpdateDate());
    assertTrue(initialDate.before(sd.getWriter(WS_VERSION_2).getLastUpdateDate()));
  }

  @Test
  public void testInitUpdatesReaderIfIndexChanged() throws Exception {

    addArtist2();
    SearcherManager searcherManager = new SearcherManager(ramDir,
        new MusicBrainzSearcherFactory(ResourceType.RECORDING));
    ss = new ArtistSearch(searcherManager);

    Results res = ss.search("type:\"group\"", 0, 10);
    assertEquals(2, res.totalHits);
  }
}
