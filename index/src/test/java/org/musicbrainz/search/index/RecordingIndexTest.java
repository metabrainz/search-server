package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class RecordingIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(RecordingIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        RecordingIndex ri = new RecordingIndex(createConnection());
        ri.init(writer);
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
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen')");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (1, 16153, 2)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (2, 16153, 3)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (3, 16153, 4)");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',2,null, 1978,null, 1995, 2, 0)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist,name, joinphrase) " +
                " VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 1, null)");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day, barcode, comment) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1970, 1, 1, null, null)");
        stmt.addBatch("INSERT INTO release_meta (id, lastupdate, dateadded, coverarturl, infourl, amazonasin, amazonstore) " +
        	" VALUES (491240, null, null, null, null, '123456789', null)");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format, name) VALUES (1, 1, 491240, 1, 7, null)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (2, 1, 3)");
        stmt.addBatch("INSERT INTO tracklist (id, trackcount) VALUES (1, 2)");

        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                + " VALUES (1, 1, 1, 4, 2, 1,33100)");
        stmt.addBatch("INSERT INTO recording(id, gid, name, artist_credit, length, comment)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000, null)");


        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean')");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (2, 'Do It Cleans')");

        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (1, 1, '1234568')");
        stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (2, 1, 'abcdefghi')");

        stmt.addBatch("INSERT INTO puid(id, puid)VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e38');");
        stmt.addBatch("INSERT INTO recording_puid(id, puid, recording, editpending)VALUES (1, 1, 1, 0)");
                
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * All Basic Fields Plus Release Events
     *
     * @throws Exception exception
     */
    private void addTrackTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen')");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (1, 16153, 2)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (2, 16153, 3)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (3, 16153, 4)");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist,name, joinphrase) " +
                " VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1,null, null)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day,barcode, comment) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, null, 1, 1, 1, 1, 1, 1, 1, null, null)");
        stmt.addBatch("INSERT INTO release_meta (id, lastupdate, dateadded, coverarturl, infourl, amazonasin, amazonstore) " +
        	" VALUES (491240, null, null, null, null, '123456789', null)");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format, name) VALUES (1, 1, 491240, 1, 7, null)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (2, 1, 3)");
        stmt.addBatch("INSERT INTO tracklist (id, trackcount) VALUES (1, 2)");
        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                + " VALUES (1, 1, 1, 4, 1, 1, 33100)");
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length, comment)"
                + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000, null)");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean')");
        stmt.addBatch("INSERT INTO tag (id, name, refcount) VALUES (1, 'punk', 2)");
        stmt.addBatch("INSERT INTO recording_tag (recording, tag, count) VALUES (1, 1, 10)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Add recording that is on two releases
     *
     * @throws Exception
     */
    private void addTrackThree() throws Exception {
            Connection conn = createConnection();
            conn.setAutoCommit(true);

            Statement stmt = conn.createStatement();

            stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
            stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");
            stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo & The Bunnyman')");
            stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen')");

            stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (1, 16153, 2)");
            stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (2, 16153, 3)");
            stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (3, 16153, 4)");

            stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                    " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',2,null, 1978,null, 1995, 2, 0)");
            stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
            stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist,name, joinphrase) " +
                    " VALUES (1, 0, 16153, 1, null)");

            stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
            stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
            stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                    " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 1, null)");
            stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                    " VALUES (491243, 'efd2ace2-b3b9-305f-8a53-9803595c0e67', 2, 1, 2, null)");

            stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging, country, " +
                    "  language, script, date_year, date_month, date_day, barcode, comment) " +
                    " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1970, 1, 1, null, null)");
            stmt.addBatch("INSERT INTO release_meta (id, lastupdate, dateadded, coverarturl, infourl, amazonasin, amazonstore) " +
                " VALUES (491240, null, null, null, null, '123456789', null)");
            stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format, name) VALUES (1, 1, 491240, 1, 7, null)");
            stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
            stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (2, 1, 3)");
            stmt.addBatch("INSERT INTO tracklist (id, trackcount) VALUES (1, 2)");

            stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                    + " VALUES (1, 1, 1, 7, 2, 1,33100)");
            stmt.addBatch("INSERT INTO recording(id, gid, name, artist_credit, length, comment)"
                    + " VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000, null)");



            stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging, country, " +
                    "  language, script, date_year, date_month, date_day, barcode, comment) " +
                    " VALUES (491241, 'c3b8dbc9-c1ff-4743-9015-8d762819134f', 1, 1, 491243, 2, 1, 1, 1, 1, 1970, 1, 1, null, null)");
            stmt.addBatch("INSERT INTO release_meta (id, lastupdate, dateadded, coverarturl, infourl, amazonasin, amazonstore) " +
                " VALUES (491241, null, null, null, null, '123456789', null)");

            stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format, name) VALUES (2, 2, 491241, 1, 7, null)");
            stmt.addBatch("INSERT INTO tracklist (id, trackcount) VALUES (2, 2)");
            stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                    + " VALUES (2, 1, 2, 4, 2, 1,33100)");
            stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'Do It Clean')");
            stmt.addBatch("INSERT INTO track_name (id, name) VALUES (2, 'Do It Cleans')");

            stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (1, 1, '1234568')");
            stmt.addBatch("INSERT INTO isrc (id, recording, isrc) VALUES (2, 1, 'abcdefghi')");

            stmt.addBatch("INSERT INTO puid(id, puid)VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e38');");
            stmt.addBatch("INSERT INTO recording_puid(id, puid, recording, editpending)VALUES (1, 1, 1, 0)");

            stmt.executeBatch();
            stmt.close();
            conn.close();
        }


    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    public void testIndexRecording() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.TRACK_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_ID.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_STATUS.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.ISRC.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getField(RecordingIndexField.RECORDING_ID.getName()).stringValue());
            assertEquals("Crocodiles (bonus disc)", doc.getField(RecordingIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(RecordingIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.NUM_TRACKS.getName()).stringValue()));
            assertEquals(4, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.TRACKNUM.getName()).stringValue()));
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.NUM_TRACKS_RELEASE.getName()).stringValue()));
            assertEquals(33000, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.DURATION.getName()).stringValue()));
            assertEquals("Non-Album Tracks", doc.getField(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
            assertEquals("Official", doc.getField(RecordingIndexField.RELEASE_STATUS.getName()).stringValue());
            assertEquals("1234568", doc.getField(RecordingIndexField.ISRC.getName()).stringValue());
            assertEquals("1", doc.getField(RecordingIndexField.POSITION.getName()).stringValue());
        }
        ir.close();
    }

     /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    public void testReleaseType() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals("Non-Album Tracks", doc.getField(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
        * Basic test of all fields
        *
        * @throws Exception exception
        */
       public void testReleaseDate() throws Exception {

           addTrackOne();
           RAMDirectory ramDir = new RAMDirectory();
           createIndex(ramDir);

           IndexReader ir = IndexReader.open(ramDir, true);
           assertEquals(2, ir.numDocs());
           {
               Document doc = ir.document(1);
               assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
               assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_DATE.getName()).length);
               assertEquals("1970-01-01", doc.getField(RecordingIndexField.RELEASE_DATE.getName()).stringValue());
           }
           ir.close();
       }


     /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
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
            assertEquals("Echo and The Bunnymen",ac.getNameCredit().get(0).getArtist().getSortName());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    public void testNoReleaseType() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals("-", doc.getField(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    public void testNoReleaseStatus() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_STATUS.getName()).length);
            assertEquals("-", doc.getField(RecordingIndexField.RELEASE_STATUS.getName()).stringValue());
        }
        ir.close();
    }

     /**
     *
     * @throws Exception exception
     */

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
     *
     * @throws Exception exception
     */
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
            assertEquals("a comment",ac.getNameCredit().get(0).getArtist().getDisambiguation());
        }
        ir.close();
    }

    /**
     *
     * @throws Exception exception
     */
    public void testTag() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals("punk", doc.getField(RecordingIndexField.TAG.getName()).stringValue());
        }
        ir.close();
    }

    /**
     *
     * @throws Exception exception
     */

    public void testTrackName() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals("Do It Clean", doc.getField(RecordingIndexField.RECORDING_OUTPUT.getName()).stringValue());
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.TRACK_OUTPUT.getName()).length);
            assertEquals("Do It Cleans", doc.getField(RecordingIndexField.TRACK_OUTPUT.getName()).stringValue());
        }
        ir.close();
    }

     /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    public void testISRC() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(2, doc.getFields(RecordingIndexField.ISRC.getName()).length);
            assertEquals("1234568", doc.getField(RecordingIndexField.ISRC.getName()).stringValue());
        }
        ir.close();
    }


    /**
     *
     * @throws Exception exception
     */
    public void testNoISRC() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(RecordingIndexField.ISRC.getName()).length);
        }
        ir.close();
    }

    /**
     * Test gives puid
     *
     * @throws Exception exception
     */
    public void testPuid() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(RecordingIndexField.PUID.getName()).length);
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e38", doc.getField(RecordingIndexField.PUID.getName()).stringValue());
        }
        ir.close();
    }

/**
     * Test no puid
     *
     * @throws Exception exception
     */
    public void testNoPuid() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
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
    public void testIndexRecordingOnTwoReleases() throws Exception {

        addTrackThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.TRACK_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_ID.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.RELEASE.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.RELEASE_STATUS.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.TRACKNUM.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.NUM_TRACKS.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.ISRC.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getField(RecordingIndexField.RECORDING_ID.getName()).stringValue());
            assertEquals("Crocodiles (bonus disc)", doc.getFields(RecordingIndexField.RELEASE.getName())[0].stringValue());
            assertEquals("Crocodiles", doc.getFields(RecordingIndexField.RELEASE.getName())[1].stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getFields(RecordingIndexField.RELEASE_ID.getName())[0].stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134f", doc.getFields(RecordingIndexField.RELEASE_ID.getName())[1].stringValue());
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.NUM_TRACKS.getName()).stringValue()));
            assertEquals(7, NumericUtils.prefixCodedToInt(doc.getFields(RecordingIndexField.TRACKNUM.getName())[0].stringValue()));
            assertEquals(4, NumericUtils.prefixCodedToInt(doc.getFields(RecordingIndexField.TRACKNUM.getName())[1].stringValue()));
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.NUM_TRACKS_RELEASE.getName()).stringValue()));
            assertEquals(33000, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.DURATION.getName()).stringValue()));
            assertEquals("Non-Album Tracks", doc.getFields(RecordingIndexField.RELEASE_TYPE.getName())[0].stringValue());
            assertEquals("Album", doc.getFields(RecordingIndexField.RELEASE_TYPE.getName())[1].stringValue());
            assertEquals("Official", doc.getFields(RecordingIndexField.RELEASE_STATUS.getName())[0].stringValue());
            assertEquals("Promotion", doc.getFields(RecordingIndexField.RELEASE_STATUS.getName())[1].stringValue());

            assertEquals("1234568", doc.getField(RecordingIndexField.ISRC.getName()).stringValue());
            assertEquals("1", doc.getFields(RecordingIndexField.POSITION.getName())[0].stringValue());
            assertEquals("1", doc.getFields(RecordingIndexField.POSITION.getName())[1].stringValue());
        }
        ir.close();
    }
}