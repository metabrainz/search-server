package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.Relation;
import org.musicbrainz.mmd2.Url;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class UrlIndexTest extends AbstractIndexTest {


    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,UrlIndexField.class);
        UrlIndex ti = new UrlIndex(conn);
        CommonTables ct = new CommonTables(conn, ti.getName());
        ct.createTemporaryTables(false);

        ti.init(writer, false);
        ti.addMetaInformation(writer);
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }

    private void addUrlOne() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO artist (id, name, gid, sort_name, begin_date_year, begin_date_month, type, gender, area,begin_area,ended)" +
                " VALUES (521316, 'Nine Inch Nails', '4302e264-1cf0-4d1f-aca7-2a6f89e34b36', 'Nails, Nine Inch', 1999, 4, 2, 1, 1,38,true)");
        stmt.addBatch("INSERT INTO url (id, gid,url) VALUES (1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'http://en.wikipedia.org/wiki/Nine_Inch_Nails')");
        stmt.addBatch("INSERT INTO l_artist_url (id,link,entity0,entity1) VALUES(1,1,521316,1)");
        stmt.addBatch("INSERT INTO link(id, link_type)VALUES (1, 1)");
        stmt.addBatch("INSERT INTO link_type(id,name) VALUES (1, 'Wikipedia')");
        stmt.executeBatch();
        stmt.close();
    }

    @Test
    public void testIndexUrlGuid() throws Exception {

        addUrlOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, UrlIndexField.URL_ID, "aa95182f-df0a-3ad6-8bfb-4b63482cd276");

        }
        ir.close();
    }

    @Test
    public void testIndexUrlUrl() throws Exception {

        addUrlOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, UrlIndexField.URL, "http://en.wikipedia.org/wiki/Nine_Inch_Nails");

        }
        ir.close();
    }

    @Test
    public void testIndexUrlStore() throws Exception {

        addUrlOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            Document doc = ir.document(1);
            Url url = (Url) MMDSerializer.unserialize(doc.get(UrlIndexField.URL_STORE.getName()), Url.class);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276",url.getId());
            assertEquals("http://en.wikipedia.org/wiki/Nine_Inch_Nails", url.getResource());
            assertEquals("artist", url.getRelationList().get(0).getTargetType());
            Relation relation = url.getRelationList().get(0).getRelation().get(0);
            assertEquals("Wikipedia", relation.getType());
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", relation.getTarget().getId());
            assertEquals("4302e264-1cf0-4d1f-aca7-2a6f89e34b36", relation.getArtist().getId());
            assertEquals("Nine Inch Nails", relation.getArtist().getName());
            assertEquals("Nails, Nine Inch", relation.getArtist().getSortName());


        }
        ir.close();
    }
}