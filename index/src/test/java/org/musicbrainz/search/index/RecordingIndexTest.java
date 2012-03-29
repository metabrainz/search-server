package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.Test;
import org.musicbrainz.mmd2.ArtistCredit;

import java.sql.Statement;

import static org.junit.Assert.*;

public class RecordingIndexTest extends AbstractIndexTest {

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir, RecordingIndexField.class);
        RecordingIndex ri = new RecordingIndex(conn);
        CommonTables ct = new CommonTables(conn, ri.getName());
        ct.createTemporaryTables(false);
        ri.init(writer, false);
        ri.addMetaInformation(writer);
        ri.indexData(writer, 0, Integer.MAX_VALUE);
        ri.destroy();
        writer.close();
    }


    /**
     * All Basic Fields Plus Release Events
     *
     * @throws Exception exception
     */
    private void addTrackOne() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 2)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1970, 1, 1)");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format) VALUES (1, 1, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO tracklist (id, track_count) VALUES (1, 2)");

        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                + " VALUES (1, 1, 1, 4, 2, 1, 33100)");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length, comment)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000, 'demo')");

        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean')");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (2, 'Do It Cleans')");

        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (1, 1, 'FRAAA9000038')");
        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (2, 1, 'FRAAA9100082')");

        stmt.addBatch("INSERT INTO puid (id, puid) VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e38')");
        stmt.addBatch("INSERT INTO recording_puid (id, puid, recording) VALUES (1, 1, 1)");

        stmt.addBatch("INSERT INTO country (id, iso_code, name) VALUES (1, 'UK','United Kingdom')");

        stmt.executeBatch();
        stmt.close();
    }

    /**
     * All Basic Fields Plus Release Events and different track artist to recording artist
     *
     * @throws Exception exception
     */
    private void addTrackTwo() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 1, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Pixies')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (2, 'ddd4879c-5e88-4385-b131-bf65296bf245', 2, 2, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (2, 2, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (2, 0, 2, 1)");


        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, packaging, country, " +
                "  language, script, date_year, date_month, date_day) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format) VALUES (1, 1, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO tracklist (id, track_count) VALUES (1, 2)");
        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                + " VALUES (1, 1, 1, 4, 1, 2, 33100)");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000)");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean')");
        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'punk', 2)");
        stmt.addBatch("INSERT INTO recording_tag (recording, tag, count) VALUES (1, 1, 10)");

        stmt.executeBatch();
        stmt.close();
    }

    /**
     * Add recording that is on two releases
     *
     * @throws Exception
     */
    private void addTrackThree() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 2)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 1)");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491243, 'efd2ace2-b3b9-305f-8a53-9803595c0e67', 2, 1, 2)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1970, 1, 1)");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format, name) VALUES (1, 1, 491240, 1, 7, null)");
        stmt.addBatch("INSERT INTO tracklist (id, track_count) VALUES (1, 2)");

        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                + " VALUES (1, 1, 1, 7, 2, 1, 33100)");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, country, " +
                "  language, script, date_year, date_month, date_day) " +
                " VALUES (491241, 'c3b8dbc9-c1ff-4743-9015-8d762819134f', 1, 1, 491243, 2, 1, 1, 1, 1970, 1, 1)");

        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format) VALUES (2, 2, 491241, 1, 7)");
        stmt.addBatch("INSERT INTO tracklist (id, track_count) VALUES (2, 2)");
        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                + " VALUES (2, 1, 2, 4, 2, 1, 33100)");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean')");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (2, 'Do It Cleans')");

        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (1, 1, 'FRAAA9000038')");
        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (2, 1, 'FRAAA9100082')");

        stmt.addBatch("INSERT INTO puid (id, puid) VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e38')");
        stmt.addBatch("INSERT INTO recording_puid (id, puid, recording) VALUES (1, 1, 1)");

        stmt.executeBatch();
        stmt.close();
    }

    /**
     * Add standalone recording
     *
     * @throws Exception
     */
    private void addTrackFour() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 2)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000)");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean')");

        stmt.addBatch("INSERT INTO puid (id, puid) VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e38')");
        stmt.addBatch("INSERT INTO recording_puid (id, puid, recording) VALUES (1, 1, 1)");

        stmt.executeBatch();
        stmt.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexRecording() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.TRACK_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_ID.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_STATUS.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.ISRC.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getFieldable(RecordingIndexField.RECORDING_ID.getName()).stringValue());
            assertEquals("Crocodiles (bonus disc)", doc.getFieldable(RecordingIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getFieldable(RecordingIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getFieldable(RecordingIndexField.NUM_TRACKS.getName()).stringValue()));
            assertEquals(4, NumericUtils.prefixCodedToInt(doc.getFieldable(RecordingIndexField.TRACKNUM.getName()).stringValue()));
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getFieldable(RecordingIndexField.NUM_TRACKS_RELEASE.getName()).stringValue()));
            assertEquals(33000, NumericUtils.prefixCodedToInt(doc.getFieldable(RecordingIndexField.RECORDING_DURATION_OUTPUT.getName()).stringValue()));
            assertEquals("Non-Album Tracks", doc.getFieldable(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
            assertEquals("Official", doc.getFieldable(RecordingIndexField.RELEASE_STATUS.getName()).stringValue());
            assertEquals("FRAAA9000038", doc.getFieldable(RecordingIndexField.ISRC.getName()).stringValue());
            assertEquals("1", doc.getFieldable(RecordingIndexField.POSITION.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexStandaloneRecording() throws Exception {

        addTrackFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            //assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_ID.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(0, doc.getFieldables(RecordingIndexField.RELEASE_STATUS.getName()).length);
            assertEquals(0, doc.getFieldables(RecordingIndexField.RELEASE.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getFieldable(RecordingIndexField.RECORDING_ID.getName()).stringValue());
            assertEquals("standalone", doc.getFieldable(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testReleaseType() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals("Non-Album Tracks", doc.getFieldable(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testReleaseCountry() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.COUNTRY.getName()).length);
            assertEquals("UK", doc.getFieldable(RecordingIndexField.COUNTRY.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testReleaseDate() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_DATE.getName()).length);
            assertEquals("1970-01-01", doc.getFieldable(RecordingIndexField.RELEASE_DATE.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testArtistSortname() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseGroupIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Echo and The Bunnymen", ac.getNameCredit().get(0).getArtist().getSortName());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testNoReleaseType() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals("-", doc.getFieldable(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testNoReleaseStatus() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_STATUS.getName()).length);
            assertEquals("-", doc.getFieldable(RecordingIndexField.RELEASE_STATUS.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testNoArtistComment() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(RecordingIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertNull(ac.getNameCredit().get(0).getArtist().getDisambiguation());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testArtistComment() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(RecordingIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("a comment", ac.getNameCredit().get(0).getArtist().getDisambiguation());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testRecordingArtist() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(RecordingIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());
            assertTrue(doc.get(RecordingIndexField.TRACK_ARTIST_CREDIT.getName()).equals("-"));
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testTrackArtist() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(RecordingIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());

            assertFalse(doc.get(RecordingIndexField.TRACK_ARTIST_CREDIT.getName()).equals("-"));
            ac = ArtistCreditHelper.unserialize(doc.get(RecordingIndexField.TRACK_ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Pixies", ac.getNameCredit().get(0).getArtist().getName());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testTag() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals("punk", doc.getFieldable(RecordingIndexField.TAG.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testTrackName() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals("Do It Clean", doc.getFieldable(RecordingIndexField.RECORDING_OUTPUT.getName()).stringValue());
            assertEquals(1, doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.TRACK_OUTPUT.getName()).length);
            assertEquals("Do It Cleans", doc.getFieldable(RecordingIndexField.TRACK_OUTPUT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testISRC() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(2, doc.getFieldables(RecordingIndexField.ISRC.getName()).length);
            assertEquals("FRAAA9000038", doc.getFieldable(RecordingIndexField.ISRC.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * @throws Exception exception
     */
    @Test
    public void testNoISRC() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(RecordingIndexField.ISRC.getName()).length);
        }
        ir.close();
    }

    /**
     * Test gives puid
     *
     * @throws Exception exception
     */
    @Test
    public void testPuid() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.PUID.getName()).length);
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e38", doc.getFieldable(RecordingIndexField.PUID.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Test gives puid
     *
     * @throws Exception exception
     */
    @Test
    public void testComment() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.COMMENT.getName()).length);
            assertEquals("demo", doc.getFieldable(RecordingIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Test gives format
     *
     * @throws Exception exception
     */
    @Test
    public void testFormat() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.FORMAT.getName()).length);
            assertEquals("Vinyl", doc.getFieldable(RecordingIndexField.FORMAT.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Test no puid
     *
     * @throws Exception exception
     */
    @Test
    public void testNoPuid() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFieldables(RecordingIndexField.PUID.getName()).length);
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexRecordingOnTwoReleases() throws Exception {

        addTrackThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.TRACK_OUTPUT.getName()).length);
            assertEquals(1, doc.getFieldables(RecordingIndexField.RECORDING_ID.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.RELEASE.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.RELEASE_STATUS.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.TRACKNUM.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.NUM_TRACKS.getName()).length);
            assertEquals(2, doc.getFieldables(RecordingIndexField.ISRC.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getFieldable(RecordingIndexField.RECORDING_ID.getName()).stringValue());
            assertEquals("Crocodiles (bonus disc)", doc.getFieldables(RecordingIndexField.RELEASE.getName())[0].stringValue());
            assertEquals("Crocodiles", doc.getFieldables(RecordingIndexField.RELEASE.getName())[1].stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getFieldables(RecordingIndexField.RELEASE_ID.getName())[0].stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134f", doc.getFieldables(RecordingIndexField.RELEASE_ID.getName())[1].stringValue());
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getFieldable(RecordingIndexField.NUM_TRACKS.getName()).stringValue()));
            assertEquals(7, NumericUtils.prefixCodedToInt(doc.getFieldables(RecordingIndexField.TRACKNUM.getName())[0].stringValue()));
            assertEquals(4, NumericUtils.prefixCodedToInt(doc.getFieldables(RecordingIndexField.TRACKNUM.getName())[1].stringValue()));
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getFieldable(RecordingIndexField.NUM_TRACKS_RELEASE.getName()).stringValue()));
            assertEquals(33000, NumericUtils.prefixCodedToInt(doc.getFieldable(RecordingIndexField.RECORDING_DURATION_OUTPUT.getName()).stringValue()));
            assertEquals("Non-Album Tracks", doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName())[0].stringValue());
            assertEquals("Album", doc.getFieldables(RecordingIndexField.RELEASE_TYPE.getName())[1].stringValue());
            assertEquals("Official", doc.getFieldables(RecordingIndexField.RELEASE_STATUS.getName())[0].stringValue());
            assertEquals("Promotion", doc.getFieldables(RecordingIndexField.RELEASE_STATUS.getName())[1].stringValue());

            assertEquals("FRAAA9000038", doc.getFieldable(RecordingIndexField.ISRC.getName()).stringValue());
            assertEquals("1", doc.getFieldables(RecordingIndexField.POSITION.getName())[0].stringValue());
            assertEquals("1", doc.getFieldables(RecordingIndexField.POSITION.getName())[1].stringValue());
        }
        ir.close();
    }
}