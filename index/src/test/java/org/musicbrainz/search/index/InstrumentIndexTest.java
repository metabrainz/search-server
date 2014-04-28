package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.Instrument;

import java.sql.Statement;

import static org.junit.Assert.assertEquals;

public class InstrumentIndexTest extends AbstractIndexTest {


    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = createIndexWriter(ramDir,InstrumentIndexField.class);
        InstrumentIndex ti = new InstrumentIndex(conn);
        CommonTables ct = new CommonTables(conn, ti.getName());
        ct.createTemporaryTables(false);

        ti.init(writer, false);
        ti.addMetaInformation(writer);
        ti.indexData(writer, 0, Integer.MAX_VALUE);
        ti.destroy();
        writer.close();

    }

    private void addInstrumentOne() throws Exception {

        Statement stmt = conn.createStatement();
        stmt.addBatch("INSERT INTO instrument (comment, id, gid,name,type, description) VALUES ('comment',1, 'aa95182f-df0a-3ad6-8bfb-4b63482cd276', 'Trumpet',1,'Brass instruument')");
        stmt.addBatch("INSERT INTO instrument_type(id, name) VALUES (1, 'Brass')");
        stmt.addBatch("INSERT INTO instrument_alias (id, name, sort_name, instrument, primary_for_locale, locale, type ) VALUES (3,  'tromba','tromba sort', 1, true, 'it',1)");

        stmt.executeBatch();
        stmt.close();
    }



    @Test
    public void testIndexInstrumentName() throws Exception {

        addInstrumentOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, InstrumentIndexField.INSTRUMENT, "trumpet");

        }
        ir.close();
    }

    @Test
    public void testIndexInstrumentGuid() throws Exception {

        addInstrumentOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, InstrumentIndexField.INSTRUMENT_ID, "aa95182f-df0a-3ad6-8bfb-4b63482cd276");

        }
        ir.close();
    }

    @Test
    public void testIndexInstrumentAlias() throws Exception {

        addInstrumentOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, InstrumentIndexField.ALIAS, "sort");
            checkTermX(ir, InstrumentIndexField.ALIAS, "tromba", 1);

        }
        ir.close();
    }


    @Test
    public void testIndexInstrumentComment() throws Exception {

        addInstrumentOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {
            checkTerm(ir, InstrumentIndexField.COMMENT, "comment");
        }
        ir.close();
    }

    @Test
    public void testStoredIndexInstrument() throws Exception {

        addInstrumentOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        IndexReader ir = DirectoryReader.open(ramDir);
        assertEquals(2, ir.numDocs());
        {

            Document doc = ir.document(1);
            Instrument instrument = (Instrument) MMDSerializer.unserialize(doc.get(InstrumentIndexField.INSTRUMENT_STORE.getName()), Instrument.class);
            assertEquals("aa95182f-df0a-3ad6-8bfb-4b63482cd276", instrument.getId());
            assertEquals("Trumpet", instrument.getName());

            assertEquals("comment",instrument.getDisambiguation());
            assertEquals("Brass", instrument.getType());
            assertEquals("tromba",instrument.getAliasList().getAlias().get(0).getContent());
            assertEquals("tromba sort",instrument.getAliasList().getAlias().get(0).getSortName());
            assertEquals("it",instrument.getAliasList().getAlias().get(0).getLocale());

        }
        ir.close();
    }

}