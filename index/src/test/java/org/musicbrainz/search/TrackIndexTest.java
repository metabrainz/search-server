package org.musicbrainz.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class TrackIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();

    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
        TrackIndex li = new TrackIndex(createConnection());
        li.indexData(writer, 0, Integer.MAX_VALUE);
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


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,null, '1978-00-00','1995-00-00', 2, -1, 0)");


        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(1,2,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO track(" +
                "            id, artist, name, gid, length,year,modpending)" +
                "    VALUES (5555528,16153, 'Do It Clean', '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 254706, 0, 0)");

        //stmt.addBatch("INSERT INTO track(" +
        //        "            id, artist, name, gid, length,year,modpending)" +
        //        "    VALUES (5555529,16153, 'Read it in Books', '675efaf7-f94f-4181-b64c-7d94151f42c0', 0, 0, 0)");

        stmt.addBatch("INSERT INTO albumjoin(" +
                "            id, album, track,sequence, modpending)" +
                "    VALUES (555489, 491240, 5555528, 1, 0)");

        //stmt.addBatch("INSERT INTO albumjoin(" +
        //                "            id, album, track,sequence, modpending)" +
        //                "    VALUES (5555490, 491240, 5555529, 2, 0)");


        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', null, null, " +
                "            null, 5, 1, null)");

        stmt.addBatch("INSERT INTO language(" +
                "            id, isocode_3t, isocode_3b, isocode_2, name, frequency)" +
                "    VALUES (120, 'eng', 'eng', 'en', 'English', 2);");

        stmt.addBatch("INSERT INTO script(" +
                "            id, isocode, isonumber, name, frequency)" +
                "    VALUES (28, 'Latn', 215, 'Latn', 4);");

        stmt.addBatch("INSERT INTO country(id, isocode, name)" +
                " VALUES (221, 'AF','Afghanistan')");

        stmt.addBatch("INSERT INTO release(" +
                "            id, album, country, releasedate, modpending, label, catno, barcode,format)" +
                "    VALUES (1, 491240, 221, '1970-01-01', null, null,null,null,null)");

        stmt.addBatch("INSERT INTO labelalias(id, ref, name, timesused, modpending, lastused)" +
                " VALUES (1327, 563, '4AD US', 0, 0, '1970-01-01')");
        stmt.addBatch("INSERT INTO label(id,name, gid, modpending, labelcode,sortname,country, page, resolution, begindate,enddate,type)" +
                "    VALUES (563, '4AD', 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,null,'4AD',1,2260992,null, null, null,null)");
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


        stmt.addBatch("INSERT INTO artist(id,name, gid, modpending, sortname, page, resolution, begindate,enddate,type,quality,modpending_qual)" +
                "    VALUES (16153, 'Echo & The Bunnymen', 'ccd4879c-5e88-4385-b131-bf65296bf245',0, 'Echo & The Bunnymen', 205832224,'a comment', '1978-00-00','1995-00-00', 2, -1, 0)");


        stmt.addBatch("INSERT INTO album(" +
                "            id, artist, name, gid, modpending,attributes, page, language," +
                "            script, modpending_lang, quality, modpending_qual, release_group)" +
                "    VALUES (491240, 16153, 'Crocodiles (bonus disc)', 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 0,(0,100), 154669573, 120, " +
                "            28, null, -1, 0, 491240)");

        stmt.addBatch("INSERT INTO track(" +
                "            id, artist, name, gid, length,year,modpending)" +
                "    VALUES (5555528,16153, 'Do It Clean', '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 254706, 0, 0)");

        //stmt.addBatch("INSERT INTO track(" +
        //        "            id, artist, name, gid, length,year,modpending)" +
        //        "    VALUES (5555529,16153, 'Read it in Books', '675efaf7-f94f-4181-b64c-7d94151f42c0', 0, 0, 0)");

        stmt.addBatch("INSERT INTO albumjoin(" +
                "            id, album, track,sequence, modpending)" +
                "    VALUES (555489, 491240, 5555528, 1, 0)");

        //stmt.addBatch("INSERT INTO albumjoin(" +
        //                "            id, album, track,sequence, modpending)" +
        //                "    VALUES (5555490, 491240, 5555529, 2, 0)");


        stmt.addBatch("INSERT INTO albummeta(" +
                "            id, tracks, discids, puids, firstreleasedate, asin, coverarturl, " +
                "            lastupdate, rating, rating_count, dateadded)" +
                "    VALUES (491240, 2, 0, 0, '1980-07-00', null, null, " +
                "            null, 5, 1, null)");

        stmt.addBatch("INSERT INTO language(" +
                "            id, isocode_3t, isocode_3b, isocode_2, name, frequency)" +
                "    VALUES (120, 'eng', 'eng', 'en', 'English', 2);");

        stmt.addBatch("INSERT INTO script(" +
                "            id, isocode, isonumber, name, frequency)" +
                "    VALUES (28, 'Latn', 215, 'Latn', 4);");

        stmt.addBatch("INSERT INTO country(id, isocode, name)" +
                " VALUES (221, 'AF','Afghanistan')");

        stmt.addBatch("INSERT INTO release(" +
                "            id, album, country, releasedate, modpending, label, catno, barcode,format)" +
                "    VALUES (1, 491240, 221, '1970-01-01', null, null,null,null,null)");

        stmt.addBatch("INSERT INTO labelalias(id, ref, name, timesused, modpending, lastused)" +
                " VALUES (1327, 563, '4AD US', 0, 0, '1970-01-01')");
        stmt.addBatch("INSERT INTO label(id,name, gid, modpending, labelcode,sortname,country, page, resolution, begindate,enddate,type)" +
                "    VALUES (563, '4AD', 'a539bb1e-f2e1-4b45-9db8-8053841e7503',0,null,'4AD',1,2260992,null, null, null,null)");
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
            assertEquals(1, doc.getFields(TrackIndexField.TRACK.getName()).length);
            assertEquals("2f250ed2-6285-40f1-aa2a-14f1c05e9765", doc.getField(TrackIndexField.TRACK_ID.getName()).stringValue());
            assertEquals("Do It Clean", doc.getField(TrackIndexField.TRACK.getName()).stringValue());
            assertEquals("Crocodiles (bonus disc)", doc.getField(TrackIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(TrackIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(TrackIndexField.ARTIST.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(TrackIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("2", doc.getField(TrackIndexField.NUM_TRACKS.getName()).stringValue());
            assertEquals("00000000000001", doc.getField(TrackIndexField.TRACKNUM.getName()).stringValue());
            assertEquals("00000000005gj6", doc.getField(TrackIndexField.DURATION.getName()).stringValue());
            assertEquals("0000000000003j", doc.getField(TrackIndexField.QUANTIZED_DURATION.getName()).stringValue());
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
            assertEquals(0, doc.getFields(TrackIndexField.RELEASE_TYPE.getName()).length);
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
            assertEquals(0, doc.getFields(TrackIndexField.RELEASE_TYPE.getName()).length);
            assertEquals(1, doc.getFields(TrackIndexField.ARTIST_COMMENT.getName()).length);
            assertEquals("a comment", doc.getField(TrackIndexField.ARTIST_COMMENT.getName()).stringValue());

        }
        ir.close();


    }

}