package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.MetaIndexField;

/**
 * Test retrieving artist from index and Outputting as Xml
 */
public class IssueSearch167Test {

    private AbstractDismaxSearchServer sd;

    @Before
    public void setUp() throws Exception {
        ObjectFactory of = new ObjectFactory();
        RAMDirectory ramDir = new RAMDirectory();
        Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
        writerConfig.setSimilarity(new MusicbrainzSimilarity());
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);


        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "11111111-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST_ACCENT, "Republica");
            doc.addField(ArtistIndexField.ARTIST, "Republica");
            doc.addField(ArtistIndexField.SORTNAME, "Republica");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.IPI, "1001");

            Artist artist = of.createArtist();
            artist.setId("11111111-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Republica");
            artist.setSortName("Republica");
            LifeSpan lifespan = of.createLifeSpan();
            lifespan.setBegin("1999-04");
            lifespan.setEnded("false");
            artist.setLifeSpan(lifespan);
            artist.setType("Group");
            artist.setDisambiguation("the real one");
            artist.setCountry("AF");
            Gender gender = new Gender();
            gender.setContent("male");
            artist.setGender(gender);
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("thrash");
            tag.setCount(BigInteger.valueOf(5));
            tagList.getTag().add(tag);

            tag = of.createTag();
            tag.setName("güth");
            tag.setCount(BigInteger.valueOf(11));
            tagList.getTag().add(tag);
            artist.setTagList(tagList);

            IpiList ipiList = of.createIpiList();
            ipiList.getIpi().add("1001");
            artist.setIpiList(ipiList);

            doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));
            writer.addDocument(doc.getLuceneDocument());
        }

        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "22222222-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST_ACCENT, "República");
            doc.addField(ArtistIndexField.ARTIST, "República");
            doc.addField(ArtistIndexField.SORTNAME, "República");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.IPI, "1001");

            Artist artist = of.createArtist();
            artist.setId("22222222-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Republica");
            artist.setSortName("Republica");
            LifeSpan lifespan = of.createLifeSpan();
            lifespan.setBegin("1999-04");
            lifespan.setEnded("false");
            artist.setLifeSpan(lifespan);
            artist.setType("Group");
            artist.setDisambiguation("the real one");
            artist.setCountry("AF");
            Gender gender = new Gender();
            gender.setContent("male");
            artist.setGender(gender);
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("thrash");
            tag.setCount(BigInteger.valueOf(5));
            tagList.getTag().add(tag);

            tag = of.createTag();
            tag.setName("güth");
            tag.setCount(BigInteger.valueOf(11));
            tagList.getTag().add(tag);
            artist.setTagList(tagList);

            IpiList ipiList = of.createIpiList();
            ipiList.getIpi().add("1001");
            artist.setIpiList(ipiList);

            doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));
            writer.addDocument(doc.getLuceneDocument());
        }

        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "33333333-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST_ACCENT, "Repúblice");
            doc.addField(ArtistIndexField.ARTIST, "Repúblice");
            doc.addField(ArtistIndexField.SORTNAME, "Repúblice");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAG, "güth");
            ;
            doc.addField(ArtistIndexField.IPI, "1001");

            Artist artist = of.createArtist();
            artist.setId("33333333-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Repúblice");
            artist.setSortName("Repúblice");
            LifeSpan lifespan = of.createLifeSpan();
            lifespan.setBegin("1999-04");
            lifespan.setEnded("false");
            artist.setLifeSpan(lifespan);
            artist.setType("Group");
            artist.setDisambiguation("the real one");
            artist.setCountry("AF");
            Gender gender = new Gender();
            gender.setContent("male");
            artist.setGender(gender);
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("thrash");
            tag.setCount(BigInteger.valueOf(5));
            tagList.getTag().add(tag);

            tag = of.createTag();
            tag.setName("güth");
            tag.setCount(BigInteger.valueOf(11));
            tagList.getTag().add(tag);
            artist.setTagList(tagList);

            IpiList ipiList = of.createIpiList();
            ipiList.getIpi().add("1001");
            artist.setIpiList(ipiList);

            doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));

            writer.addDocument(doc.getLuceneDocument());
        }

        //General Purpose Artist
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "44444444-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST_ACCENT, "Repúblic");
            doc.addField(ArtistIndexField.ARTIST, "Repúblic");
            doc.addField(ArtistIndexField.SORTNAME, "Repúblic");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.IPI, "1001");

            Artist artist = of.createArtist();
            artist.setId("44444444-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Repúblic");
            artist.setSortName("Repúblic");
            LifeSpan lifespan = of.createLifeSpan();
            lifespan.setBegin("1999-04");
            lifespan.setEnded("false");
            artist.setLifeSpan(lifespan);
            artist.setType("Group");
            artist.setDisambiguation("the real one");
            artist.setCountry("AF");
            Gender gender = new Gender();
            gender.setContent("male");
            artist.setGender(gender);
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("thrash");
            tag.setCount(BigInteger.valueOf(5));
            tagList.getTag().add(tag);

            tag = of.createTag();
            tag.setName("güth");
            tag.setCount(BigInteger.valueOf(11));
            tagList.getTag().add(tag);
            artist.setTagList(tagList);

            IpiList ipiList = of.createIpiList();
            ipiList.getIpi().add("1001");
            artist.setIpiList(ipiList);

            doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));
            writer.addDocument(doc.getLuceneDocument());
        }

        //For Phrase Searching Tests
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "55555555-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST_ACCENT, "echo & the bunnymen");
            doc.addField(ArtistIndexField.ARTIST, "echo & the bunnymen");
            doc.addField(ArtistIndexField.SORTNAME, "bunnymen, echo");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.IPI, "1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        //For Phrase Searching Tests
        {
            MbDocument doc = new MbDocument();
            doc.addField(ArtistIndexField.ARTIST_ID, "66666666-1cf0-4d1f-aca7-2a6f89e34b36");
            doc.addField(ArtistIndexField.ARTIST_ACCENT, "echobelly");
            doc.addField(ArtistIndexField.ARTIST, "echobelly");
            doc.addField(ArtistIndexField.SORTNAME, "echobelly");
            doc.addField(ArtistIndexField.BEGIN, "1999-04");
            doc.addField(ArtistIndexField.TYPE, "Group");
            doc.addField(ArtistIndexField.COMMENT, "the real one");
            doc.addField(ArtistIndexField.COUNTRY, "AF");
            doc.addField(ArtistIndexField.GENDER, "male");
            doc.addField(ArtistIndexField.TAG, "thrash");
            doc.addField(ArtistIndexField.TAG, "güth");
            doc.addField(ArtistIndexField.IPI, "1001");

            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.ARTIST));
        sd = new ArtistDismaxSearch(new ArtistSearch(searcherManager));
    }

    @Test
    public void testFindArtistDismax1() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Republica");
            TopDocs topdocs = searcher.search(q, 10);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue());
                System.out.println(explain);
            }
            assertEquals(4, topdocs.scoreDocs.length);

            org.apache.lucene.document.Document doc = searcher.doc(topdocs.scoreDocs[0].doc);
            assertEquals("11111111-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
            doc = searcher.doc(topdocs.scoreDocs[1].doc);
            assertEquals("22222222-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismax2() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("República");
            TopDocs topdocs = searcher.search(q, 10);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue());
                System.out.println(explain);
            }
            assertEquals(4, topdocs.scoreDocs.length);

            org.apache.lucene.document.Document doc = searcher.doc(topdocs.scoreDocs[0].doc);
            assertEquals("22222222-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
            doc = searcher.doc(topdocs.scoreDocs[1].doc);
            assertEquals("11111111-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
            doc = searcher.doc(topdocs.scoreDocs[2].doc);
            assertEquals("33333333-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
            doc = searcher.doc(topdocs.scoreDocs[3].doc);
            assertEquals("44444444-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxFuzzy() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Repúblic");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(4, topdocs.scoreDocs.length);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue() + ":" + explain);
            }

            org.apache.lucene.document.Document doc = searcher.doc(topdocs.scoreDocs[0].doc);
            assertEquals("44444444-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxWildcard() throws Exception {
        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Repúb");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(4, topdocs.scoreDocs.length);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue() + ":" + explain);
            }
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxShortWildcard() throws Exception {
        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Rep");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(4, topdocs.scoreDocs.length);
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxPhraseFuzzy() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Echo bunneymen");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(2, topdocs.scoreDocs.length);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue() + ":" + explain);
            }

            org.apache.lucene.document.Document doc = searcher.doc(topdocs.scoreDocs[0].doc);
            assertEquals("55555555-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

            doc = searcher.doc(topdocs.scoreDocs[1].doc);
            assertEquals("66666666-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxPhraseFuzzy2() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Echo & the bunnymen");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(2, topdocs.scoreDocs.length);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue() + ":" + explain);
            }

            org.apache.lucene.document.Document doc = searcher.doc(topdocs.scoreDocs[0].doc);
            assertEquals("55555555-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

            doc = searcher.doc(topdocs.scoreDocs[1].doc);
            assertEquals("66666666-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxWildcard2() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Echo");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(2, topdocs.scoreDocs.length);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue() + ":" + explain);
            }

            org.apache.lucene.document.Document doc = searcher.doc(topdocs.scoreDocs[0].doc);
            //assertEquals("55555555-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));

            doc = searcher.doc(topdocs.scoreDocs[1].doc);
            //assertEquals("66666666-1cf0-4d1f-aca7-2a6f89e34b36", doc.get(ArtistIndexField.ARTIST_ID.getName()));
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxSpecialChars() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Republica/DC");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(2, topdocs.scoreDocs.length);
            for (ScoreDoc match : topdocs.scoreDocs) {
                Explanation explain = searcher.explain(q, match.doc);
                System.out.println("DocNo:" + match.doc + ":" + match.score + ":" + searcher.doc(match.doc).getField("arid").stringValue() + ":" + explain);
            }
        } finally {
            searcherManager.release(searcher);
        }
    }

    @Test
    public void testFindArtistDismaxSpecialChars2() throws Exception {

        SearcherManager searcherManager = sd.getSearcherManager();
        IndexSearcher searcher = searcherManager.acquire();
        try {
            Query q = sd.parseQuery("Republica-DC");
            TopDocs topdocs = searcher.search(q, 10);
            assertEquals(2, topdocs.scoreDocs.length);
        } finally {
            searcherManager.release(searcher);
        }
    }

}
