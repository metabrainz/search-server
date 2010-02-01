/*
 * MusicBrainz Search Server
 * Copyright (C) 2009 Paul Taylor

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.musicbrainz.search.index;

import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.sql.Connection;
import java.sql.Statement;


public class ReleaseGroupIndexTest extends AbstractIndexTest {

    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(ReleaseGroupIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
        ReleaseGroupIndex li = new ReleaseGroupIndex(createConnection());
        li.init(writer);
        li.indexData(writer, 0, Integer.MAX_VALUE);
        li.destroy();
        writer.close();
    }

    /**
     * @throws Exception
     */
    private void addReleaseGroupOne() throws Exception {
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

        stmt.addBatch("INSERT INTO artist (id,name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 2, 'a comment', 1978, null, 1995, 2)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                " VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2, null)");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day, barcode, comment) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1, 1, 1, null, null)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * No Type
     *
     * @throws Exception
     */
    private void addReleaseGroupTwo() throws Exception {
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

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (16153, 1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 'a comment', 1978, null, 1995, 2)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist,name, joinphrase) " +
                " VALUES (1, 0, 16153, 1, null)");

        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (bonus disc)')");

        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, null, null);");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day,barcode, comment) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1, 1, 1, null, null)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Two Albums
     *
     * @throws Exception
     */
    private void addReleaseGroupThree() throws Exception {
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

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (16153,1, 'ccd4879c-5e88-4385-b131-bf65296bf245', 1, 'a comment', 1978, null, 1995, 2)");
        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 1, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist,name, joinphrase) " +
                " VALUES (1, 0, 16153, 1, null)");
        
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Crocodiles')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (2, 'Crocodiles (Bonus disc)')");
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (3, 'Crocodiles (Special disc)')");

        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                " VALUES (491240, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2, null);");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day, barcode, comment) " +
                " VALUES (491240, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 2, 1, 491240, 1, 1, 1, 1, 1, 1, 1, 1, null, null)");

        stmt.addBatch("INSERT INTO release (id, gid, name, artist_credit, release_group, status, packaging,country, " +
                "  language, script, date_year, date_month, date_day, barcode, comment) " +
                " VALUES (491241, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 3, 1, 491240, 1, 1, 1, 1, 1, 1, 1, 1, null, null)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * @throws Exception
     */
    private void addReleaseGroupFour() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'Erich Kunzel')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Kunzel, Eric')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'The Cincinnati Pops Orchestra')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Cincinnati Pops Orchestra, The')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (5, 'Cincinnati Pops')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (6, 'Erich Kunzel and Kunzel, Eric')");

        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (1, 1, '99845d0c-f239-4051-a6b1-4b5e9f7ede0b', 2, 'a comment', 1978, null, 1995, 2)");
        stmt.addBatch("INSERT INTO artist (id, name, gid, sortname, comment, begindate_year, begindate_month, enddate_year, type)" +
                " VALUES (2, 3, 'd8fbd94c-cd06-4e8b-a559-761ad969d07e', 4, 'a comment', 1978, null, 1995, 2)");

        stmt.addBatch("INSERT INTO artist_credit (id, name, artistcount, refcount) VALUES (1, 6, 1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase)" +
                " VALUES (1, 0, 1, 1, ' and ')");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase)" +
                " VALUES (1, 1, 2, 5, null)");
        
        stmt.addBatch("INSERT INTO release_name (id, name) VALUES (1, 'Epics')");
        stmt.addBatch("INSERT INTO release_group (id, gid, name, artist_credit, type, comment)" +
                "    VALUES (1, 'efd2ace2-b3b9-305f-8a53-9803595c0e37', 1, 1, 2, null)");

        stmt.addBatch("INSERT INTO release(id, gid, name, artist_credit, release_group, status, packaging, country, " +
                "  language, script, date_year, date_month, date_day, barcode, comment) " +
                " VALUES (1, 'c3b8dbc9-c1ff-4743-9015-8d762819134e', 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, null, null)");

        stmt.addBatch("INSERT INTO tag (id, name, refcount) VALUES (1, 'punk', 2)");
        stmt.addBatch("INSERT INTO release_group_tag (release_group, tag, count) VALUES (1, 1, 10)");

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    /**
     * Basic test of all fields
     *
     * @throws Exception
     */
    public void testIndexReleaseGroupFields() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.RELEASEGROUP.getName()).length);
            assertEquals("Crocodiles", doc.getField(ReleaseGroupIndexField.RELEASEGROUP.getName()).stringValue());
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getField(ReleaseGroupIndexField.RELEASEGROUP_ID.getName()).stringValue());
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.RELEASE.getName()).length);
            assertEquals("Crocodiles (bonus disc)", doc.getField(ReleaseGroupIndexField.RELEASE.getName()).stringValue());
            checkTerm(ir,ReleaseGroupIndexField.ARTIST_NAME,"and");
            checkTerm(ir,ReleaseGroupIndexField.ARTIST_ID,"ccd4879c-5e88-4385-b131-bf65296bf245");


        }
        ir.close();

    }




    public void testIndexReleaseGroupWithType() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.TYPE.getName()).length);
            assertEquals("album", doc.getField(ReleaseGroupIndexField.TYPE.getName()).stringValue());
        }
        ir.close();


    }

    public void testIndexReleaseGroupSortname() throws Exception {

        addReleaseGroupOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);

            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseGroupIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Echo and The Bunnymen",ac.getNameCredit().get(0).getArtist().getSortName());
        }
        ir.close();


    }


    /**
     * Checks record with type = null is set to unknown
     *
     * @throws Exception
     */
    /*
    //TODO what do we want to do in this situation, in NGS now returns no record
    public void testIndexReleaseGroupWithNoType() throws Exception {

        addReleaseGroupTwo();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.TYPE.getName()).length);
            assertEquals("nat", doc.getField(ReleaseGroupIndexField.TYPE.getName()).stringValue());
        }
        ir.close();
    }
    */

    /**
     * Checks record with multiple releases
     *
     * @throws Exception
     */
    public void testIndexReleaseGroupWithMultipleReleases() throws Exception {

        addReleaseGroupThree();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(2, doc.getFields(ReleaseGroupIndexField.RELEASE.getName()).length);
            String val1 = doc.getFields(ReleaseGroupIndexField.RELEASE.getName())[0].stringValue();
            String val2 = doc.getFields(ReleaseGroupIndexField.RELEASE.getName())[1].stringValue();

            assertTrue("Crocodiles (Bonus disc)".equals(val1) || "Crocodiles (Bonus disc)".equals(val2));
            assertTrue("Crocodiles (Special disc)".equals(val1) || "Crocodiles (Special disc)".equals(val2));
        }
        ir.close();
    }


    public void testIndexReleaseGroupMultipleArtists() throws Exception {

        addReleaseGroupFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);

            TermEnum tr = ir.terms(new Term(ReleaseGroupIndexField.ARTIST_NAME.getName(), ""));
            assertEquals(ReleaseGroupIndexField.ARTIST_NAME.getName(), tr.term().field());
            assertEquals(1, tr.docFreq());
            assertEquals("cincinnati", tr.term().text());
            tr.next();
            assertEquals("erich", tr.term().text());
            tr.next();
            assertEquals("kunzel", tr.term().text());
            tr.next();
            assertEquals("orchestra", tr.term().text());
            tr.next();
            assertEquals("pops", tr.term().text());
            tr.next();
            assertEquals("the", tr.term().text());
            
            tr = ir.terms(new Term(ReleaseGroupIndexField.ARTIST_ID.getName(), ""));
            assertEquals(ReleaseGroupIndexField.ARTIST_ID.getName(), tr.term().field());
            assertEquals(1, tr.docFreq());
            assertEquals("99845d0c-f239-4051-a6b1-4b5e9f7ede0b", tr.term().text());
            tr.next();
            assertEquals("d8fbd94c-cd06-4e8b-a559-761ad969d07e", tr.term().text());
            tr.next();

            tr = ir.terms(new Term(ReleaseGroupIndexField.ARTIST_NAMECREDIT.getName(), ""));
            assertEquals(ReleaseGroupIndexField.ARTIST_NAMECREDIT.getName(), tr.term().field());
            assertEquals(1, tr.docFreq());
            assertEquals("cincinnati", tr.term().text());
            tr.next();
            assertEquals("erich", tr.term().text());
            tr.next();
            assertEquals("kunzel", tr.term().text());
            tr.next();
            assertEquals("pops", tr.term().text());

            assertEquals("Epics", doc.getFields(ReleaseGroupIndexField.RELEASEGROUP.getName())[0].stringValue());
            assertEquals("efd2ace2-b3b9-305f-8a53-9803595c0e37", doc.getFields(ReleaseGroupIndexField.RELEASEGROUP_ID.getName())[0].stringValue());

            ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseGroupIndexField.ARTIST_CREDIT.getName()));
            assertNotNull(ac);
            assertEquals("Erich Kunzel", ac.getNameCredit().get(0).getArtist().getName());
            assertEquals("Cincinnati Pops", ac.getNameCredit().get(1).getName());
            assertEquals("The Cincinnati Pops Orchestra", ac.getNameCredit().get(1).getArtist().getName());
        }
        ir.close();


    }


     public void testIndexReleaseGroupWithTag() throws Exception {

        addReleaseGroupFour();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        {
            Document doc = ir.document(0);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.RELEASEGROUP.getName()).length);
            assertEquals(1, doc.getFields(ReleaseGroupIndexField.TAG.getName()).length);
            assertEquals("punk", doc.getField(ReleaseGroupIndexField.TAG.getName()).stringValue());
        }
        ir.close();
    }

}