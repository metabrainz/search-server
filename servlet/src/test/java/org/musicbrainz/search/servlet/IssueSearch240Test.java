package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class IssueSearch240Test
{


  private AbstractSearchServer ss;
  private AbstractDismaxSearchServer sd;



  @Before
  public void setUp() throws Exception {
    ObjectFactory of = new ObjectFactory();

    RAMDirectory ramDir = new RAMDirectory();
    Analyzer analyzer = DatabaseIndex.getAnalyzer(RecordingIndexField.class);
    IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
    IndexWriter writer = new IndexWriter(ramDir, writerConfig);

    {
      MbDocument doc = new MbDocument();
      doc.addField(RecordingIndexField.RECORDING_ID, "7ca7782b-a602-448b-b108-bb881a7be2d6");
      doc.addField(RecordingIndexField.RECORDING, "I Don\u001at Dance");
      doc.addField(RecordingIndexField.RECORDING_OUTPUT, "I Don\u001at Dance");
      doc.addField(RecordingIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
      doc.addField(RecordingIndexField.RELEASE, "Our Glorious 5 Year Plan");
      doc.addField(RecordingIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
      doc.addField(RecordingIndexField.ARTIST, "Farming Incident");
      doc.addField(RecordingIndexField.ARTIST_NAME, "Farming Incident");
      doc.addField(RecordingIndexField.PUID, "1d9e8ed6-3893-4d3b-aa7d-72e79609e386");
      doc.addField(RecordingIndexField.COMMENT, "demo");
      doc.addField(RecordingIndexField.COUNTRY, "UK");
      doc.addField(RecordingIndexField.FORMAT, "Vinyl");

      ArtistCredit ac = of.createArtistCredit();
      NameCredit nc = of.createNameCredit();
      Artist artist = of.createArtist();
      artist.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
      artist.setName("Farming Incident");
      artist.setSortName("Incident, Farming");
      nc.setArtist(artist);
      ac.getNameCredit().add(nc);
      doc.addField(RecordingIndexField.ARTIST_CREDIT, MMDSerializer.serialize(ac));

      //Track Artist different to Recording Artist
      ac = of.createArtistCredit();
      nc = of.createNameCredit();
      artist = of.createArtist();
      artist.setId("2302e264-1cf0-4d1f-aca7-2a6f89e34b36");
      artist.setName("Pig Incident");
      artist.setSortName("Incident, Pig");
      nc.setArtist(artist);
      ac.getNameCredit().add(nc);
      doc.addField(RecordingIndexField.TRACK_ARTIST_CREDIT, MMDSerializer.serialize(ac));

      doc.addNumericField(RecordingIndexField.DURATION, 234000);
      doc.addNumericField(RecordingIndexField.RECORDING_DURATION_OUTPUT, 234000);

      doc.addNumericField(RecordingIndexField.QUANTIZED_DURATION, (234000 / 2000));
      doc.addNumericField(RecordingIndexField.NUM_TRACKS,10);
      doc.addNumericField(RecordingIndexField.NUM_TRACKS_RELEASE,10);
      doc.addNumericField(RecordingIndexField.TRACKNUM, 5);
      doc.addField(RecordingIndexField.NUMBER, "A4");
      doc.addField(RecordingIndexField.TRACK_OUTPUT, "Gravitational Lens");
      doc.addField(RecordingIndexField.RECORDING, "Gravitational Lens");
      doc.addField(RecordingIndexField.RELEASEGROUP_ID, "4444e264-1cf0-4d1f-aca7-2a6f89e34b36");

      doc.addField(RecordingIndexField.POSITION, "1");
      doc.addField(RecordingIndexField.RELEASE_TYPE, "Compilation");
      doc.addField(RecordingIndexField.RELEASE_PRIMARY_TYPE, "Album");
      doc.addField(RecordingIndexField.RELEASE_SECONDARY_TYPE, "Compilation");

      SecondaryTypeList stl = of.createSecondaryTypeList();
      stl.getSecondaryType().add("Compilation");
      doc.addField(RecordingIndexField.SECONDARY_TYPE_OUTPUT, MMDSerializer.serialize(stl));

      doc.addField(RecordingIndexField.RELEASE_STATUS, "Official");
      doc.addField(RecordingIndexField.RELEASE_DATE, "1970-01-01");
      doc.addField(RecordingIndexField.ISRC, "123456789");
      doc.addField(RecordingIndexField.ISRC, "abcdefghi");
      doc.addNumericField(RecordingIndexField.DURATION, 233000);
      doc.addNumericField(RecordingIndexField.TRACK_DURATION_OUTPUT, 233000);

      doc.addField(RecordingIndexField.TAG, "indie");
      doc.addField(RecordingIndexField.TAGCOUNT, "101");
      doc.addField(RecordingIndexField.RELEASE_AC_VA,"1");
      writer.addDocument(doc.getLuceneDocument());

    }

    {
      MbDocument doc = new MbDocument();
      doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
      doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
      writer.addDocument(doc.getLuceneDocument());
    }

    writer.close();
    SearcherManager searcherManager = new SearcherManager(ramDir,
        new MusicBrainzSearcherFactory(ResourceType.RECORDING));
    ss = new RecordingSearch(searcherManager);
    sd = new RecordingDismaxSearch(ss);
  }


  @Test
  public void testOutputAsXml() throws Exception {

    Results res = ss.search("rid:7ca7782b-a602-448b-b108-bb881a7be2d6", 0, 10);
    ResultsWriter writer = ss.getMmd2Writer();
    StringWriter sw = new StringWriter();
    PrintWriter pr = new PrintWriter(sw);
    writer.write(pr, res,SearchServerServlet.RESPONSE_XML);
    pr.close();
    String output = sw.toString();
    System.out.println("Xml is" + output);
    assertTrue(output.contains("count=\"1\""));
    assertTrue(output.contains("offset=\"0\""));
    assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
    assertTrue(output.contains("id=\"4444e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
    //IllegalAccessError control char convetted toString() apostrophe
    //assertTrue(output.contains("<title>I Don't Dance</title"));
  }

}
