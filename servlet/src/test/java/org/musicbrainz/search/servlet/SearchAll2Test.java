
package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.servlet.mmd1.LabelType;
import org.musicbrainz.search.servlet.mmd2.AllWriter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class SearchAll2Test
{

    private AbstractSearchServer labelSearch;
    private AbstractSearchServer artistSearch;
    private AbstractSearchServer releaseSearch;
    private AbstractSearchServer releaseGroupSearch;
    private AbstractSearchServer recordingSearch;
    private AbstractSearchServer workSearch;



    @Before
    public void setUp() throws Exception
    {
        ObjectFactory of = new ObjectFactory();
        {
            RAMDirectory ramDir = new RAMDirectory();
            Analyzer analyzer = DatabaseIndex.getAnalyzer(LabelIndexField.class);
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            writerConfig.setSimilarity(new MusicbrainzSimilarity());
            IndexWriter writer = new IndexWriter(ramDir, writerConfig);

            {
                MbDocument doc = new MbDocument();
                doc.addField(LabelIndexField.LABEL_ID, "ff571ff4-04cb-4b9c-8a1c-354c330f863c");
                doc.addField(LabelIndexField.LABEL, "Jockey Slut");
                doc.addField(LabelIndexField.SORTNAME, "Slut, Jockey");
                doc.addField(LabelIndexField.ALIAS, "Jockeys");
                doc.addField(LabelIndexField.CODE, 1234);
                doc.addField(LabelIndexField.BEGIN, "1993");
                doc.addField(LabelIndexField.END, "2004");
                doc.addField(LabelIndexField.ENDED, "true");
                doc.addField(LabelIndexField.TYPE, "Production");
                doc.addField(LabelIndexField.COUNTRY, "GB");
                doc.addField(LabelIndexField.TAG, "dance");
                doc.addField(LabelIndexField.TAGCOUNT, "22");
                doc.addField(LabelIndexField.IPI, "1001");

                writer.addDocument(doc.getLuceneDocument());
            }

            {
                MbDocument doc = new MbDocument();
                doc.addField(LabelIndexField.LABEL_ID, "a539bb1e-f2e1-4b45-9db8-8053841e7503");
                doc.addField(LabelIndexField.LABEL, "4AD");
                doc.addField(LabelIndexField.SORTNAME, "4AD");
                doc.addField(LabelIndexField.BEGIN, "1979");
                doc.addField(LabelIndexField.CODE, 5807);
                doc.addField(LabelIndexField.TYPE, LabelType.PRODUCTION.getName());
                doc.addField(LabelIndexField.COUNTRY, "unknown");

                writer.addDocument(doc.getLuceneDocument());
            }

            {
                MbDocument doc = new MbDocument();
                doc.addField(LabelIndexField.LABEL_ID, "a539bb1e-f2e1-4b45-9db8-8053841e7504");
                doc.addField(LabelIndexField.LABEL, "Dark Prism");
                doc.addField(LabelIndexField.SORTNAME, "Dark Prism");
                doc.addField(LabelIndexField.CODE, Index.NO_VALUE);
                doc.addField(LabelIndexField.TYPE, LabelType.HOLDING.getName());
                writer.addDocument(doc.getLuceneDocument());
            }

            {
                MbDocument doc = new MbDocument();
                doc.addField(LabelIndexField.LABEL_ID, "b539bb1e-f2e1-4b45-9db8-8053841e7504");
                doc.addField(LabelIndexField.LABEL, "blob");
                doc.addField(LabelIndexField.SORTNAME, "blob");
                doc.addField(LabelIndexField.TYPE, "unknown");
                writer.addDocument(doc.getLuceneDocument());
            }

            {
                MbDocument doc = new MbDocument();
                doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
                doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
                writer.addDocument(doc.getLuceneDocument());
            }


            writer.close();
            SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.LABEL));
            labelSearch = new LabelSearch(searcherManager);
        }

        {
            RAMDirectory ramDir = new RAMDirectory();
            Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            writerConfig.setSimilarity(new MusicbrainzSimilarity());
            IndexWriter writer = new IndexWriter(ramDir, writerConfig);


            //General Purpose Artist
            {
                MbDocument doc = new MbDocument();
                doc.addField(ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                doc.addField(ArtistIndexField.ARTIST, "Dark Incident");
                doc.addField(ArtistIndexField.SORTNAME, "Dark, Incident");
                doc.addField(ArtistIndexField.BEGIN, "1999-04");
                doc.addField(ArtistIndexField.ENDED, "true");
                doc.addField(ArtistIndexField.TYPE, "Group");
                doc.addField(ArtistIndexField.COMMENT, "the real one");
                doc.addField(ArtistIndexField.COUNTRY, "AF");
                doc.addField(ArtistIndexField.GENDER, "male");
                doc.addField(ArtistIndexField.TAG, "thrash");
                doc.addField(ArtistIndexField.TAG, "güth");
                doc.addField(ArtistIndexField.IPI, "1001");
                doc.addField(ArtistIndexField.IPI, "1002");

                Artist artist = of.createArtist();
                artist.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                artist.setName("Dark Incident");
                artist.setSortName("Dark Incident");
                LifeSpan lifespan = of.createLifeSpan();
                lifespan.setBegin("1999-04");
                lifespan.setEnded("true");
                artist.setLifeSpan(lifespan);
                artist.setType("Group");
                artist.setDisambiguation("the real one");
                artist.setCountry("AF");
                artist.setGender("male");
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
                ipiList.getIpi().add("1002");
                artist.setIpiList(ipiList);

                doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));

                writer.addDocument(doc.getLuceneDocument());
            }

            //Artist with & on name and aliases
            {
                MbDocument doc = new MbDocument();
                doc.addField(ArtistIndexField.ARTIST_ID, "ccd4879c-5e88-4385-b131-bf65296bf245");
                doc.addField(ArtistIndexField.ARTIST, "The darkness");
                doc.addField(ArtistIndexField.SORTNAME, "dark,the");
                doc.addField(ArtistIndexField.BEGIN, "1978");
                doc.addField(ArtistIndexField.COUNTRY, "unknown");
                doc.addField(ArtistIndexField.TYPE, "Group");
                doc.addField(ArtistIndexField.ALIAS, "The darkness");

                Artist artist = of.createArtist();
                artist.setId("ccd4879c-5e88-4385-b131-bf65296bf245");
                artist.setName("The darkness");
                artist.setSortName("dark,the");
                LifeSpan lifespan = of.createLifeSpan();
                lifespan.setBegin("1978");
                artist.setLifeSpan(lifespan);
                artist.setType("Group");

                AliasList aliasList = of.createAliasList();
                Alias alias = of.createAlias();
                alias.setContent("The darkness");
                aliasList.getAlias().add(alias);

                doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));

                writer.addDocument(doc.getLuceneDocument());
            }

            //Artist, type person unknown gender
            {
                MbDocument doc = new MbDocument();
                doc.addField(ArtistIndexField.ARTIST_ID, "dde4879c-5e88-4385-b131-bf65296bf245");
                doc.addField(ArtistIndexField.ARTIST, "The dark place");
                doc.addField(ArtistIndexField.TYPE, ArtistType.PERSON.getName());
                doc.addField(ArtistIndexField.GENDER, "unknown");

                Artist artist = of.createArtist();
                artist.setId("dde4879c-5e88-4385-b131-bf65296bf245");
                artist.setName("The dark place");
                artist.setSortName("dark,the");
                artist.setType("Person");

                doc.addField(ArtistIndexField.ARTIST_STORE, MMDSerializer.serialize(artist));
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
            artistSearch = new ArtistSearch(searcherManager);
        }

        {
            RAMDirectory ramDir = new RAMDirectory();
            Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            writerConfig.setSimilarity(new MusicbrainzSimilarity());
            IndexWriter writer = new IndexWriter(ramDir, writerConfig);
            writer.close();
            SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.RELEASE));
            releaseSearch = new ReleaseSearch(searcherManager);

        }

        {
            RAMDirectory ramDir = new RAMDirectory();
            Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            writerConfig.setSimilarity(new MusicbrainzSimilarity());
            IndexWriter writer = new IndexWriter(ramDir, writerConfig);
            writer.close();
            SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.RECORDING));
            recordingSearch = new RecordingSearch(searcherManager);

        }

        {
            RAMDirectory ramDir = new RAMDirectory();
            Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            writerConfig.setSimilarity(new MusicbrainzSimilarity());
            IndexWriter writer = new IndexWriter(ramDir, writerConfig);
            writer.close();
            SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.RELEASE_GROUP));
            releaseGroupSearch = new ReleaseGroupSearch(searcherManager);

        }

        {
            RAMDirectory ramDir = new RAMDirectory();
            Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            writerConfig.setSimilarity(new MusicbrainzSimilarity());
            IndexWriter writer = new IndexWriter(ramDir, writerConfig);
            writer.close();
            SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.WORK));
            workSearch = new WorkSearch(searcherManager);

        }
    }

    @Test
    public void testSearchAll() throws Exception
    {
        Collection<Callable<Results>> searches = new ArrayList<Callable<Results>>();
        searches.add(new CallableSearch(artistSearch, "dark", 0, 10));
        searches.add(new CallableSearch(releaseSearch, "dark", 0, 10));
        searches.add(new CallableSearch(releaseGroupSearch, "dark", 0, 10));
        searches.add(new CallableSearch(labelSearch, "dark", 0, 10));
        searches.add(new CallableSearch(recordingSearch,"dark", 0, 10));
        searches.add(new CallableSearch(workSearch, "dark", 0, 10));

        ExecutorService es = Executors.newCachedThreadPool();
        List<Future<Results>> results = es.invokeAll(searches);
        Results allResults = new Results();
        // Results are returned in same order as they were submitted
        Results artistResults = results.get(0).get();
        Results releaseResults = results.get(1).get();
        Results releaseGroupResults = results.get(2).get();
        Results labelResults = results.get(3).get();
        Results recordingResults = results.get(4).get();
        Results workResults = results.get(5).get();

        assertEquals(3, artistResults.getTotalHits());
        assertEquals(1, labelResults.getTotalHits());
        assertEquals(0, releaseResults.getTotalHits());



        AllWriter writer = new AllWriter(0, 10, artistResults, releaseResults, releaseGroupResults, labelResults,
                recordingResults, workResults);
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr,allResults,"xml",true);
        pr.close();
        String output = sw.toString();
        System.out.println(output);
        assertTrue(output.contains("<entity-list count=\"4\" offset=\"0\">"));
        assertTrue(output.contains("<label id=\"a539bb1e-f2e1-4b45-9db8-8053841e7504\" type=\"holding\" ext:score=\"100\">"));
        assertTrue(output.contains("<artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\" type=\"Group\" ext:score=\"56\">"));

    }

    @Test
    public void testSearchAllJson() throws Exception
    {
        Collection<Callable<Results>> searches = new ArrayList<Callable<Results>>();
        searches.add(new CallableSearch(artistSearch, "dark", 0, 10));
        searches.add(new CallableSearch(releaseSearch, "dark", 0, 10));
        searches.add(new CallableSearch(releaseGroupSearch, "dark", 0, 10));
        searches.add(new CallableSearch(labelSearch, "dark", 0, 10));
        searches.add(new CallableSearch(recordingSearch,"dark", 0, 10));
        searches.add(new CallableSearch(workSearch, "dark", 0, 10));

        ExecutorService es = Executors.newCachedThreadPool();
        List<Future<Results>> results = es.invokeAll(searches);
        Results allResults = new Results();
        // Results are returned in same order as they were submitted
        Results artistResults = results.get(0).get();
        Results releaseResults = results.get(1).get();
        Results releaseGroupResults = results.get(2).get();
        Results labelResults = results.get(3).get();
        Results recordingResults = results.get(4).get();
        Results workResults = results.get(5).get();

        assertEquals(3, artistResults.getTotalHits());
        assertEquals(1, labelResults.getTotalHits());
        assertEquals(0, releaseResults.getTotalHits());



        AllWriter writer = new AllWriter(0, 10, artistResults, releaseResults, releaseGroupResults, labelResults,
                recordingResults, workResults);
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr,allResults,SearchServerServlet.RESPONSE_JSON_NEW,true);
        pr.close();
        String output = sw.toString();
        System.out.println(output);
        assertTrue(output.contains("\"count\" : 4"));
        assertTrue(output.contains("\"id\" : \"a539bb1e-f2e1-4b45-9db8-8053841e7504\""));
        assertTrue(output.contains("\"id\" : \"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\","));

    }
}
