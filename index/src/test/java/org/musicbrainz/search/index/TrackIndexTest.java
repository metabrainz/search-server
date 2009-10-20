package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.TrackIndex;
import org.musicbrainz.search.index.TrackIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class TrackIndexTest extends AbstractIndexTest {

    private static Class INDEX_FIELD_CLASS = TrackIndexField.class;
    
    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new PerFieldEntityAnalyzer(INDEX_FIELD_CLASS), true, IndexWriter.MaxFieldLength.LIMITED);
        TrackIndex ti = new TrackIndex(createConnection());
        ti.init();
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }


    /**
     * All Basic Fields Plus Release Events
     *
     * @throws Exception
     */
    private void addTrackOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (1, 'Echo & The Bunnymen', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (2, 'Echo and The Bunnymen', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (3, 'Echo & The Bunnyman', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (4, 'Echo And The Bunnymen', 1)");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(1, 16153, 2);");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(2, 16153, 3);");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(3, 16153, 4);");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,null, 1978,null, 1995, 2)"
        );
        stmt.addBatch("INSERT INTO artist_credit (id, artistcount, refcount) VALUES (1, 1, 1)");

        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                "    VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (1, 'Crocodiles', 0)");
        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (2, 'Crocodiles (bonus disc)', 0)");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 1, null)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "    language, script, date_year, date_month, date_day, barcode, comment) " +
                "  VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, null, 1, 1, 1, 1, 1, 1, 1, null, null)"
        );
        stmt.addBatch("INSERT INTO release_meta (id, amazonasin) VALUES (491240, '123456789')");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format, name) VALUES (1, 1, 491240, 1, 7, null)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (2, 1, 3)");
        stmt.addBatch("INSERT INTO tracklist (id, trackcount) VALUES (1,2)");

        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) " +
                "VALUES (1, 1, 1, 4, 1, 1, 33100)"
        );
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length, comment) " +
                "VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1,1,33000, null)"
        );
        stmt.addBatch("INSERT INTO track_name (id, name, refcount) VALUES (1, 'Do It Clean', 1)");
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * All Basic Fields Plus Release Events
     *
     * @throws Exception
     */
    private void addTrackTwo() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();


        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (1, 'Echo & The Bunnymen', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (2, 'Echo and The Bunnymen', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (3, 'Echo & The Bunnyman', 1)");
        stmt.addBatch("INSERT INTO artist_name (id, name, refcount) VALUES (4, 'Echo And The Bunnymen', 1)");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(1, 16153, 2);");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(2, 16153, 3);");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES(3, 16153, 4);");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (16153, 1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 'a comment', 1978, null, 1995, 2)"
        );
        stmt.addBatch("INSERT INTO artist_credit (id, artistcount, refcount) VALUES (1, 1, 1)");

        stmt.addBatch("INSERT INTO artist_credit_name(artist_credit, position, artist,name, joinphrase) " +
                "    VALUES (1, 0, 16153, 1, null)"
        );

        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (1, 'Crocodiles', 0)");
        stmt.addBatch("INSERT INTO release_name (id, name, refcount) VALUES (2, 'Crocodiles (bonus disc)', 0)");
        stmt.addBatch("INSERT INTO release_group (id, gid,name, artist_credit, type, comment)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1,null, null)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "language, script, date_year, date_month, date_day, barcode, comment) " +
                "  VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, null, 1, 1, 1, 1, 1, 1, 1, null, null)");
        stmt.addBatch("INSERT INTO release_meta (id, amazonasin) VALUES (491240, '123456789')");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format, name) VALUES (1, 1, 491240, 1, 7, null)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (2, 1, 3)");
        stmt.addBatch("INSERT INTO tracklist (id, trackcount) VALUES (1,2)");
        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) " +
                "  VALUES (1, 1, 1, 4, 1, 1, 33100)"
        );
        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length, comment)" +
                "  VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1,1,33000, null)"
        );
        stmt.addBatch("INSERT INTO track_name (id, name, refcount) VALUES (1, 'Do It Clean', 1) ");
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testIndexTrack() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(TrackIndexField.TRACK_ID.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getField(TrackIndexField.TRACK_ID.getName()).stringValue());
            assertEquals("Do It Clean", doc.getField(TrackIndexField.TRACK.getName()).stringValue());
            assertEquals("Crocodiles (bonus disc)", doc.getField(TrackIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(TrackIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(TrackIndexField.ARTIST.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(TrackIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getField(TrackIndexField.NUM_TRACKS.getName()).stringValue()));
            assertEquals(4, NumericUtils.prefixCodedToInt(doc.getField(TrackIndexField.TRACKNUM.getName()).stringValue()));
            assertEquals(33000, NumericUtils.prefixCodedToInt(doc.getField(TrackIndexField.DURATION.getName()).stringValue()));
            assertEquals(16, NumericUtils.prefixCodedToInt(doc.getField(TrackIndexField.QUANTIZED_DURATION.getName()).stringValue()));
        }
        ir.close();
    }

     /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testReleaseType() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(TrackIndexField.TRACK.getName()).length);
            assertEquals(1, doc.getFields(TrackIndexField.RELEASE_TYPE.getName()).length);
            assertEquals("Non Album Tracks", doc.getField(TrackIndexField.RELEASE_TYPE.getName()).stringValue());

        }
        ir.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testNoReleaseType() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(TrackIndexField.TRACK.getName()).length);
            assertEquals(0, doc.getFields(TrackIndexField.RELEASE_TYPE.getName()).length);
        }
        ir.close();
    }

     /**
     *
     * @throws Exception
     */

    public void testNoArtistComment() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(TrackIndexField.TRACK.getName()).length);
            assertEquals(1, doc.getFields(TrackIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(0, doc.getFields(TrackIndexField.ARTIST_COMMENT.getName()).length);
        }
        ir.close();
    }

     /**
     *
     * @throws Exception
     */
    public void testArtistComment() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(TrackIndexField.TRACK.getName()).length);
            assertEquals(1, doc.getFields(TrackIndexField.ARTIST_COMMENT.getName()).length);
            assertEquals("a comment", doc.getField(TrackIndexField.ARTIST_COMMENT.getName()).stringValue());

        }
        ir.close();
    }

    public void testToAvoidWarnings()
     {
         assertEquals(1,1);
     }

}