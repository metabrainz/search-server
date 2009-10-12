package org.musicbrainz.search.index;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.analysis.StandardUnaccentAnalyzer;
import org.musicbrainz.search.index.RecordingIndexField;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;


public class RecordingIndexTest extends AbstractIndexTest {

    private static String RECORDING_ONE_GID = "27ae9c34-36c7-43f6-8f7d-fe775b151bc1";
    private static String ENTITY_TYPE = "recording";
    
    public void setUp() throws Exception {
        super.setup();
    }

    private void createIndex(RAMDirectory ramDir) throws Exception {
        IndexWriter writer = new IndexWriter(ramDir, new StandardUnaccentAnalyzer(), true, IndexWriter.MaxFieldLength.LIMITED);
        RecordingIndex ri = new RecordingIndex(createConnection());
        ri.init();
        ri.indexData(writer, 0, Integer.MAX_VALUE);
        ri.destroy();
        writer.close();
    }

    /**
     * A full featured case, where the artist name is not used in any artist_credit
     *
     * @throws Exception
     */
    private void addRecordingOne() throws Exception {
        Connection conn = createConnection();
        conn.setAutoCommit(true);

        Statement stmt = conn.createStatement();

        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (1, 'The Beatles')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (2, 'Beatles, The')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (3, 'Beatles')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (4, 'Beetles')");
        stmt.addBatch("INSERT INTO artist_name (id, name) VALUES (9, 'Mister X')");

        stmt.addBatch("INSERT INTO artist (id, gid, name, sortname, comment) " +
                "VALUES (1, 'b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d', 1, 2, 'a comment')"
        );
        stmt.addBatch("INSERT INTO artist (id, gid, name, sortname) " +
                "VALUES (9, '603f0130-342b-4f35-8050-928ab01d7eaf', 9, 9)"
        );

        // Artist "The Beatles" with credit name "Beatles"
        stmt.addBatch("INSERT INTO artist_credit (id, artistcount) VALUES (1, 1)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                "VALUES (1, 1, 1, 3, null)"
        );

        // Imaginary "Mister X presents Beetles" artist credit (yes probably a very cheap compilation :)
        stmt.addBatch("INSERT INTO artist_credit (id, artistcount) VALUES (2, 2)");
        stmt.addBatch("INSERT INTO artist_credit_name (artist_credit, position, artist, name, joinphrase) " +
                "VALUES (2, 1, 9, 9, ' presents '), " +
                "       (2, 2, 1, 4, null)"
        );
        
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (1, 'A Day in the Life')");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (2, 'A Day in the Life (1967-01-19)')");
        stmt.addBatch("INSERT INTO track_name (id, name) VALUES (3, 'A Day in the Life (original)')");

        stmt.addBatch("INSERT INTO recording (id, gid, name, artist_credit, length, comment) " + 
                "VALUES (1, '" + RECORDING_ONE_GID + "', 1, 1, 308320, 'a very useful comment')"
        );

        stmt.addBatch("INSERT INTO track (id, recording, tracklist, position, name, artist_credit) " + 
                "VALUES (1, 1, 0, 0, 2, 1), " +
                "       (2, 1, 0, 0, 3, 2) "
        );
        
        stmt.executeBatch();
        stmt.close();
        conn.close();
    }

    public void testIndexRecordingWithComment() throws Exception {

        addRecordingOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = RecordingIndexField.COMMENT + ":\"a very useful comment\"";
        List<Document> results = search(ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_GID.getName()).length);
            assertEquals(RECORDING_ONE_GID, doc.getField(RecordingIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(RecordingIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByTrackArtistCreditName() throws Exception {

        addRecordingOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = RecordingIndexField.ARTIST + ":\"Mister X\"";
        List<Document> results = search(ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_GID.getName()).length);
            assertEquals(RECORDING_ONE_GID, doc.getField(RecordingIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(RecordingIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByTrackArtistCreditNameJoinPhrase() throws Exception {

        addRecordingOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = RecordingIndexField.ARTIST + ":\"presents\"";
        List<Document> results = search(ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_GID.getName()).length);
            assertEquals(RECORDING_ONE_GID, doc.getField(RecordingIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(RecordingIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }

    public void testSearchByTrackFullArtistCreditName() throws Exception {

        addRecordingOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = RecordingIndexField.ARTIST + ":\"Mister X presents Beetles\"";
        List<Document> results = search(ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_GID.getName()).length);
            assertEquals(RECORDING_ONE_GID, doc.getField(RecordingIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(RecordingIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByTrackName() throws Exception {

        addRecordingOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = RecordingIndexField.TRACK + ":\"A Day in the Life (original)\"";
        List<Document> results = search(ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_GID.getName()).length);
            assertEquals(RECORDING_ONE_GID, doc.getField(RecordingIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(RecordingIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }
    
    public void testSearchByRecodingArtistName() throws Exception {

        addRecordingOne();
        RAMDirectory ramDir = new RAMDirectory();
        createIndex(ramDir);

        // Check if something has been indexed
        IndexReader ir = IndexReader.open(ramDir, true);
        assertEquals(1, ir.numDocs());
        ir.close();     

        // Try to search using this piece of information
        String query = RecordingIndexField.ARTIST + ":\"The Beatles\"";
        List<Document> results = search(ramDir, query, 0, 10);
        assertEquals(1, results.size());
        {
            Document doc = results.get(0);
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_GID.getName()).length);
            assertEquals(RECORDING_ONE_GID, doc.getField(RecordingIndexField.ENTITY_GID.getName()).stringValue());
            assertEquals(1, doc.getFields(RecordingIndexField.ENTITY_TYPE.getName()).length);
            assertEquals(ENTITY_TYPE, doc.getField(RecordingIndexField.ENTITY_TYPE.getName()).stringValue());
        }
        
    }

}