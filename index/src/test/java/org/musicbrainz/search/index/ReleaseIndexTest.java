package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class ReleaseIndexTest extends AbstractIndexTest {


    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        CommonTables ct = new CommonTables(conn);
        ct.createTemporaryTables();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(ReleaseIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        ReleaseIndex ri = new ReleaseIndex(conn,CacheType.TEMPTABLE);
        ri.init(writer, false);
        ri.addMetaInformation(writer);
        ri.indexData(writer, 0, Integer.MAX_VALUE);
        ri.destroy();
        writer.close();
    }


    /**
     * Minimum plus type and status
     *
     * @throws Exception exception
     */
    private void addReleaseOne() throws Exception {
        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 2, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type) " +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 3)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format) VALUES (1, 1, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO puid (id, puid) VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e38')");
        stmt.addBatch("INSERT INTO recording_puid (id, puid, recording) VALUES (1, 1, 2)");
        stmt.addBatch("INSERT INTO tracklist (id, track_count) VALUES (1, 1)");
        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit, length) "
                        + " VALUES (1, 2, 1, 4, 2, 1, 33100)");
        stmt.addBatch("INSERT INTO recording(id, gid, name, artist_credit, length)"
                        + " VALUES (2, '2f250ed2-6285-40f1-aa2a-14f1c05e9765', 1, 1, 33000)");


        stmt.executeBatch();
        stmt.close();
    }

    /**
     * No Release Type
     *
     * @throws Exception exception
     */
    private void addReleaseTwo() throws Exception {
        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id ,name) VALUES (1, 'Echo & The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 1, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1,1, 1)");
        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position) VALUES (1, 1, 491240, 1)");

        stmt.executeBatch();
        stmt.close();
    }

    /**
     * No Release Status
     *
     * @throws Exception exception
     */
    private void addReleaseThree() throws Exception {
        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment,)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 1, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, packaging, country, " +
                "  language, script, date_year, date_month, date_day) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format) VALUES (1, 1, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (2, 1, 3)");
        stmt.addBatch("INSERT INTO tracklist (id, track_count) VALUES (1, 10)");
        stmt.executeBatch();
        stmt.close();
    }

    /**
     * All Basic Fields
     *
     * @throws Exception exception
     */
    private void addReleaseFour() throws Exception {
        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 1, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "language, script, date_year, date_month, date_day) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 28, 1, 1, 1)");
        stmt.addBatch("INSERT INTO language (id, iso_code_3t, iso_code_3b, iso_code_2, name, frequency) " +
        	" VALUES (1, 'eng', 'eng', 'en', 'English', 1)");
        stmt.addBatch("INSERT INTO script (id, iso_code, iso_number, name, frequency) VALUES (28, 'Latn' , 215, 'Latin', 4)");
        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format) VALUES (1, 1, 491240, 1, 7)");

        stmt.executeBatch();
        stmt.close();
    }


    /**
     * All Basic Fields Plus Release Events
     *
     * @throws Exception exception
     */
    private void addReleaseFive() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sort_name, comment)" +
                " VALUES (16153, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 1, 'a comment')");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artist_count, ref_count) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 1)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit) " +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day, barcode) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 221, 1, 28, 1970, 1, 1, '5060180310066')");
        stmt.addBatch("INSERT INTO language (id, iso_code_3t, iso_code_3b, iso_code_2, name, frequency) " +
        	" VALUES (1, 'eng', 'eng', 'en', 'English', 1)");
        stmt.addBatch("INSERT INTO script (id, iso_code, iso_number, name, frequency) VALUES (28, 'Latn' , 215, 'Latin', 4)");
        stmt.addBatch("INSERT INTO country (id, iso_code, name) VALUES (221, 'GB', 'United Kingdom')");

        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, 'korova')");

        stmt.addBatch("INSERT INTO label (id, gid, name, sort_name, country) " +
                " VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 1)");
        stmt.addBatch("INSERT INTO release_label (id, release, label, catalog_number) VALUES (1, 491240, 1, 'ECHO1')");

        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, tracklist, release, position, format) VALUES (1, 1, 491240, 1, 7)");

        stmt.executeBatch();
        stmt.close();
    }


    /**
     * Basic test of all fields
     *
     * @throws Exception exception
     */
    public void testIndexReleaseMinPlusTypeAndStatusFields() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals("Crocodiles (bonus disc)", doc.getField(ReleaseIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getField(ReleaseIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseIndexField.TYPE.getName()).length);
            assertEquals("Single", doc.getField(ReleaseIndexField.TYPE.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseIndexField.STATUS.getName()).length);
            assertEquals("Official", doc.getField(ReleaseIndexField.STATUS.getName()).stringValue());
            assertEquals(0, doc.getFields(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.SCRIPT.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseArtist() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            checkTerm(ir, ReleaseIndexField.ARTIST_NAME, "and");
            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Echo & The Bunnymen", ac.getNameCredit().get(0).getArtist().getName());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNumDiscs() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).length);
            assertEquals(1, NumericUtils.prefixCodedToInt(doc.getField(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).stringValue()));
            assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISCIDS.getName()).length);
            assertEquals(1, NumericUtils.prefixCodedToInt(doc.getField(ReleaseIndexField.NUM_DISCIDS.getName()).stringValue()));
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseSortArtist() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Echo and The Bunnymen", ac.getNameCredit().get(0).getArtist().getSortName());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoType() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.TYPE.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoLanguage() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.LANGUAGE.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */

    public void testIndexReleaseNoScript() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.SCRIPT.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoFormat() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.FORMAT.getName()).length);
            assertEquals("-", doc.getField(ReleaseIndexField.FORMAT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoBarcode() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoLabel() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.LABEL.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoCatalogNo() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoCountry() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoDate() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.DATE.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNoStatus() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.STATUS.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseLanguage() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals("eng", doc.getField(ReleaseIndexField.LANGUAGE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseASIN() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.AMAZON_ID.getName()).length);
            assertEquals("B00005NTQ7", doc.getField(ReleaseIndexField.AMAZON_ID.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */

    public void testIndexReleaseScript() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.SCRIPT.getName()).length);
            assertEquals("Latn", doc.getField(ReleaseIndexField.SCRIPT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseFormat() throws Exception {
        addReleaseFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.FORMAT.getName()).length);
            assertEquals("Vinyl", doc.getField(ReleaseIndexField.FORMAT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseDiscIds() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).length);
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getField(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).stringValue()));
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    public void testIndexReleaseNumTracks() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.NUM_TRACKS_MEDIUM.getName()).length);
            assertEquals(10, NumericUtils.prefixCodedToInt(doc.getField(ReleaseIndexField.NUM_TRACKS_MEDIUM.getName()).stringValue()));
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */

    public void testIndexFullReleaseEvent() throws Exception {

        addReleaseFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.DATE.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.LABEL.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.FORMAT.getName()).length);

            assertEquals("gb", doc.getField(ReleaseIndexField.COUNTRY.getName()).stringValue());
            assertEquals("5060180310066", doc.getField(ReleaseIndexField.BARCODE.getName()).stringValue());
            assertEquals("1970-01-01", doc.getField(ReleaseIndexField.DATE.getName()).stringValue());
            assertEquals("ECHO1", doc.getField(ReleaseIndexField.CATALOG_NO.getName()).stringValue());
            assertEquals("korova", doc.getField(ReleaseIndexField.LABEL.getName()).stringValue());
            assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.getField(ReleaseIndexField.LABEL_ID.getName()).stringValue());
            assertEquals("Vinyl", doc.getField(ReleaseIndexField.FORMAT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */

    public void testIndexNoLabelInfo() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.BARCODE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.DATE.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.CATALOG_NO.getName()).length);
            assertEquals(0, doc.getFields(ReleaseIndexField.LABEL.getName()).length);
            assertEquals(1, doc.getFields(ReleaseIndexField.FORMAT.getName()).length);
        }
        ir.close();
    }

    /**
     * Tets Puid Indexed (not stored)
     * @throws Exception
     */
    public void testIndexPuid() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        TermEnum tr = ir.terms(new Term(ReleaseIndexField.PUID.getName(), ""));
        assertEquals(ReleaseIndexField.PUID.getName(), tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e38", tr.term().text());
    }

}