package org.musicbrainz.search.servlet;

import junit.framework.TestCase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.RAMDirectory;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.RecordingIndexField;

public class IssueSearch167RecordingTest extends TestCase {

    private SearchServer sd;

    public IssueSearch167RecordingTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION,analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);


        //Recording 1
        {
            MbDocument doc = new MbDocument();
            doc.addField(RecordingIndexField.RECORDING_ID, "11111111-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.RECORDING, "Luve");
            doc.addField(RecordingIndexField.COMMENT, "the real one");
            doc.addField(RecordingIndexField.COUNTRY, "AF");
            doc.addField(RecordingIndexField.TAG, "thrash");
            doc.addField(RecordingIndexField.TAGCOUNT, "5");
            doc.addField(RecordingIndexField.TAG, "güth");
            doc.addField(RecordingIndexField.TAGCOUNT, "11");
            writer.addDocument(doc.getLuceneDocument());
        }

        //Recording 2
        {
            MbDocument doc = new MbDocument();
            doc.addField(RecordingIndexField.RECORDING_ID, "22222222-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.RECORDING, "Luvely");
            doc.addField(RecordingIndexField.COMMENT, "the real one");
            doc.addField(RecordingIndexField.COUNTRY, "AF");
            doc.addField(RecordingIndexField.TAG, "thrash");
            doc.addField(RecordingIndexField.TAGCOUNT, "5");
            doc.addField(RecordingIndexField.TAG, "güth");
            doc.addField(RecordingIndexField.TAGCOUNT, "11");
            writer.addDocument(doc.getLuceneDocument());
        }

        //Recording 3
        {
            MbDocument doc = new MbDocument();
            doc.addField(RecordingIndexField.RECORDING_ID, "33333333-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.RECORDING, "Blood");
            doc.addField(RecordingIndexField.COMMENT, "the real one");
            doc.addField(RecordingIndexField.COUNTRY, "AF");
            doc.addField(RecordingIndexField.TAG, "thrash");
            doc.addField(RecordingIndexField.TAGCOUNT, "5");
            doc.addField(RecordingIndexField.TAG, "güth");
            doc.addField(RecordingIndexField.TAGCOUNT, "11");
            writer.addDocument(doc.getLuceneDocument());
        }

        //Recording 4
        {
            MbDocument doc = new MbDocument();
            doc.addField(RecordingIndexField.RECORDING_ID, "44444444-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.RECORDING, "Scope");
            doc.addField(RecordingIndexField.COMMENT, "the real one");
            doc.addField(RecordingIndexField.COUNTRY, "AF");
            doc.addField(RecordingIndexField.TAG, "thrash");
            doc.addField(RecordingIndexField.TAGCOUNT, "5");
            doc.addField(RecordingIndexField.TAG, "güth");
            doc.addField(RecordingIndexField.TAGCOUNT, "11");
            writer.addDocument(doc.getLuceneDocument());
        }

        //Recording 5
        {
            MbDocument doc = new MbDocument();
            doc.addField(RecordingIndexField.RECORDING_ID, "55555555-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.RECORDING, "Nope");
            doc.addField(RecordingIndexField.COMMENT, "the real one");
            doc.addField(RecordingIndexField.COUNTRY, "AF");
            doc.addField(RecordingIndexField.TAG, "thrash");
            doc.addField(RecordingIndexField.TAGCOUNT, "5");
            doc.addField(RecordingIndexField.TAG, "güth");
            doc.addField(RecordingIndexField.TAGCOUNT, "11");
            writer.addDocument(doc.getLuceneDocument());
        }

        //Recording 6
        {
            MbDocument doc = new MbDocument();
            doc.addField(RecordingIndexField.RECORDING_ID, "66666666-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(RecordingIndexField.RECORDING, "Luve");
            doc.addField(RecordingIndexField.COMMENT, "the real one");
            doc.addField(RecordingIndexField.COUNTRY, "AF");
            doc.addField(RecordingIndexField.TAG, "thrash");
            doc.addField(RecordingIndexField.TAGCOUNT, "5");
            doc.addField(RecordingIndexField.TAG, "güth");
            doc.addField(RecordingIndexField.TAGCOUNT, "11");
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        sd = new RecordingDismaxSearch(new IndexSearcher(ramDir, true));
    }

    /**
     * The current problem is that if you use default rewrite method then wildcard matches score very badly because
     * they get no idf boost, whereas if you use a scorer they score higher than exact match because the idf for the rarer
     * match is much higher than for the exact match, so ends up higher despite the fact that we boost exact match.
     *
     * @throws Exception
     */
    public void testCompareWildcardWithExactRecordingMatchDismax1() throws Exception {

        IndexSearcher searcher = sd.getIndexSearcher();
        Query q = sd.parseQuery("Luve");
        TopDocs topdocs = searcher.search(q, 10);
        for(ScoreDoc match:topdocs.scoreDocs)
        {
            Explanation explain = searcher.explain(q, match.doc);
            System.out.println("DocNo:"+match.doc+":"+match.score+":"+sd.getIndexSearcher().doc(match.doc).getFieldable("rid").stringValue());
            System.out.println(explain);
        }
        assertEquals(3,topdocs.scoreDocs.length);
    }


}
