package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

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
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindRecordingTest {


    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();

        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(RecordingIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        {
            MbDocument doc = new MbDocument();
            Recording recording = of.createRecording();

            doc.addField(RecordingIndexField.RECORDING_ID, "7ca7782b-a602-448b-b108-bb881a7be2d6");
            recording.setId("7ca7782b-a602-448b-b108-bb881a7be2d6");

            doc.addField(RecordingIndexField.RECORDING, "Gravitational Lenz");
            doc.addField(RecordingIndexField.RECORDING, "Gravitational Lens");
            recording.setTitle("Gravitational Lenz");

            doc.addField(RecordingIndexField.COMMENT, "demo");
            recording.setDisambiguation("demo");

            doc.addField(RecordingIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
            doc.addField(RecordingIndexField.RELEASE, "Our Glorious 5 Year Plan");

            doc.addField(RecordingIndexField.VIDEO,"true");
            recording.setVideo("true");

            doc.addField(RecordingIndexField.FORMAT, "Vinyl");
            ReleaseList releaseList = of.createReleaseList();
            Release release = of.createRelease();
            MediumList ml = of.createMediumList();
            Medium m = of.createMedium();
            Medium.TrackList trackList = of.createMediumTrackList();
            releaseList.getRelease().add(release);
            release.setMediumList(ml);
            ml.getMedium().add(m);
            m.setTrackList(trackList);
            Medium.TrackList.Track track = of.createMediumTrackListTrack();
            trackList.getDefTrack().add(track);
            recording.setReleaseList(releaseList);
            m.setFormat("Vinyl");
            track.setTitle("Gravitational Lens");
            doc.addField(RecordingIndexField.TRACK_ID,"2d9e8ed6-3893-4d3b-aa7d-72e79609e386");

            release.setId("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
            release.setTitle("Our Glorious 5 Year Plan");

            doc.addField(RecordingIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.ARTIST, "Farming Incident");
            doc.addField(RecordingIndexField.ARTIST_NAME, "Farming Incident");
            ArtistCredit ac = of.createArtistCredit();
            NameCredit nc = of.createNameCredit();
            Artist artist = of.createArtist();
            artist.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Farming Incident");
            artist.setSortName("Incident, Farming");
            nc.setArtist(artist);
            ac.getNameCredit().add(nc);
            recording.setArtistCredit(ac);

            //Track Artist different to Recording Artist
            ac = of.createArtistCredit();
            nc = of.createNameCredit();
            artist = of.createArtist();
            artist.setId("2302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Pig Incident");
            artist.setSortName("Incident, Pig");
            nc.setArtist(artist);
            ac.getNameCredit().add(nc);
            track.setArtistCredit(ac);

            doc.addNumericField(RecordingIndexField.DURATION, 234000);
            recording.setLength(BigInteger.valueOf(234000));

            doc.addField(RecordingIndexField.RELEASEGROUP_ID, "4444e264-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.POSITION, "1");
            doc.addField(RecordingIndexField.RELEASE_TYPE, "Compilation");
            doc.addField(RecordingIndexField.RELEASE_PRIMARY_TYPE, "Album");
            doc.addField(RecordingIndexField.RELEASE_SECONDARY_TYPE, "Compilation");
            ReleaseGroup rg = of.createReleaseGroup();
            rg.setId("4444e264-1cf0-4d1f-aca7-2a6f89e34b36");
            rg.setType("Compilation");
            rg.setPrimaryType("Album");
            SecondaryTypeList slt = of.createSecondaryTypeList();
            slt.getSecondaryType().add("Compilation");
            rg.setSecondaryTypeList(slt);
            release.setReleaseGroup(rg);
            m.setPosition(BigInteger.valueOf(1));

            doc.addNumericField(RecordingIndexField.QUANTIZED_DURATION, (234000 / 2000));
            doc.addNumericField(RecordingIndexField.NUM_TRACKS, 10);
            doc.addNumericField(RecordingIndexField.NUM_TRACKS_RELEASE, 10);
            doc.addNumericField(RecordingIndexField.TRACKNUM, 5);
            doc.addField(RecordingIndexField.NUMBER, "A4");
            trackList.setCount(BigInteger.valueOf(10));
            trackList.setOffset(BigInteger.valueOf(4));
            track.setNumber("A4");
            track.setLength(BigInteger.valueOf(233000));
            track.setId("c3b8dbc9-c1ff-4743-9015-8d762819134e");
            ml.setTrackCount(BigInteger.valueOf(10));
            doc.addField(RecordingIndexField.RELEASE_STATUS, "Official");
            doc.addField(RecordingIndexField.RELEASE_DATE, "1970-01-01");
            doc.addField(RecordingIndexField.COUNTRY, "UK");
            release.setStatus("Official");
            release.setDate("1970-01-01");
            release.setCountry("UK");
            ReleaseEventList rel = of.createReleaseEventList();
            ReleaseEvent     re  = of.createReleaseEvent();
            DefAreaElementInner areaInner = of.createDefAreaElementInner();
            Iso31661CodeList    isoList   = of.createIso31661CodeList();
            isoList.getIso31661Code().add("UK");
            areaInner.setIso31661CodeList(isoList);
            areaInner.setIso31661CodeList(isoList);
            areaInner.setId("1fa8aa07-c688-1f7c-734b-4d82e528b09b");
            areaInner.setName("United Kingdom");
            areaInner.setSortName("United Kingdom");
            re.setArea(areaInner);
            re.setDate("1970-01-01");
            rel.getReleaseEvent().add(re);
            release.setReleaseEventList(rel);
            doc.addField(RecordingIndexField.ISRC, "123456789");
            doc.addField(RecordingIndexField.ISRC, "abcdefghi");
            IsrcList isrcList = of.createIsrcList();
            recording.setIsrcList(isrcList);
            Isrc isrc1 = of.createIsrc();
            isrc1.setId("123456789");
            Isrc isrc2 = of.createIsrc();
            isrc2.setId("abcdefghi");

            isrcList.getIsrc().add(isrc1);
            isrcList.getIsrc().add(isrc2);


            doc.addNumericField(RecordingIndexField.DURATION, 233000);


            doc.addField(RecordingIndexField.TAG, "indie");
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("indie");
            tag.setCount(new BigInteger("101"));
            tagList.getTag().add(tag);
            release.setTagList(tagList);

            Artist vaArtist = of.createArtist();
            vaArtist.setId("89ad4ac3-39f7-470e-963a-56509c546377");
            vaArtist.setName("Various Artists");
            NameCredit   naCredit = of.createNameCredit();
            naCredit.setArtist(vaArtist);
            ArtistCredit vaCredit = of.createArtistCredit();
            vaCredit.getNameCredit().add(naCredit);
            release.setArtistCredit(vaCredit);

            doc.addField(RecordingIndexField.RECORDING_STORE, MMDSerializer.serialize(recording));
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

    protected String getRecordingId(MbDocument doc) {
        Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE), Recording.class);
        return recording.getId();
    }

    @Test
    public void testFindRecordingByV1TrackField() throws Exception {
        Results res = ss.search("track:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }


    @Test
    public void testFindRecording() throws Exception {
        Results res = ss.search("recording:\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingDismax1() throws Exception {
        Results res = sd.search("Gravitational", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingDismax2() throws Exception {
        Results res = sd.search("Glorious", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingDismax3() throws Exception {
        Results res = sd.search("Farming Incident", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByV1TrackId() throws Exception {
        Results res = ss.search("trid:\"7ca7782b-a602-448b-b108-bb881a7be2d6\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingById() throws Exception {
        Results res = ss.search("rid:\"7ca7782b-a602-448b-b108-bb881a7be2d6\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseId() throws Exception {
        Results res = ss.search("reid:\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByVideo() throws Exception {
        Results res = ss.search("video:\"true\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByDemo() throws Exception {
        Results res = ss.search("comment:\"demo\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByArtistId() throws Exception {
        Results res = ss.search("arid:\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    public void testFindRecordingByArtistName() throws Exception {
        Results res = ss.search("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    /**
     * Searches recording field, which should include names of associated tracks)
     */
    @Test
    public void testFindRecordingByTrackName() throws Exception {
        Results res = ss.search("recording:\"Gravitational Lens\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseType() throws Exception {
        Results res = ss.search("type:\"compilation\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByPrimaryReleaseType() throws Exception {
        Results res = ss.search("primarytype:\"album\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingBySecondaryReleaseType() throws Exception {
        Results res = ss.search("secondarytype:\"compilation\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseGroupId() throws Exception {
        Results res = ss.search("rgid:\"4444e264-1cf0-4d1f-aca7-2a6f89e34b36\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseCountry() throws Exception {
        Results res = ss.search("country:UK", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseFormat() throws Exception {
        Results res = ss.search("format:Vinyl", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseTypeNumeric() throws Exception {
        Results res = ss.search("type:\"4\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByNumberOfTracksOnMediumOnRelease() throws Exception {
        Results res = ss.search("tracks:10", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByNumberOfTracksOnRelease() throws Exception {
        Results res = ss.search("tracksrelease:10", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByDuration() throws Exception {
        Results res = ss.search("dur:234000", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByDuration2() throws Exception {
        Results res = ss.search("dur:234000", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByISRC() throws Exception {
        Results res = ss.search("isrc:123456789", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByNonNumericDuration() throws Exception {
        Results res = ss.search("dur:fred", 0, 10);
        assertEquals(0, res.getTotalHits());
    }

    @Test
    public void testFindRecordingByTag() throws Exception {
        Results res = ss.search("tag:indie", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByDurationRange() throws Exception {
        Results res = ss.search("dur:[87 TO 240000]", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByQdur() throws Exception {
        Results res = ss.search("qdur:117", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByTrackPosition() throws Exception {
        Results res = ss.search("tnum:5", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByTrackNumber() throws Exception {
        Results res = ss.search("number:A4", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByPosition() throws Exception {
        Results res = ss.search("position:1", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseStatus() throws Exception {
        Results res = ss.search("status:Official", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByReleaseDate() throws Exception {
        Results res = ss.search("date:1970-01-01", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByDefault() throws Exception {
        Results res = ss.search("\"Gravitational Lenz\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testNumericRangeQuery() throws Exception {
        Results res = ss.search("tracks:[1 TO 10]", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindRecordingByTrackId() throws Exception {
        Results res = ss.search("tid:2d9e8ed6-3893-4d3b-aa7d-72e79609e386", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("7ca7782b-a602-448b-b108-bb881a7be2d6", getRecordingId(res.results.get(0).getDoc()));
    }

    @Test
    public void testOutputAsMmd1Xml() throws Exception {

        Results res = ss.search("track:\"Gravitational Lenz\"", 0, 10);
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

        Results res = ss.search("recording:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML);
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
        assertTrue(output.contains("<track id=\"c3b8dbc9-c1ff-4743-9015-8d762819134e\"><number>A4</number><title>Gravitational Lens</title><length>233000</length><artist-credit><name-credit><artist id=\"2302e264-1cf0-4d1f-aca7-2a6f89e34b36\"><name>Pig Incident</name><sort-name>Incident, Pig</sort-name></artist></name-credit></artist-credit></track>"));
        assertTrue(output.contains("<area id=\"1fa8aa07-c688-1f7c-734b-4d82e528b09b\">"));
        assertTrue(output.contains("<name>United Kingdom</name>"));
        assertTrue(output.contains("<sort-name>United Kingdom</sort-name>"));
        assertTrue(output.contains("<iso-3166-1-code-list><iso-3166-1-code>UK</iso-3166-1-code></iso-3166-1-code-list>"));
        assertTrue(output.contains("<video>true</video>"));
    }


    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.search("recording:\"Gravitational Lenz\"", 0, 10);
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
        assertTrue(output.contains("\"artist-credit\":{\"name-credit\":[{\"artist\":{\"id\":\"89ad4ac3-39f7-470e-963a-56509c546377\",\"name\":\"Various Artists\"}"));
        assertTrue(output.contains("\"release-event-list\":{\"release-event\":[{\"date\":\"1970-01-01\""));
        assertTrue(output.contains("\"id\":\"1fa8aa07-c688-1f7c-734b-4d82e528b09b\","));
        assertTrue(output.contains("\"name\":\"United Kingdom\","));
        assertTrue(output.contains("\"sort-name\":\"United Kingdom\","));
        assertTrue(output.contains("\"iso-3166-1-code-list\":{\"iso-3166-1-code\":[\"UK\"]}"));
        assertTrue(output.contains("\"video\":\"true\""));
    }

    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("recording:\"Gravitational Lenz\"", 0, 10);
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
        assertTrue(output.contains("\"artist-credit\":[{\"artist\":{\"id\":\"89ad4ac3-39f7-470e-963a-56509c546377\",\"name\":\"Various Artists\"}}"));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"length\":234000"));
        assertTrue(output.contains("\"position\":1"));
        assertTrue(output.contains("\"track-count\":10"));
        assertTrue(output.contains("\"secondary-types\":[\"Compilation\"]}"));
        assertTrue(output.contains("\"release-events\":[{\"date\":\"1970-01-01\",\"area\""));
        assertTrue(output.contains("{\"id\":\"c3b8dbc9-c1ff-4743-9015-8d762819134e\""));
        assertTrue(output.contains("\"id\":\"1fa8aa07-c688-1f7c-734b-4d82e528b09b\","));
        assertTrue(output.contains("\"name\":\"United Kingdom\","));
        assertTrue(output.contains("\"sort-name\":\"United Kingdom\","));
        assertTrue(output.contains("\"iso-3166-1-codes\":[\"UK\"]"));
        assertTrue(output.contains("\"video\":true"));
    }

    @Test
    public void testOutputJsonNewPretty() throws Exception {

        Results res = ss.search("recording:\"Gravitational Lenz\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New Pretty is" + output);
        assertTrue(output.contains("\"offset\" : 0"));
    }

}
