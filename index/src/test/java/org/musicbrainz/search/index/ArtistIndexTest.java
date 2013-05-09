package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.Alias;
import org.musicbrainz.mmd2.AliasList;
import org.musicbrainz.mmd2.Artist;
import org.musicbrainz.mmd2.Release;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, begin_date_year, begin_date_month, type, gender, area,ended)" +
            " VALUES (521316, 1, '4302e264-1cf0-4d1f-aca7-2a6f89e34b36', 1, 1999, 4, 2, 1, 1,true)");
        stmt.addBatch("INSERT INTO artist_ipi (artist,ipi) values(521316,'10001')");
        stmt.addBatch("INSERT INTO area (id, name) VALUES (1, 'Afghanistan')");
        stmt.addBatch("INSERT INTO iso_3166_1 (area, code) VALUES (1, 'AF')");

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

        stmt.addBatch("INSERT INTO artist_alias (id, sort_name, type, artist, name, begin_date_year,begin_date_month,begin_date_day,primary_for_locale ) " +
                " VALUES (1, 6, 1, 16153, 2,1978,05,01, false)");

        stmt.addBatch("INSERT INTO artist_alias (id, artist, sort_name, name, primary_for_locale, type, end_date_year,end_date_month ) VALUES (2, 16153, 6, 3, null, null, 1984,09)");
        stmt.addBatch("INSERT INTO artist_alias (id, artist, sort_name, name, primary_for_locale, locale, type ) VALUES (3, 16153, 6, 4, true, 'en',3)");

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
            checkTerm(ir, ArtistIndexField.ARTIST, "farming");
            checkTermX(ir, ArtistIndexField.ARTIST, "incident",1);
            checkTerm(ir, ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            checkTerm(ir, ArtistIndexField.SORTNAME, "farming");
            checkTermX(ir, ArtistIndexField.SORTNAME, "incident", 1);
            checkTerm(ir, ArtistIndexField.BEGIN, "1999-04");
            checkTerm(ir, ArtistIndexField.TYPE, "group");
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
            checkTerm(ir, ArtistIndexField.TYPE, "group");
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
            checkTerm(ir, ArtistIndexField.COMMENT, "a");
            checkTermX(ir, ArtistIndexField.COMMENT, "comment", 1);

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
                checkTerm(ir, ArtistIndexField.COUNTRY, "af");
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
                    checkTerm(ir, ArtistIndexField.IPI, "10001");
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
                checkTerm(ir, ArtistIndexField.COUNTRY, "unknown");
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
            checkTerm(ir, ArtistIndexField.GENDER, "male");
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
            checkTerm(ir, ArtistIndexField.GENDER, "unknown");
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
    public void testIndexArtistWithAliasAndAliasSortname() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, ArtistIndexField.ALIAS, "and");
            checkTermX(ir, ArtistIndexField.ALIAS, "bunnyman", 1);
            checkTermX(ir, ArtistIndexField.ALIAS, "bunnymen", 2);
            checkTermX(ir, ArtistIndexField.ALIAS, "buunymen", 3); //From alias sortname
            checkTermX(ir, ArtistIndexField.ALIAS, "echo", 4);
            checkTermX(ir, ArtistIndexField.ALIAS, "orchestra", 5);

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
            checkTerm(ir, ArtistIndexField.BEGIN, "1978");
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
            checkTerm(ir, ArtistIndexField.ENDED, "true");
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
            checkTerm(ir, ArtistIndexField.ENDED, "false");
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
            checkTerm(ir, ArtistIndexField.END, "1995");
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
            checkTerm(ir, ArtistIndexField.TYPE, "person");
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
            checkTerm(ir, ArtistIndexField.COMMENT, "-");
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
            checkTerm(ir, ArtistIndexField.ARTIST, "lynch");
            checkTermX(ir, ArtistIndexField.ARTIST, "siobhan", 1);

            checkTerm(ir, ArtistIndexField.SORTNAME, "lynch");
            checkTermX(ir, ArtistIndexField.SORTNAME, "siobhan",1);

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
            checkTerm(ir, ArtistIndexField.TAG, "goth");
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
            checkTerm(ir, ArtistIndexField.ALIAS, "lynch");
            checkTermX(ir, ArtistIndexField.ALIAS, "s", 1);


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


    /**
     * @throws Exception exception
     */
    @Test
    public void testStoredArtist1() throws Exception {

        addArtistOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Artist artist = (Artist) MMDSerializer.unserialize(doc.get(ArtistIndexField.ARTIST_STORE.getName()), Artist.class);
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", artist.getId());
            assertEquals("Farming Incident", artist.getName());
            assertEquals("AF", artist.getCountry());


        }
        ir.close();
    }

    /**
     * @throws Exception exception
     */
    @Test
    public void testStoredArtistWithAliases() throws Exception {

        addArtistTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Artist artist = (Artist) MMDSerializer.unserialize(doc.get(ArtistIndexField.ARTIST_STORE.getName()), Artist.class);
            assertEquals("ccd4879c-5e88-4385-b131-bf65296bf245", artist.getId());
            assertEquals("Echo & The Bunnymen", artist.getName());
            assertEquals(null, artist.getCountry());
            AliasList aliasList = artist.getAliasList();
            assertNotNull(aliasList);
            assertEquals(3, aliasList.getAlias().size());
            Alias alias = aliasList.getAlias().get(0);
            assertEquals("Echo & The Bunnyman", alias.getContent());
            assertEquals("Buunymen, Echo And The", alias.getSortName());
            assertEquals(null, alias.getPrimary());
            assertEquals(null, alias.getLocale());
            assertEquals(null, alias.getType());
            assertEquals(null, alias.getBeginDate());
            assertEquals("1984-09", alias.getEndDate());

            alias = aliasList.getAlias().get(1);
            assertEquals("Echo And The Bunnymen", alias.getContent());
            assertEquals("Buunymen, Echo And The", alias.getSortName());
            assertEquals("primary", alias.getPrimary());
            assertEquals("en", alias.getLocale());
            assertEquals("Search hint", alias.getType());
            assertEquals(null, alias.getBeginDate());
            assertEquals(null, alias.getEndDate());

            alias = aliasList.getAlias().get(2);
            assertEquals("Echo and The Bunnymen", alias.getContent());
            assertEquals("Buunymen, Echo And The", alias.getSortName());
            assertEquals(null, alias.getPrimary());
            assertEquals(null, alias.getLocale());
            assertEquals("Artist name", alias.getType());
            assertEquals("1978-05-01", alias.getBeginDate());
            assertEquals(null, alias.getEndDate());

        }
        ir.close();
    }
}