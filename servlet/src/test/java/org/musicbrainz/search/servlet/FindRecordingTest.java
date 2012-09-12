package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.*;
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
public class FindRecordingTest {


    private SearchServer ss;
    private SearchServer sd;



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
            doc.addField(RecordingIndexField.RECORDING, "Gravitational Lenz");
            doc.addField(RecordingIndexField.RECORDING_OUTPUT, "Gravitational Lenz");
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
            doc.addField(MetaIndexField.LAST_UPDATED, NumericUtils.longToPrefixCoded(new Date().getTime()));
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        ss = new RecordingSearch(new IndexSearcher(IndexReader.open(ramDir)));
        sd = new RecordingDismaxSearch(new IndexSearcher(IndexReader.open(ramDir)));
    }

    @Test
    public void testFindRecordingByV1TrackField() throws Exception {
        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }


    @Test
    public void testFindRecording() throws Exception {
        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
        assertEquals("Gravitational Lens", doc.get(RecordingIndexField.TRACK_OUTPUT));
        assertEquals("123456789", doc.get(RecordingIndexField.ISRC));
    }

    @Test
    public void testFindRecordingDismax1() throws Exception {
        Results res = sd.searchLucene("Gravitational", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingDismax2() throws Exception {
        Results res = sd.searchLucene("Glorious", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingDismax3() throws Exception {
        Results res = sd.searchLucene("Farming Incident", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByV1TrackId() throws Exception {
        Results res = ss.searchLucene("trid:\"7ca7782b-a602-448b-b108-bb881a7be2d6\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingById() throws Exception {
        Results res = ss.searchLucene("rid:\"7ca7782b-a602-448b-b108-bb881a7be2d6\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByReleaseId() throws Exception {
        Results res = ss.searchLucene("reid:\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByDemo() throws Exception {
        Results res = ss.searchLucene("comment:\"demo\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("demo", doc.get(RecordingIndexField.COMMENT));
    }

    @Test
    public void testFindRecordingByArtistId() throws Exception {
        Results res = ss.searchLucene("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

     public void testFindRecordingByArtistName() throws Exception {
        Results res = ss.searchLucene("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    /** Searches recording field, which should include names of associated tracks) */
    @Test
    public void testFindRecordingByTrackName() throws Exception {
        Results res = ss.searchLucene("recording:\"Gravitational Lens\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByReleaseType() throws Exception {
        Results res = ss.searchLucene("type:\"compilation\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByPrimaryReleaseType() throws Exception {
        Results res = ss.searchLucene("primarytype:\"album\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingBySecondaryReleaseType() throws Exception {
        Results res = ss.searchLucene("secondarytype:\"compilation\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByReleaseGroupId() throws Exception {
        Results res = ss.searchLucene("rgid:\"4444e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByReleaseCountry() throws Exception {
        Results res = ss.searchLucene("country:UK", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByReleaseFormat() throws Exception {
        Results res = ss.searchLucene("format:Vinyl", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByReleaseTypeNumeric() throws Exception {
        Results res = ss.searchLucene("type:\"4\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByNumberOfTracksOnMediumOnRelease() throws Exception {
        Results res = ss.searchLucene("tracks:10", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByNumberOfTracksOnRelease() throws Exception {
        Results res = ss.searchLucene("tracksrelease:10", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByDuration() throws Exception {
        Results res = ss.searchLucene("dur:234000", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByDuration2() throws Exception {
            Results res = ss.searchLucene("dur:234000", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByISRC() throws Exception {
        Results res = ss.searchLucene("isrc:123456789", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByNonNumericDuration() throws Exception {
        Results res = ss.searchLucene("dur:fred", 0, 10);
        assertEquals(0, res.totalHits);
    }

    @Test
    public void testFindRecordingByTag() throws Exception {
        Results res = ss.searchLucene("tag:indie", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
    }

    @Test
    public void testFindRecordingByDurationRange() throws Exception {
        Results res = ss.searchLucene("dur:[87 TO 240000]", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testFindRecordingByQdur() throws Exception {
        Results res = ss.searchLucene("qdur:117", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testFindRecordingByTrackPosition() throws Exception {
        Results res = ss.searchLucene("tnum:5", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testFindRecordingByTrackNumber() throws Exception {
        Results res = ss.searchLucene("number:A4", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testFindRecordingByPosition() throws Exception {
        Results res = ss.searchLucene("position:1", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testFindRecordingByReleaseStatus() throws Exception {
        Results res = ss.searchLucene("status:Official", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testFindRecordingByReleaseDate() throws Exception {
        Results res = ss.searchLucene("date:1970-01-01", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testFindRecordingByDefault() throws Exception {
        Results res = ss.searchLucene("\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", doc.get(RecordingIndexField.RECORDING_ID));
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", doc.get(RecordingIndexField.RELEASE_ID));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(5, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.TRACKNUM)));
        assertEquals("Our Glorious 5 Year Plan", doc.get(RecordingIndexField.RELEASE));
        assertEquals(234000, NumericUtils.prefixCodedToInt(doc.get(RecordingIndexField.DURATION)));
    }

    @Test
    public void testNumericRangeQuery() throws Exception {
           Results res = ss.searchLucene("tracks:[1 TO 10]", 0, 10);
           assertEquals(1, res.totalHits);
    }

    @Test
    public void testFindRecordingByPuid() throws Exception {
        Results res = ss.searchLucene("puid:1d9e8ed6-3893-4d3b-aa7d-72e79609e386", 0, 10);
        assertEquals(1, res.totalHits);
    }


    @Test
    public void testOutputAsMmd1Xml() throws Exception {

        Results res = ss.searchLucene("track:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = new TrackMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("<track id=\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("<title>Gravitational Lenz</title>"));
        assertTrue(output.contains("<duration>234000</duration>"));
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("type=\"Compilation\""));
        assertTrue(output.contains("id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386"));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("offset=\"4\""));
        assertTrue(output.contains("count=\"10\""));
    }

    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
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
        assertTrue(output.contains("id=\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("<title>Gravitational Lenz</title>"));
        assertTrue(output.contains("<disambiguation>demo</disambiguation>"));
        assertTrue(output.contains("<length>234000</length>"));
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("release id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("type=\"Compilation\""));
        assertTrue(output.contains("<primary-type>Album"));
        assertTrue(output.contains("<secondary-type>Compilation"));
        assertTrue(output.contains("offset=\"4\""));
        assertTrue(output.contains("count=\"10\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("<country>UK</country>"));
        assertTrue(output.contains("<format>Vinyl</format>"));
        assertTrue(output.contains("<isrc id=\"123456789\"/>"));
        assertTrue(output.contains("<isrc id=\"abcdefghi\"/>"));
        assertTrue(output.contains("<primary-type>Album</primary-type>"));
        assertTrue(output.contains("<title>Gravitational Lens</title>"));
        assertTrue(output.contains("<status>Official</status>"));
        assertTrue(output.contains("<date>1970-01-01</date>"));
        assertTrue(output.contains("<track-count>10</track-count>"));
        assertTrue(output.contains("<artist-credit><name-credit><artist id=\"89ad4ac3-39f7-470e-963a-56509c546377\"><name>Various Artists</name></artist></name-credit></artist-credit>"));
        assertTrue(output.contains("indie</name>"));
        assertTrue(output.contains("<track><number>A4</number><title>Gravitational Lens</title><length>233000</length><artist-credit><name-credit><artist id=\"2302e264-1cf0-4d1f-aca7-2a6f89e34b36\"><name>Pig Incident</name><sort-name>Incident, Pig</sort-name></artist></name-credit></artist-credit></track>"));
        assertTrue(output.contains("<puid-list><puid id=\"1d9e8ed6-3893-4d3b-aa7d-72e79609e386\"/></puid-list>"));
    }


    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"score\":\"100\""));
        assertTrue(output.contains("\"type\":\"Compilation\""));
        assertTrue(output.contains("title\":\"Gravitational Lenz\""));
        assertTrue(output.contains("\"length\":234000"));
        assertTrue(output.contains("\"isrc\":[{\"id\":\"123456789"));
        assertTrue(output.contains("\"position\":1"));
        assertTrue(output.contains("\"status\":\"Official\""));
        assertTrue(output.contains("\"track-count\":10"));
        assertTrue(output.contains("format\":\"Vinyl\""));
        assertTrue(output.contains("country\":\"UK\""));
        assertTrue(output.contains("\"primary-type\":\"Album\""));
        assertTrue(output.contains("{\"secondary-type\":[\"Compilation\"]}}"));
        assertTrue(output.contains("\"tag\":[{\"count\":101,\"name\":\"indie\"}"));
        assertTrue(output.contains("\"puid-list\":{\"puid\":[{\"id\":\"1d9e8ed6-3893-4d3b-aa7d-72e79609e386\"}]}"));
        assertTrue(output.contains("\"artist-credit\":{\"name-credit\":[{\"artist\":{\"id\":\"89ad4ac3-39f7-470e-963a-56509c546377\",\"name\":\"Various Artists\"}"));
    }

    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);

        assertTrue(output.contains("id\":\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        assertTrue(output.contains("\"score\":\"100\""));
        assertTrue(output.contains("title\":\"Gravitational Lenz\""));
        assertTrue(output.contains("\"isrcs\":[{\"id\":\"123456789"));
        assertTrue(output.contains("\"status\":\"Official\""));
        assertTrue(output.contains("format\":\"Vinyl\""));
        assertTrue(output.contains("\"position\":1,\"format\":\"Vinyl\""));
        assertTrue(output.contains("\"releases\":[{\"id\":\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("country\":\"UK\""));
        assertTrue(output.contains("\"primary-type\":\"Album\""));
        assertTrue(output.contains("\"secondary-types\":[\"Compilation\"]}"));
        assertTrue(output.contains("\"tags\":[{\"count\":101,\"name\":\"indie\"}"));
        assertTrue(output.contains("puids\":[{\"id\":\"1d9e8ed6-3893-4d3b-aa7d-72e79609e386\"}"));
        assertTrue(output.contains("\"artist-credit\":[{\"artist\":{\"id\":\"89ad4ac3-39f7-470e-963a-56509c546377\",\"name\":\"Various Artists\"}}"));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"length\":234000"));
        assertTrue(output.contains("\"position\":1"));
        assertTrue(output.contains("\"track-count\":10"));

    }

    @Test
    public void testOutputJsonNewPretty() throws Exception {

        Results res = ss.searchLucene("recording:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW,true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New Pretty is" + output);
        assertTrue(output.contains("\"offset\" : 0"));
    }
}
