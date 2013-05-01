package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.Test;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.mmd2.Release;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReleaseIndexTest extends AbstractIndexTest {

	private void createIndex(RAMDirectory ramDir) throws Exception {
		createIndex(ramDir, true);
	}
	
    private void createIndex(RAMDirectory ramDir, boolean useTemporaryTables) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,ReleaseIndexField.class);
        ReleaseIndex ri = new ReleaseIndex(conn);
        CommonTables ct = new CommonTables(conn, ri.getName());
        ct.createTemporaryTables(!useTemporaryTables);
        ri.init(writer, !useTemporaryTables);
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
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (1, 0, 16153, 1, '')");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type) " +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 3)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, " +
                "  language, script, comment) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1,'demo')");
        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format) VALUES (1, 1, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO puid (id, puid) VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e38')");
        stmt.addBatch("INSERT INTO recording_puid (id, puid, recording) VALUES (1, 1, 2)");

        stmt.addBatch("INSERT INTO track (id, recording, medium, position, name, artist_credit, length) "
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
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name,join_phrase) " +
                " VALUES (1, 0, 16153, 1, '')");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging," +
                "  language, script) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1)");

        stmt.addBatch("INSERT INTO release_country (release, country, date_year, date_month, date_day) values (491241, 221, 1970,1,1)");
        stmt.addBatch("INSERT INTO area (id, name) VALUES (221, 'United Kingdom')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (221, 'GB')");

        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, track_count, release, position) VALUES (1, 1, 491240, 1)");

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
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (1, 0, 16153, 1,'')");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, packaging," +
                "  language, script) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1)");

        stmt.addBatch("INSERT INTO release_country (release, country, date_year, date_month, date_day) values (491240, 221, 1970,1,1)");
        stmt.addBatch("INSERT INTO area (id, name) VALUES (221, 'United Kingdom')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (221, 'GB')");

        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format) VALUES (1, 10, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (1, 1, 1)");
        stmt.addBatch("INSERT INTO medium_cdtoc (id, medium, cdtoc) VALUES (2, 1, 3)");
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
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (1, 0, 16153, 1,'')");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 1)");

        stmt.addBatch("INSERT INTO release_group_secondary_type_join (release_group, secondary_type) VALUES (491240,1)");
        stmt.addBatch("INSERT INTO release_group_secondary_type_join (release_group, secondary_type) VALUES (491240,2)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging," +
                "language, script) " +
                "  VALUES (491240,'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 28)");

        stmt.addBatch("INSERT INTO release_country (release, country, date_year, date_month, date_day) values (491240, 221, 1970,1,1)");
        stmt.addBatch("INSERT INTO area (id, name) VALUES (221, 'United Kingdom')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (221, 'GB')");

        stmt.addBatch("INSERT INTO release_country (release, country, date_year, date_month, date_day) values (491240, 222, 1970,1,1)");
        stmt.addBatch("INSERT INTO area (id, name) VALUES (222, 'Sweden')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (222, 'SW')");

        stmt.addBatch("INSERT INTO language (id, iso_code_3, iso_code_2t, iso_code_2b, iso_code_2, name, frequency) " +
        	" VALUES (1, null, 'eng', 'eng', 'en', 'English', 1)");
        stmt.addBatch("INSERT INTO script (id, iso_code, iso_number, name, frequency) VALUES (28, 'Latn' , 215, 'Latin', 4)");
        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format) VALUES (1, 1, 491240, 1, 7)");
        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'punk', 2)");
        stmt.addBatch("INSERT INTO release_tag (release, tag, count) VALUES (491240, 1, 10)");


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
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, join_phrase) " +
                " VALUES (1, 0, 16153, 1, '')");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit) " +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, " +
                "  language, script, barcode) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 28,'5060180310066')");
        stmt.addBatch("INSERT INTO language (id, iso_code_3, iso_code_2t, iso_code_2b, iso_code_2, name, frequency) " +
        	" VALUES (1, null, 'end', 'eng', 'en', 'English', 1)");
        stmt.addBatch("INSERT INTO script (id, iso_code, iso_number, name, frequency) VALUES (28, 'Latn' , 215, 'Latin', 4)");

        stmt.addBatch("INSERT INTO release_country (release, country, date_year, date_month, date_day) values (491240, 221, 1970,1,1)");
        stmt.addBatch("INSERT INTO area (id, name) VALUES (221, 'United Kingdom')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (221, 'GB')");


        stmt.addBatch("INSERT INTO label_name (id, name) VALUES (1, 'korova')");

        stmt.addBatch("INSERT INTO label (id, gid, name, sort_name,area) " +
                " VALUES (1, 'a539bb1e-f2e1-4b45-9db8-8053841e7503', 1, 1, 1)");

        stmt.addBatch("INSERT INTO release_label (id, release, label, catalog_number) VALUES (1, 491240, 1, 'ECHO1')");
        stmt.addBatch("INSERT INTO release_meta (id, amazon_asin) VALUES (491240, 'B00005NTQ7')");
        stmt.addBatch("INSERT INTO medium (id, track_count, release, position, format) VALUES (1, 1, 491240, 1, 7)");

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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.RELEASE, "bonus");
            checkTerm(ir, ReleaseIndexField.RELEASE_ID, "c3b8dbc9-c1ff-4743-9015-8d762819134e");
            checkTerm(ir, ReleaseIndexField.TYPE, "ep");
            checkTerm(ir, ReleaseIndexField.RELEASEGROUP_ID, "efd2ace2-b3b9-305f-8a53-9803595c0e37");
            checkTerm(ir, ReleaseIndexField.STATUS, "official");
            checkTerm(ir, ReleaseIndexField.LANGUAGE, "unknown");
            checkTerm(ir, ReleaseIndexField.SCRIPT, "unknown");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Release release = (Release) MMDSerializer.unserialize(doc.get(ReleaseIndexField.RELEASE_STORE.getName()), Release.class);
            ArtistCredit ac = release.getArtistCredit();
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);

            checkTerm(ir,ReleaseIndexField.NUM_DISCIDS_MEDIUM,1);
            checkTerm(ir,ReleaseIndexField.NUM_DISCIDS,1);
        }
        ir.close();
    }

    @Test
    public void testIndexReleaseNumMediums() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Fields fields = MultiFields.getFields(ir);
            Terms terms = fields.terms(ReleaseIndexField.NUM_MEDIUMS.getName());
            TermsEnum termsEnum = terms.iterator(null);
            termsEnum.next();
            assertEquals(1, NumericUtils.prefixCodedToInt(termsEnum.term()));
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            Release release = (Release) MMDSerializer.unserialize(doc.get(ReleaseIndexField.RELEASE_STORE.getName()), Release.class);
            ArtistCredit ac = release.getArtistCredit();
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.TYPE, "unknown");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.LANGUAGE, "unknown");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.SCRIPT, "unknown");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.FORMAT, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.BARCODE, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.LABEL, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.CATALOG_NO, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.COUNTRY, "unknown");

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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.DATE, "unknown");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.STATUS, "unknown");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.LANGUAGE, "eng");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.LANGUAGE, "end");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.AMAZON_ID, "b00005ntq7");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.SCRIPT, "latn");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir,ReleaseIndexField.COMMENT,"demo");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.FORMAT, "vinyl");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.COUNTRY, "gb");
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseMultipleCountrys() throws Exception {
        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.COUNTRY, "gb");
            checkTermX(ir, ReleaseIndexField.COUNTRY, "sw", 1);
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir,ReleaseIndexField.NUM_DISCIDS_MEDIUM, 2);
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir,ReleaseIndexField.NUM_TRACKS_MEDIUM, 10);
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir,ReleaseIndexField.COUNTRY, "gb");
            checkTerm(ir,ReleaseIndexField.BARCODE, "5060180310066");
            checkTerm(ir,ReleaseIndexField.DATE, "1970-01-01");
            checkTerm(ir,ReleaseIndexField.CATALOG_NO, "echo1");
            checkTerm(ir,ReleaseIndexField.LABEL, "korova");
            checkTerm(ir,ReleaseIndexField.FORMAT, "vinyl");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            checkTerm(ir, ReleaseIndexField.COUNTRY, "unknown");
            checkTerm(ir, ReleaseIndexField.BARCODE, "-");
            checkTerm(ir, ReleaseIndexField.LABEL, "-");
            checkTerm(ir, ReleaseIndexField.FORMAT, "-");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms(ReleaseIndexField.PUID.getName());
        TermsEnum termsEnum = terms.iterator(null);
        termsEnum.next();
        assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e38", termsEnum.term().utf8ToString());
    }

    /**
     * Tets Puid Indexed (not stored)
     * @throws Exception
     */
    @Test
    public void testIndexPuidWithoutTemporaryTables() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir, false);

        IndexReader ir = DirectoryReader.open(ramDir);
        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms(ReleaseIndexField.PUID.getName());
        TermsEnum termsEnum = terms.iterator(null);
        termsEnum.next();
        assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e38", termsEnum.term().utf8ToString());
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testIndexReleaseGroupId() throws Exception {
        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.RELEASEGROUP_ID, "efd2ace2-b3b9-305f-8a53-9803595c0e37");
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

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir,ReleaseIndexField.PRIMARY_TYPE, "album");
            checkTerm(ir, ReleaseIndexField.TYPE, "compilation");
            checkTerm(ir, ReleaseIndexField.SECONDARY_TYPE, "compilation");
            checkTermX(ir, ReleaseIndexField.SECONDARY_TYPE, "interview", 1);

        }
        ir.close();
    }

    @Test
    public void testIndexReleaseWithTag() throws Exception {

        addReleaseFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ReleaseIndexField.TAG, "punk");
        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testStoredRelease1() throws Exception {

        addReleaseOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Release release = (Release) MMDSerializer.unserialize(doc.get(ReleaseIndexField.RELEASE_STORE.getName()), Release.class);
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", release.getId());
            assertEquals("Crocodiles (bonus disc)", release.getTitle());
            assertEquals("B00005NTQ7", release.getAsin());


        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testStoredRelease2() throws Exception {

        addReleaseThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Release release = (Release) MMDSerializer.unserialize(doc.get(ReleaseIndexField.RELEASE_STORE.getName()), Release.class);
            assertEquals("c3b8dbc9-c1ff-4743-9015-8d762819134e", release.getId());
            assertEquals("Crocodiles (bonus disc)", release.getTitle());
            assertEquals("B00005NTQ7", release.getAsin());
            assertEquals("GB", release.getCountry());
            assertEquals("1970-01-01", release.getDate());


        }
        ir.close();
    }
}