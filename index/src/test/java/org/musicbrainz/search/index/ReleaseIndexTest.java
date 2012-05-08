package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.Test;
import org.musicbrainz.mmd2.ArtistCredit;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReleaseIndexTest extends AbstractIndexTest {

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,ReleaseIndexField.class);
        ReleaseIndex ri = new ReleaseIndex(conn);
        CommonTables ct = new CommonTables(conn, ri.getName());
        ct.createTemporaryTables(false);

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
                "  language, script, date_year, date_month, date_day,comment) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1, 1, 1,'demo')");
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
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2)");

        stmt.addBatch("INSERT INTO release_group_secondary_type_join (release_group, secondary_type) VALUES (491240,1)");
        stmt.addBatch("INSERT INTO release_group_secondary_type_join (release_group, secondary_type) VALUES (491240,2)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "language, script, date_year, date_month, date_day) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 28, 1, 1, 1)");
        stmt.addBatch("INSERT INTO language (id, iso_code_3, iso_code_2t, iso_code_2b, iso_code_2, name, frequency) " +
        	" VALUES (1, null, 'eng', 'eng', 'en', 'English', 1)");
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
        stmt.addBatch("INSERT INTO language (id, iso_code_3, iso_code_2t, iso_code_2b, iso_code_2, name, frequency) " +
        	" VALUES (1, null, 'end', 'eng', 'en', 'English', 1)");
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
    @Test
    public void testIndexReleaseMinPlusTypeAndStatusFields() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals("Crocodiles (bonus disc)", doc.getFieldable(ReleaseIndexField.RELEASE.getName()).stringValue());
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", doc.getFieldable(ReleaseIndexField.RELEASE_ID.getName()).stringValue());
            assertEquals(1, doc.getFieldables(ReleaseIndexField.TYPE.getName()).length);
            assertEquals("Single", doc.getFieldable(ReleaseIndexField.TYPE.getName()).stringValue());
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getFieldable(ReleaseIndexField.RELEASEGROUP_ID.getName()).stringValue());
            assertEquals(1, doc.getFieldables(ReleaseIndexField.STATUS.getName()).length);
            assertEquals("Official", doc.getFieldable(ReleaseIndexField.STATUS.getName()).stringValue());
            assertEquals(1, doc.getFieldables(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.SCRIPT.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseArtist() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
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
    @Test
    public void testIndexReleaseNumDiscs() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).length);
            assertEquals(1, NumericUtils.prefixCodedToInt(doc.getFieldable(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).stringValue()));
            assertEquals(1, doc.getFieldables(ReleaseIndexField.NUM_DISCIDS.getName()).length);
            assertEquals(1, NumericUtils.prefixCodedToInt(doc.getFieldable(ReleaseIndexField.NUM_DISCIDS.getName()).stringValue()));
        }
        ir.close();
    }

    @Test
    public void testIndexReleaseNumMediums() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            TermEnum tr = ir.terms(new Term(ReleaseIndexField.NUM_MEDIUMS.getName(), ""));
            assertEquals(ReleaseIndexField.NUM_MEDIUMS.getName(), tr.term().field());
            assertEquals(1, tr.docFreq());
            assertEquals(1, NumericUtils.prefixCodedToInt(tr.term().text()));
            tr.next();
        }
        ir.close();


    }
    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseSortArtist() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
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
    @Test
    public void testIndexReleaseNoType() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ArtistIndexField.TYPE.getName()).length);
            assertEquals("unknown", doc.getFieldable(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoLanguage() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals(Index.UNKNOWN, doc.getFieldable(ReleaseIndexField.LANGUAGE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoScript() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.SCRIPT.getName()).length);
            assertEquals(Index.UNKNOWN, doc.getFieldable(ReleaseIndexField.SCRIPT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoFormat() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.FORMAT.getName()).length);
            assertEquals("-", doc.getFieldable(ReleaseIndexField.FORMAT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoBarcode() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.BARCODE.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoLabel() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFieldables(ReleaseIndexField.LABEL.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoCatalogNo() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFieldables(ReleaseIndexField.CATALOG_NO.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoCountry() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(Index.UNKNOWN, doc.getFieldable(ReleaseIndexField.COUNTRY.getName()).stringValue());

        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoDate() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(0, doc.getFieldables(ReleaseIndexField.DATE.getName()).length);
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNoStatus() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.STATUS.getName()).length);
            assertEquals(Index.UNKNOWN, doc.getFieldable(ReleaseIndexField.STATUS.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseLanguage() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals("eng", doc.getFieldable(ReleaseIndexField.LANGUAGE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseLanguageNo3() throws Exception {

        addReleaseFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.LANGUAGE.getName()).length);
            assertEquals("end", doc.getFieldable(ReleaseIndexField.LANGUAGE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseASIN() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.AMAZON_ID.getName()).length);
            assertEquals("B00005NTQ7", doc.getFieldable(ReleaseIndexField.AMAZON_ID.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseScript() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.SCRIPT.getName()).length);
            assertEquals("Latn", doc.getFieldable(ReleaseIndexField.SCRIPT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseComment() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.COMMENT.getName()).length);
            assertEquals("demo", doc.getFieldable(ReleaseIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseFormat() throws Exception {
        addReleaseFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.FORMAT.getName()).length);
            assertEquals("Vinyl", doc.getFieldable(ReleaseIndexField.FORMAT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseCountry() throws Exception {
        addReleaseFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals("GB", doc.getFieldable(ReleaseIndexField.COUNTRY.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseDiscIds() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).length);
            assertEquals(2, NumericUtils.prefixCodedToInt(doc.getFieldable(ReleaseIndexField.NUM_DISCIDS_MEDIUM.getName()).stringValue()));
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseNumTracks() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.NUM_TRACKS_MEDIUM.getName()).length);
            assertEquals(10, NumericUtils.prefixCodedToInt(doc.getFieldable(ReleaseIndexField.NUM_TRACKS_MEDIUM.getName()).stringValue()));
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexFullReleaseEvent() throws Exception {

        addReleaseFive();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.BARCODE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.DATE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.CATALOG_NO.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.LABEL.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.FORMAT.getName()).length);

            assertEquals("GB", doc.getFieldable(ReleaseIndexField.COUNTRY.getName()).stringValue());
            assertEquals("5060180310066", doc.getFieldable(ReleaseIndexField.BARCODE.getName()).stringValue());
            assertEquals("1970-01-01", doc.getFieldable(ReleaseIndexField.DATE.getName()).stringValue());
            assertEquals("ECHO1", doc.getFieldable(ReleaseIndexField.CATALOG_NO.getName()).stringValue());
            assertEquals("korova", doc.getFieldable(ReleaseIndexField.LABEL.getName()).stringValue());
            assertEquals("a539bb1e-f2e1-4b45-9db8-8053841e7503", doc.getFieldable(ReleaseIndexField.LABEL_ID.getName()).stringValue());
            assertEquals("Vinyl", doc.getFieldable(ReleaseIndexField.FORMAT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexNoLabelInfo() throws Exception {

        addReleaseTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.COUNTRY.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.BARCODE.getName()).length);
            assertEquals(0, doc.getFieldables(ReleaseIndexField.DATE.getName()).length);
            assertEquals(0, doc.getFieldables(ReleaseIndexField.CATALOG_NO.getName()).length);
            assertEquals(0, doc.getFieldables(ReleaseIndexField.LABEL.getName()).length);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.FORMAT.getName()).length);
        }
        ir.close();
    }

    /**
     * Tets Puid Indexed (not stored)
     * @throws Exception
     */
    @Test
    public void testIndexPuid() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        TermEnum tr = ir.terms(new Term(ReleaseIndexField.PUID.getName(), ""));
        assertEquals(ReleaseIndexField.PUID.getName(), tr.term().field());
        assertEquals(1, tr.docFreq());
        assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e38", tr.term().text());
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseGroupId() throws Exception {
        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASEGROUP_ID.getName()).length);
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getFieldable(ReleaseIndexField.RELEASEGROUP_ID.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseSecondaryType() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFieldables(ReleaseIndexField.RELEASE.getName()).length);

            assertEquals(1, doc.getFieldables(ReleaseGroupIndexField.PRIMARY_TYPE.getName()).length);
            assertEquals("Album", doc.getFieldables(ReleaseGroupIndexField.PRIMARY_TYPE.getName())[0].stringValue());

            //NOte old type field maps secondary type to compilation
            assertEquals(1, doc.getFieldables(ReleaseGroupIndexField.TYPE.getName()).length);
            assertEquals("Compilation", doc.getFieldables(ReleaseGroupIndexField.TYPE.getName())[0].stringValue());

            assertEquals(2, doc.getFieldables(ReleaseIndexField.SECONDARY_TYPE.getName()).length);
            assertEquals("Compilation", doc.getFieldables(ReleaseIndexField.SECONDARY_TYPE.getName())[0].stringValue());
            assertEquals("Soundtrack", doc.getFieldables(ReleaseIndexField.SECONDARY_TYPE.getName())[1].stringValue());

        }
        ir.close();
    }
}