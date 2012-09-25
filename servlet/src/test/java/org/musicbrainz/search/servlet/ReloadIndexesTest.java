package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
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

  private SearchServer ss;
  private SearchServer sd;
  private RAMDirectory ramDir;

  @Before
  public void setUp() throws Exception {
    ramDir = new RAMDirectory();
    addArtist1();
    SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.ARTIST));
    ss = new ArtistSearch(searcherManager);
    sd = new ArtistDismaxSearch(searcherManager);
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
      doc.addField(MetaIndexField.LAST_UPDATED, NumericUtils.longToPrefixCoded(new Date().getTime()));
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

    writer.commit();
    writer.close();
  }

  @Test
  public void testReloadDoesNothingIfIndexNotChanged() throws Exception {
    ss.reloadIndex();
    Results res = ss.searchLucene("type:\"group\"", 0, 10);
    assertEquals(1, res.totalHits);
  }

  @Test
  public void testReloadUpdatesReaderIfIndexChanged() throws Exception {

    Results res;
    res = ss.searchLucene("type:\"group\"", 0, 10);
    assertEquals(1, res.totalHits);

    // Testing reloading if there are no changes
    ss.reloadIndex();

    res = ss.searchLucene("type:\"group\"", 0, 10);
    assertEquals(1, res.totalHits);

    addArtist2();
    ss.reloadIndex();

    res = ss.searchLucene("type:\"group\"", 0, 10);
    assertEquals(2, res.totalHits);
  }

  @Test
  public void testDismaxSearchServerUpdatesToo() throws Exception {

    Results res;
    res = sd.searchLucene("Bunnymen", 0, 10);
    assertEquals(0, res.totalHits);

    addArtist2();
    // this should also reload index for the 2nd searchserver sd, since they share the same
    // underlying searchmanager
    ss.reloadIndex();

    res = sd.searchLucene("Bunnymen", 0, 10);
    assertEquals(1, res.totalHits);
  }

  @Test
  public void testInitUpdatesReaderIfIndexChanged() throws Exception {

    addArtist2();
    SearcherManager searcherManager = new SearcherManager(ramDir,
        new MusicBrainzSearcherFactory(ResourceType.RECORDING));
    ss = new ArtistSearch(searcherManager);

    Results res = ss.searchLucene("type:\"group\"", 0, 10);
    assertEquals(2, res.totalHits);
  }
}
