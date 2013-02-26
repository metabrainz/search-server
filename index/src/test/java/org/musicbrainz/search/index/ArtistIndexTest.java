package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class ArtistIndexTest extends AbstractIndexTest {




    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,ArtistIndexField.class);
        ArtistIndex ai = new ArtistIndex(conn);
        CommonTables ct = new CommonTables(conn, ai.getName());
        ct.createTemporaryTables(false);
        ai.init(writer, false);
        ai.addMetaInformation(writer);
        ai.indexData(writer, 0, Integer.MAX_VALUE);
        ai.destroy();
        writer.close();

    }

    private void addArtistOne() throws Exception {

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Farming Incident')");
        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, begin_date_year, begin_date_month, type, gender, country,ended)" +
            " VALUES (521316, 1, '4302e264-1cf0-4d1f-aca7-2a6f89e34b36', 1, 1999, 4, 2, 1, 1,true)");
        stmt.addBatch("INSERT INTO artist_ipi (artist,ipi) values(521316,'10001')");
        stmt.addBatch("INSERT INTO country (id, iso_code, name) VALUES (1, 'AF', 'Afghanistan')");

        stmt.executeBatch();
        stmt.close();
    }


    private void addArtistTwo() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Echo & The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Echo and The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Echo & The Bunnyman')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Echo And The Bunnymen')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (5, 'Bunnymen Orchestra')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (6, 'Buunymen, Echo And The')");

        stmt.addBatch("INSERT INTO artist_alias (id, sort_name, type, artist, name, begin_date_year,begin_date_month,begin_date_day) " +
                " VALUES (1, 6, 1, 16153, 2,1978,05,01)");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (2, 16153, 3)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, name) VALUES (3, 16153, 4)");

        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 5)");

        //This is same as alias, so should be ignored
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name) " +
                " VALUES (1, 0, 16153, 3)");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, comment, begin_date_year, end_date_year, type)" +
                   " VALUES (16153, 1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 'a comment', 1978, 1995, 2)");


        stmt.executeBatch();
        stmt.close();
    }

    private void addArtistThree() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Siobhan Lynch')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Lynch, Siobhan')");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name,type)" +
            " VALUES (76834, 1, 'ae8707b6-684c-4d4a-95c5-d117970a6dfe', 2, 1)");


        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Goth', 2)");
        stmt.addBatch("INSERT INTO artist_tag (artist, tag, count) VALUES (76834, 1, 10)");
        stmt.executeBatch();
        stmt.close();
    }

    private void addArtistFour() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Siobhan Lynch')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Lynch, Siobhan')");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, type)" +
            " VALUES (76834, 1, 'ae8707b6-684c-4d4a-95c5-d117970a6dfe', 2, 1)");

        stmt.addBatch("INSERT INTO tag (id, name, ref_count) VALUES (1, 'Goth', 2)");
        stmt.addBatch("INSERT INTO artist_tag (artist, tag, count) VALUES (76834, 1, 10)");
        stmt.executeBatch();
        stmt.close();
    }

    /**
     * Checks fields are indexed correctly for artist with no alias
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexArtistWithNoAlias() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(ArtistIndexField.ALIAS.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST_ID.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
            assertEquals(0, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Farming Incident", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", doc.getField(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Farming Incident", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("1999-04", doc.getField(ArtistIndexField.BEGIN.getName()).stringValue());
            assertEquals("Group", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();

    }

    @Test
    public void testIndexArtistWithType() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);
            assertEquals("Group", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    @Test
    public void testIndexArtistWithComment() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals("a comment", doc.getField(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    @Test
    public void testIndexArtistWithCountry() throws Exception {

            addArtistOne();
            RAMDirectory ramDir = new RAMDirectory();
            createIndex(ramDir);

            IndexReader ir = DirectoryReader.open(ramDir);
            assertEquals(2, ir.numDocs());
            {
                Document doc = ir.document(1);
                assertEquals(1, doc.getFields(ArtistIndexField.COUNTRY.getName()).length);
                assertEquals("af", doc.getField(ArtistIndexField.COUNTRY.getName()).stringValue());
            }
            ir.close();
        }


    @Test
    public void testIndexArtistWithIPI() throws Exception {

                addArtistOne();
                RAMDirectory ramDir = new RAMDirectory();
                createIndex(ramDir);

                IndexReader ir = DirectoryReader.open(ramDir);
                assertEquals(2, ir.numDocs());
                {
                    Document doc = ir.document(1);
                    assertEquals(1, doc.getFields(ArtistIndexField.IPI.getName()).length);
                    assertEquals("10001", doc.getField(ArtistIndexField.IPI.getName()).stringValue());
                }
                ir.close();
            }

    @Test
    public void testIndexArtistWithNoCountry() throws Exception {

            addArtistTwo();
            RAMDirectory ramDir = new RAMDirectory();
            createIndex(ramDir);

            IndexReader ir = DirectoryReader.open(ramDir);
            assertEquals(2, ir.numDocs());
            {
                Document doc = ir.document(1);
                assertEquals(1, doc.getFields(ArtistIndexField.COUNTRY.getName()).length);
                assertEquals("unknown", doc.getField(ArtistIndexField.COUNTRY.getName()).stringValue());
            }
            ir.close();
        }

    @Test
    public void testIndexArtistWithGender() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.GENDER.getName()).length);
            assertEquals("male", doc.getField(ArtistIndexField.GENDER.getName()).stringValue());
        }
        ir.close();
    }

    @Test
    public void testIndexArtistPersonWithUnknownGender() throws Exception {

        addArtistFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.GENDER.getName()).length);
            assertEquals("unknown", doc.getField(ArtistIndexField.GENDER.getName()).stringValue());
        }
        ir.close();
    }

    @Test
    public void testIndexGroupWithNoGender() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(ArtistIndexField.GENDER.getName()).length);
        }
        ir.close();
    }



    /**
     * Checks fields are indexed correctly for artist with alias and artistCredit (the aliases are not stored)
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexArtistWithAlias() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(4, doc.getFields(ArtistIndexField.ALIAS.getName()).length); //aliases are searchable but not stored
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST_ID.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Echo & The Bunnymen", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", doc.getField(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Echo & The Bunnymen", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("1978", doc.getField(ArtistIndexField.BEGIN.getName()).stringValue());
            assertEquals("Group", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
            assertEquals("a comment", doc.getField(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception exception
     */
    @Test
    public void testBeginDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
            assertEquals("1978", doc.getField(ArtistIndexField.BEGIN.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception exception
     */
    @Test
    public void testEnded() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.ENDED.getName()).length);
            assertEquals("true", doc.getField(ArtistIndexField.ENDED.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception exception
     */
    @Test
    public void testNotEnded() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.ENDED.getName()).length);
            assertEquals("false", doc.getField(ArtistIndexField.ENDED.getName()).stringValue());
        }
        ir.close();
    }



    /**
     * Checks zeroes are removed from date
     *
     * @throws Exception exception
     */
    @Test
    public void testEndDate() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals("1995", doc.getField(ArtistIndexField.END.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks record with type = null is set to unknown
     *
     * @throws Exception  exception
     */
    @Test
    public void testIndexArtistWithNoType() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);
            assertEquals("Person", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks record with comment = null is not indexed
     *
     * @throws Exception  exception
     */
    @Test
    public void testIndexArtistWithNoComment() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals("-", doc.getField(ArtistIndexField.COMMENT.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks record with begin date = null is not indexed
     *
     * @throws Exception  exception
     */
    @Test
    public void testIndexArtistWithNoBeginDate() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(ArtistIndexField.BEGIN.getName()).length);
        }
        ir.close();
    }


    /**
     * Checks record with end date = null is not indexed
     *
     * @throws Exception  exception
     */
    @Test
    public void testIndexArtistWithNoEndDate() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(0, doc.getFields(ArtistIndexField.END.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.COMMENT.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TYPE.getName()).length);

            assertEquals("Siobhan Lynch", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("ae8707b6-684c-4d4a-95c5-d117970a6dfe", doc.getField(ArtistIndexField.ARTIST_ID.getName()).stringValue());
            assertEquals("Lynch, Siobhan", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
            assertEquals("Person", doc.getField(ArtistIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception  exception
     */
    @Test
    public void testIndexArtistWithDifferentSortName() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.SORTNAME.getName()).length);
            assertEquals("Siobhan Lynch", doc.getField(ArtistIndexField.ARTIST.getName()).stringValue());
            assertEquals("Lynch, Siobhan", doc.getField(ArtistIndexField.SORTNAME.getName()).stringValue());
        }
        ir.close();
    }


    /**
     * Checks fields with different sort name to name is indexed correctly
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexArtistWithTag() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.TAG.getName()).length);
            assertEquals("Goth", doc.getField(ArtistIndexField.TAG.getName()).stringValue());
            assertEquals(1, doc.getFields(ArtistIndexField.TAGCOUNT.getName()).length);
            assertEquals("10", doc.getField(ArtistIndexField.TAGCOUNT.getName()).stringValue());
        }
        ir.close();
    }

    /**
     * Checks adding artist with initials as an alias
     *
     * @throws Exception exception
     */
    @Test
    public void testIndexArtistAlias() throws Exception {

        addArtistThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(1, doc.getFields(ArtistIndexField.ALIAS.getName()).length);
            assertEquals("S Lynch", doc.getField(ArtistIndexField.ALIAS.getName()).stringValue());

        }
        ir.close();
    }

    /**
     * Checks dont add group  as an artist with initials as an alias
     *
     * @throws Exception exception
     */
    @Test
    public void testDontIndexArtistAliasGroup() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            assertEquals(1, doc.getFields(ArtistIndexField.ARTIST.getName()).length);
            assertEquals(0, doc.getFields(ArtistIndexField.ALIAS.getName()).length);
        }
        ir.close();
    }

    @Test
    public void testGetTypeByDbId () throws Exception {        
        assertEquals(ArtistType.PERSON,ArtistType.getBySearchId(1));
    }

    @Test
    public void testMetaInformation() throws Exception {
    	
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals("42459", doc.getField(MetaIndexField.REPLICATION_SEQUENCE.getName()).stringValue());
            assertEquals("12", doc.getField(MetaIndexField.SCHEMA_SEQUENCE.getName()).stringValue());
        }
    }
}