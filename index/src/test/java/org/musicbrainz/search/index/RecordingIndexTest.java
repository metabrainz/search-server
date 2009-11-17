package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;
import org.musicbrainz.search.index.RecordingIndex;
import org.musicbrainz.search.index.RecordingIndexField;

import java.sql.Connection;
import java.sql.Statement;


public class RecordingIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(RecordingIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        RecordingIndex li = new RecordingIndex(createConnection());
        li.init();
        li.indexData(writer, 0, Integer.MAX_VALUE);
        li.destroy();
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

        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',2,null, 1978,null, 1995, 2, 0)");
        stmt.addBatch("INSERT INTO artist_credit( " +
                " id, artistcount, refcount) " +
                " VALUES (1, 1, 1)");

        stmt.addBatch("INSERT INTO artist_credit_name(" +
                "    artist_credit, position, artist,name, joinphrase)" +
                "    VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (1, 'Crocodiles', 0)");
        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (2, 'Crocodiles (bonus disc)', 0)");
        stmt.addBatch("INSERT INTO release_group( id, gid,name,artist_credit,type,comment,editpending)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 1, null, 0)");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1,491240,null,1,1,1, 1, 1, 1, 1, null, null, 1)");
        stmt.addBatch("INSERT INTO release_meta(id, lastupdate, dateadded, coverarturl, infourl, amazonasin,amazonstore) VALUES (491240, null,null,null,null,'123456789',null)");
        stmt.addBatch("INSERT INTO medium(id, tracklist, release, position, format, name, editpending) VALUES (1, 1, 491240, 1, 7, null, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc(id, medium, cdtoc, editpending) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc(id, medium, cdtoc, editpending) VALUES (2, 1, 3, 1)");
        stmt.addBatch("INSERT INTO tracklist(id, trackcount) VALUES (1,2)");

        stmt.addBatch("INSERT INTO track(id, recording, tracklist, position, name, artist_credit,length, editpending) "
                + "VALUES (1, 1, 1, 4, 2, 1,33100, 1)");
        stmt.addBatch("INSERT INTO recording(id, gid, name, artist_credit, length, comment, editpending)"
                + "VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1,1,33000, null,1)");
        stmt.addBatch("INSERT INTO track_name(id, name, refcount)VALUES (1, 'Do It Clean', 1) ");
        stmt.addBatch("INSERT INTO track_name(id, name, refcount)VALUES (2, 'Do It Cleans', 1) ");

        stmt.addBatch("INSERT INTO isrc(" +
                      " id, recording, isrc, source, editpending)" +
                      " VALUES (1, 1, '1234568', null, 1);");
        stmt.addBatch("INSERT INTO isrc(" +
                      " id, recording, isrc, source, editpending)" +
                      " VALUES (2, 1, 'abcdefghi', null, 1);");
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


        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (1,'Echo & The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (2,'Echo and The Bunnymen',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (3,'Echo & The Bunnyman',1)");
        stmt.addBatch("INSERT INTO artist_name(id,name,refcount) values (4,'Echo And The Bunnymen',1)");

        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(1,16153,2,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(2,16153,3,0);");
        stmt.addBatch("INSERT INTO artist_alias(id, artist, name, editpending) VALUES(3,16153,4,0);");

        stmt.addBatch("INSERT INTO artist(id,name, gid, sortname,comment, begindate_year,begindate_month,enddate_year,type,editpending)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245',1,'a comment', 1978,null, 1995, 2, 0)");
        stmt.addBatch("INSERT INTO artist_credit( " +
                " id, artistcount, refcount) " +
                " VALUES (1, 1, 1)");

        stmt.addBatch("INSERT INTO artist_credit_name(" +
                "    artist_credit, position, artist,name, joinphrase)" +
                "    VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (1, 'Crocodiles', 0)");
        stmt.addBatch("INSERT INTO release_name(id,name, refcount)VALUES (2, 'Crocodiles (bonus disc)', 0)");
        stmt.addBatch("INSERT INTO release_group( id, gid,name,artist_credit,type,comment,editpending)" +
                "    VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1,null, null, 0)");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "language, script, date_year, date_month, date_day,barcode, comment, editpending) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1,491240,null,1,1,1, 1, 1, 1, 1, null, null, 1)");
        stmt.addBatch("INSERT INTO release_meta(id, lastupdate, dateadded, coverarturl, infourl, amazonasin,amazonstore) VALUES (491240, null,null,null,null,'123456789',null)");
        stmt.addBatch("INSERT INTO medium(id, tracklist, release, position, format, name, editpending) VALUES (1, 1, 491240, 1, 7, null, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc(id, medium, cdtoc, editpending) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc(id, medium, cdtoc, editpending) VALUES (2, 1, 3, 1)");
        stmt.addBatch("INSERT INTO tracklist(id, trackcount) VALUES (1,2)");
        stmt.addBatch("INSERT INTO track(id, recording, tracklist, position, name, artist_credit,length, editpending) "
                + "VALUES (1, 1, 1, 4, 1, 1,33100, 1)");
        stmt.addBatch("INSERT INTO recording(id, gid, name, artist_credit, length, comment, editpending)"
                + "VALUES (1, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1,1,33000, null,1)");
        stmt.addBatch("INSERT INTO track_name(id, name, refcount)VALUES (1, 'Do It Clean', 1) ");
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testIndexRecording() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(2, doc.getFields(RecordingIndexField.RECORDING.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.TRACK_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_ID.getName()).length);
            assertEquals(2, doc.getFields(RecordingIndexField.ISRC.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getField(RecordingIndexField.RECORDING_ID.getName()).stringValue());
            assertEquals("Do It Clean", doc.getField(RecordingIndexField.RECORDING.getName()).stringValue());
            assertEquals("Crocodiles (bonus disc)", doc.getField(RecordingIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(RecordingIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(RecordingIndexField.ARTIST.getName()).stringValue());
            assertEquals("Echo and The Bunnymen", doc.getField(RecordingIndexField.ARTIST_SORTNAME.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(RecordingIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.NUM_TRACKS.getName()).stringValue()));
            assertEquals(4, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.TRACKNUM.getName()).stringValue()));
            assertEquals(33000, NumericUtils.prefixCodedToInt(doc.getField(RecordingIndexField.DURATION.getName()).stringValue()));
            assertEquals("1234568", doc.getField(RecordingIndexField.ISRC.getName()).stringValue());
            
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
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals("Non-Album Tracks", doc.getField(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());

        }
        ir.close();
    }

     /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testArtistSortname() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.ARTIST_SORTNAME.getName()).length);
            assertEquals("Echo and The Bunnymen", doc.getField(RecordingIndexField.ARTIST_SORTNAME.getName()).stringValue());

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
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals("-", doc.getField(RecordingIndexField.RELEASE_TYPE.getName()).stringValue());
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
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.ARTIST_COMMENT.getName()).length);
            assertEquals("-", doc.getField(RecordingIndexField.ARTIST_COMMENT.getName()).stringValue());

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
            assertEquals(1, doc.getFields(RecordingIndexField.RECORDING_OUTPUT.getName()).length);
            assertEquals(1, doc.getFields(RecordingIndexField.ARTIST_COMMENT.getName()).length);
            assertEquals("a comment", doc.getField(RecordingIndexField.ARTIST_COMMENT.getName()).stringValue());

        }
        ir.close();
    }

    /**
     *
     * @throws Exception
     */

    public void testTrackName() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
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
     * @throws Exception
     */
    public void testISRC() throws Exception {

        addTrackOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(2, doc.getFields(RecordingIndexField.ISRC.getName()).length);
            assertEquals("1234568", doc.getField(RecordingIndexField.ISRC.getName()).stringValue());

        }
        ir.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testNoISRC() throws Exception {

        addTrackTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(0, doc.getFields(RecordingIndexField.ISRC.getName()).length);

        }
        ir.close();
    }
    public void testToAvoidWarnings()
     {
         assertEquals(1,1);
     }

}