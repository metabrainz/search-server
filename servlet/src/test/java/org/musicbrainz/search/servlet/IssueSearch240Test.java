package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.MetaIndexField;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.mmd1.TrackMmd1XmlWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IssueSearch240Test {


    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;


    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();

        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(RecordingIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);

        {
            MbDocument doc = new MbDocument();
            Recording recording = of.createRecording();
            doc.addField(RecordingIndexField.RECORDING_ID, "7ca7782b-a602-448b-b108-bb881a7be2d6");
            recording.setId("7ca7782b-a602-448b-b108-bb881a7be2d6");
            doc.addField(RecordingIndexField.RECORDING, "I Don\u001at Dance");
            recording.setTitle("I Don\u001at Dance");
            doc.addField(RecordingIndexField.RECORDING_STORE, MMDSerializer.serialize(recording));
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir,
                new MusicBrainzSearcherFactory(ResourceType.RECORDING));
        ss = new RecordingSearch(searcherManager);
        sd = new RecordingDismaxSearch(ss);
    }


    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("rid:7ca7782b-a602-448b-b108-bb881a7be2d6", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("id=\"7ca7782b-a602-448b-b108-bb881a7be2d6\""));
        //IllegalAccessError control char converted toString() apostrophe
        //assertTrue(output.contains("<title>I Don't Dance</title"));
    }

}
