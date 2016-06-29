package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.junit.Test;
import org.musicbrainz.mmd2.*;

import java.sql.Statement;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RecordingIndexTest extends AbstractIndexTest {

	private void createIndex(RAMDirectory ramDir) throws Exception {
		createIndex(ramDir, true);
	}
	
    private void createIndex(RAMDirectory ramDir, boolean useTemporaryTables) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir, RecordingIndexField.class);
        RecordingIndex ri = new RecordingIndex(conn);
        CommonTables ct = new CommonTables(conn, ri.getName());
        ct.createTemporaryTables(!useTemporaryTables);
        ri.init(writer, !useTemporaryTables);
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

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 'Echo & The Bunnymen', 'Echo and The Bunnymen','')");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, sort_name, locale, edits_pending, last_updated) VALUES (1, 16153, 'Echo & Bunneymen','Bunneymen & Echo', 'en',1,null)");

        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (3,  'Echo & The Bunnymen', 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (3, 0, 16153, 'Echo & The Bunnymen', '')");

        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 'Crocodiles', 1, 1)");

        stmt.addBatch("INSERT INTO release_group_secondary_type_join (release_group, secondary_type) VALUES (491240,1)");
        stmt.addBatch("INSERT INTO release_group_secondary_type_join (release_group, secondary_type) VALUES (491240,2)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, " +
                "  language, script) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 'Crocodiles (bonus disc)', 3, 491240, 1, 1, 1, 1)");

        stmt.addBatch("INSERT INTO release_country (release, country, date_year, date_month, date_day) values (491240, 221, 1970,1,1)");
        stmt.addBatch("INSERT INTO area (id, gid, name) VALUES (221, 'c3b8dbc9-c1ff-4743-9015-8d762819134g','United Kingdom')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (221, 'GB')");

        stmt.addBatch("INSERT INTO release_country (release, country) values (491240, 222)");
        stmt.addBatch("INSERT INTO area (id, gid, name) VALUES (222, 'c3b8dbc9-c1ff-4743-9015-8d762819134e','Abania')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (222, 'AF')");

        stmt.addBatch("INSERT INTO release_country (release, country) values (491240, 2)");
        stmt.addBatch("INSERT INTO area (id, gid,name) VALUES (2,  'c3b8dbc9-c1ff-4743-9015-8d762819134f','Afghanistan')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (2, 'AN')");

        stmt.addBatch("INSERT INTO release_unknown_country (release, date_year) values (491240, 1950)");

        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format) VALUES (1, 2, 491240, 1, 7)");

        stmt.addBatch("INSERT INTO track (id, gid, recording, medium, position, number, name, artist_credit, length,is_data_track ) "
                + " VALUES (1, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 1, 1, 4, 'A4', 'Do It Cleans', 1, 33100,true)");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length, comment, video)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 'Do It Clean', 3, 33000, 'demo', true)");

        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (1, 1, 'FRAAA9000038')");
        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (2, 1, 'FRAAA9100082')");


        stmt.addBatch("INSERT INTO area (id, name) VALUES (1, 'United Kingdom')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (1, 'UK')");

        stmt.executeBatch();
        stmt.close();
    }

    /**
     * All Basic Fields Plus Release Events and different release artist and track artist to recording artist
     *
     * @throws Exception exception
     */
    private void addTrackTwo() throws Exception {

        Statement stmt = conn.createStatement();

        //Recording
        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 'Echo & The Bunnymen', 1, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 'Echo & The Bunnymen', 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (1, 0, 16153, 'Echo & The Bunnymen','')");

        //Track
        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (2, 'ddd4879c-5e88-4385-b131-bf65296bf245', 'Pixies','Pixies', 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (2, 'Pixies', 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (2, 0, 2, 'Pixies')");

        //Release
        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (3, 'eee4879c-5e88-4385-b131-bf65296bf245', 'Poxies','Poxies', 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (3, 'The Poxies', 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (3, 0, 3, 'The Poxies')");


        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37',  'Crocodiles', 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, packaging, " +
                "  language, script) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 'Crocodiles (bonus disc)', 3, 491240, 1, 1, 1)");
        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format) VALUES (1, 2, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO track (id, gid, recording, medium, position, name, artist_credit, length) "
                + " VALUES (1, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 1, 1, 4, 'Do It Clean', 2, 33100)");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 'Do It Clean', 1, 33000)");
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

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 'Echo & The Bunnymen', 'Echo and The Bunnymen','')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 'Echo & The Bunnymen', 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (1, 0, 16153, 'Echo & The Bunnymen', '')");

        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37',  'Crocodiles', 1, 1)");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491243, 'efd2ace2-b3b9-305f-8a53-9803595c0e67', 'Crocodiles (bonus disc)', 1, 2)");
        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, " +
                "  language, script) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 'Crocodiles (bonus disc)', 1, 491240, 1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format, name) VALUES (1, 2, 491240, 1, 7, null)");
        stmt.addBatch("INSERT INTO track (id, gid, recording, medium, position, name, artist_credit, length) "
                + " VALUES (1, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 1, 1, 7, 'Do It Cleans', 1, 33100)");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 'Do It Cleans', 1, 33000)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, " +
                "  language, script) " +
                " VALUES (491241, 'c3b8dbc9-c1ff-4743-9015-8d762819134f', 'Crocodiles', 1, 491243, 2, 1, 1)");


        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format) VALUES (2, 2, 491241, 1, 7)");

        stmt.addBatch("INSERT INTO track (id, gid, recording, medium, position, name, artist_credit, length) "
                + " VALUES (2, 'd3b8dbc9-c1ff-4743-9015-8d762819134e', 1, 2, 4, 2, 1, 33100)");
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

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 'Echo & The Bunnymen', 'Echo and The Bunnymen','')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 'Echo and The Bunnymen', 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (1, 0, 16153, 'Echo and The Bunnymen', '')");

        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 'Do It Clean', 1, 33000)");

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
        createIndex(ramDir, true);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RECORDING_ID, "2f250ed2-6285-40f1-aa2a-14f1c05e9765");
            checkTerm(ir, RecordingIndexField.RELEASE_TYPE, "compilation");
            checkTerm(ir, RecordingIndexField.RELEASE_STATUS, "official");
            checkTerm(ir, RecordingIndexField.ISRC, "fraaa9000038");
            checkTerm(ir, RecordingIndexField.RELEASE, "bonus");
            checkTerm(ir, RecordingIndexField.RELEASE_ID, "c3b8dbc9-c1ff-4743-9015-8d762819134e");
            checkTerm(ir, RecordingIndexField.POSITION, "1");
            checkTerm(ir, RecordingIndexField.NUM_TRACKS_RELEASE, 2);
            checkTerm(ir, RecordingIndexField.TRACKNUM, 4);
            checkTerm(ir, RecordingIndexField.NUM_TRACKS_RELEASE, 2);
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexRecordingWithoutTemporaryTables() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir, false);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RECORDING_ID, "2f250ed2-6285-40f1-aa2a-14f1c05e9765");
            checkTerm(ir, RecordingIndexField.RELEASE_TYPE, "compilation");
            checkTerm(ir, RecordingIndexField.RELEASE_STATUS, "official");
            checkTerm(ir, RecordingIndexField.ISRC, "fraaa9000038");
            checkTerm(ir, RecordingIndexField.RELEASE, "bonus");
            checkTerm(ir, RecordingIndexField.RELEASE_ID, "c3b8dbc9-c1ff-4743-9015-8d762819134e");
            checkTerm(ir, RecordingIndexField.POSITION, "1");
            checkTerm(ir, RecordingIndexField.NUM_TRACKS_RELEASE, 2);
            checkTerm(ir, RecordingIndexField.TRACKNUM, 4);
            checkTerm(ir, RecordingIndexField.NUM_TRACKS_RELEASE, 2);
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RECORDING_ID, "2f250ed2-6285-40f1-aa2a-14f1c05e9765");
            checkTerm(ir, RecordingIndexField.RELEASE_TYPE, "standalone");
        }
        ir.close();
    }

    /**
     * Release Group Field
     *
     * @throws Exception exception
     */
    @Test
    public void testReleaseGroupId() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RELEASEGROUP_ID, "efd2ace2-b3b9-305f-8a53-9803595c0e37");
        }
        ir.close();
    }

    /**
     * Release Group Field
     *
     * @throws Exception exception
     */
    @Test
    public void testIsVideo() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.VIDEO, "true");
        }
        ir.close();
    }


    /**
     * Old Type Field
     *
     * @throws Exception exception
     */
    @Test
    public void testReleaseType() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RELEASE_TYPE, "compilation");
        }
        ir.close();
    }

    /**
     * Old Type Field
     *
     * @throws Exception exception
     */
    @Test
    public void testReleasePrimaryType() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RELEASE_PRIMARY_TYPE, "album");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.COUNTRY, "af");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RELEASE_DATE, "1950");

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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RELEASE_TYPE, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RELEASE_STATUS, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);


            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());


            ac = recording.getReleaseList().getRelease().get(0).getMediumList().getMedium().get(0).getTrackList().getDefTrack().get(0).getArtistCredit();
            assertNotNull(ac);
            assertEquals("Pixies", ac.getNameCredit().get(0).getArtist().getName());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testReleaseArtist() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);


            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());


            ac = recording.getReleaseList().getRelease().get(0).getArtistCredit();
            assertNotNull(ac);
            assertEquals("Poxies", ac.getNameCredit().get(0).getArtist().getName());
            assertEquals("The Poxies", ac.getNameCredit().get(0).getName());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testNoReleaseArtistBecauseSameAsRecordingArtist() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);


            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());


            ac = recording.getReleaseList().getRelease().get(0).getArtistCredit();
            assertNull(ac);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testNoTrackArtistBecauseSameAsRecordingArtist() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);


            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            ArtistCredit ac = recording.getArtistCredit();
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());


            ac = recording.getReleaseList().getRelease().get(0).getMediumList().getMedium().get(0).getTrackList().getDefTrack().get(0).getArtistCredit();
            assertNull(ac);
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.TAG, "punk");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.ISRC, "fraaa9000038");
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    @Test
    public void testTrackGuid() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.TRACK_ID, "c3b8dbc9-c1ff-4743-9015-8d762819134e");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.ISRC, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.COMMENT, "demo");
        }
        ir.close();
    }

    /**
     * Test Free Text Track Number
     *
     * @throws Exception exception
     */
    @Test
    public void testTrackNumber() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.NUMBER, "a4");
        }
        ir.close();
    }

    /**
     * Test Track Position
     *
     * @throws Exception exception
     */
    @Test
    public void testTrackPosition() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.TRACKNUM, 4);
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.FORMAT, "vinyl");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(RecordingIndexField.PUID.getName()).length);
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, RecordingIndexField.RECORDING_ID, "2f250ed2-6285-40f1-aa2a-14f1c05e9765");
            checkTerm(ir, RecordingIndexField.RELEASE_TYPE, "album");
            checkTerm(ir, RecordingIndexField.RELEASE_STATUS, "official");
            checkTerm(ir, RecordingIndexField.ISRC, "fraaa9000038");
            checkTerm(ir, RecordingIndexField.RELEASE, "bonus");
            checkTerm(ir, RecordingIndexField.RELEASE_ID, "c3b8dbc9-c1ff-4743-9015-8d762819134e");
            checkTerm(ir, RecordingIndexField.POSITION, "1");

            checkTermX(ir, RecordingIndexField.RELEASE_ID, "c3b8dbc9-c1ff-4743-9015-8d762819134f", 1);
            checkTermX(ir, RecordingIndexField.RELEASE_TYPE, "single",1);
            checkTermX(ir, RecordingIndexField.RELEASE_STATUS, "promotion",1);
            checkTerm(ir, RecordingIndexField.NUM_TRACKS_RELEASE, 2);
            checkTerm(ir, RecordingIndexField.TRACKNUM, 4);
            checkTerm(ir, RecordingIndexField.NUM_TRACKS_RELEASE, 2);

        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testStoredRecording() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Recording recording = (Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE.getName()), Recording.class);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", recording.getId());
            assertEquals("Do It Clean", recording.getTitle());

            ArtistCredit ac = recording.getArtistCredit();
            assertEquals("Echo & Bunneymen", ac.getNameCredit().get(0).getArtist().getAliasList().getAlias().get(0).getContent());
            assertEquals("Bunneymen & Echo", ac.getNameCredit().get(0).getArtist().getAliasList().getAlias().get(0).getSortName());
            assertEquals("true",recording.getVideo());

            ReleaseList releaseList = recording.getReleaseList();
            Release     release     = releaseList.getRelease().get(0);
            assertNotNull(release);
            assertEquals("Crocodiles (bonus disc)", release.getTitle());
            assertEquals("Official", release.getStatus().getContent());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", release.getId());
            assertEquals(null, release.getCountry());
            assertEquals("1950", release.getDate());
            ReleaseEventList rel = release.getReleaseEventList();
            assertEquals(4, rel.getReleaseEvent().size());
            assertEquals(null, release.getCountry());
            assertEquals("1950", release.getDate());
            assertEquals(null, rel.getReleaseEvent().get(0).getArea());
            assertEquals("1950", rel.getReleaseEvent().get(0).getDate());
            assertEquals("GB", rel.getReleaseEvent().get(1).getArea().getIso31661CodeList().getIso31661Code().get(0));
            assertEquals("1970-01-01", rel.getReleaseEvent().get(1).getDate());
            assertEquals("c3b8dbc9-c1ff-4743-0901-58d762819134", rel.getReleaseEvent().get(1).getArea().getId());
            assertEquals("United Kingdom", rel.getReleaseEvent().get(1).getArea().getName());
            assertEquals("United Kingdom", rel.getReleaseEvent().get(1).getArea().getSortName());

            assertEquals("AF", rel.getReleaseEvent().get(2).getArea().getIso31661CodeList().getIso31661Code().get(0));
            assertEquals(null, rel.getReleaseEvent().get(2).getDate());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", rel.getReleaseEvent().get(2).getArea().getId());
            assertEquals("Abania", rel.getReleaseEvent().get(2).getArea().getName());
            assertEquals("Abania", rel.getReleaseEvent().get(2).getArea().getSortName());

            assertEquals("AN", rel.getReleaseEvent().get(3).getArea().getIso31661CodeList().getIso31661Code().get(0));
            assertEquals(null, rel.getReleaseEvent().get(3).getDate());

            ReleaseGroup releaseGroup = release.getReleaseGroup();
            assertNotNull(releaseGroup);
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37",releaseGroup.getId());
            assertEquals("Compilation",releaseGroup.getType());
            assertEquals("Album",releaseGroup.getPrimaryType().getContent());

            SecondaryTypeList secondaryTypeList = releaseGroup.getSecondaryTypeList();
            assertNotNull(secondaryTypeList);
            assertEquals("Compilation",secondaryTypeList.getSecondaryType().get(0).getContent());
            assertEquals("Interview",secondaryTypeList.getSecondaryType().get(1).getContent());

            MediumList mediumList = release.getMediumList();
            assertNotNull(mediumList);
            assertEquals(2,mediumList.getTrackCount().intValue());

            Medium medium = mediumList.getMedium().get(0);
            assertNotNull(medium);
            assertEquals(1, medium.getPosition().intValue());
            assertEquals("Vinyl", medium.getFormat().getContent());

            Medium.TrackList trackList = medium.getTrackList();
            assertNotNull(trackList);

            assertNotNull(trackList.getDefTrack());

            DefTrackData track = trackList.getDefTrack().get(0);
            assertEquals("Do It Cleans", track.getTitle());
            assertNull(track.getPosition());  //We do not currently output this, but perhaps should
            assertEquals("A4", track.getNumber());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e",track.getId());


        }
        ir.close();
    }
}
