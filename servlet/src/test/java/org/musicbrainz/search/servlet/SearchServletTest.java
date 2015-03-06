package org.musicbrainz.search.servlet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.LuceneVersion;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.analysis.MusicbrainzSimilarity;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.DatabaseIndex;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.MetaIndexField;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.Stack;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * SearchServlet tests
 */
public class SearchServletTest
{
    /**
     * @throws Exception exception
     */
    @Test
    public void testSearch411() throws Exception {

        SearchServerServlet ss = new SearchServerServlet();
        StackTraceElement [] stes = new StackTraceElement[2];
        stes[0] = new StackTraceElement("java.util.TreeMap", "getEntry","TreeMap.java",342);
        boolean result = ss.isUnescapedBackslashIssue(stes, "artist:pandora /reyli barba recording:Solo el y yo/Alguien llena mi lugar");
        assertTrue(result);
    }


    @Test
    public void testSearch411False1() throws Exception {

        SearchServerServlet ss = new SearchServerServlet();
        StackTraceElement [] stes = new StackTraceElement[2];
        stes[0] = new StackTraceElement("java.util.TreeMap", "getEntry","TreeMap.java",342);
        boolean result = ss.isUnescapedBackslashIssue(stes, "artist:pandora reyli barba recording:Solo el y yoAlguien llena mi lugar");
        assertFalse(result);
    }

    @Test
    public void testSearch411False2() throws Exception {

        SearchServerServlet ss = new SearchServerServlet();
        StackTraceElement [] stes = new StackTraceElement[2];
        stes[0] = new StackTraceElement("java.util.OtherClass", "getEntry","TreeMap.java",342);
        boolean result = ss.isUnescapedBackslashIssue(stes, "artist:pandora /reyli barba recording:Solo el y yo/Alguien llena mi lugar");
        assertFalse(result);
    }

    @Test
    public void testSearch411DoSearch() throws Exception
    {

        String query = "artist:pandora /reyli barba recording:Solo el y yo/Alguien llena mi lugar";
        boolean result = false;
        try
        {
            ObjectFactory of = new ObjectFactory();
            RAMDirectory ramDir = new RAMDirectory();
            Analyzer analyzer = DatabaseIndex.getAnalyzer(ArtistIndexField.class);
            IndexWriterConfig writerConfig = new IndexWriterConfig(LuceneVersion.LUCENE_VERSION, analyzer);
            writerConfig.setSimilarity(new MusicbrainzSimilarity());
            IndexWriter writer = new IndexWriter(ramDir, writerConfig);
            //General Purpose Artist
            {
                MbDocument doc = new MbDocument();
                doc.addField(ArtistIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                doc.addField(ArtistIndexField.ARTIST, "Farming Incident");
                doc.addField(ArtistIndexField.SORTNAME, "Incident, Farming");
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
                doc.addField(ArtistIndexField.ISNI, "abcdef");

                Artist artist = of.createArtist();
                artist.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                artist.setName("Farming Incident");
                artist.setSortName("Incident, Farming");
                LifeSpan lifespan = of.createLifeSpan();
                lifespan.setBegin("1999-04");
                lifespan.setEnded("true");
                artist.setLifeSpan(lifespan);
                artist.setType("Group");
                artist.setDisambiguation("the real one");
                artist.setCountry("AF");

                DefAreaElementInner area = of.createDefAreaElementInner();
                area.setId("5302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                area.setName("Afghanistan");
                area.setSortName("Afghanistan");
                artist.setArea(area);
                doc.addField(ArtistIndexField.AREA, "Afghanistan");

                DefAreaElementInner beginArea = of.createDefAreaElementInner();
                beginArea.setId("6302e264-1cf0-4d1f-aca7-2a6f89e34b36");
                beginArea.setName("Canada");
                beginArea.setSortName("Canada");
                artist.setBeginArea(beginArea);
                doc.addField(ArtistIndexField.BEGIN_AREA, "Canada");

                doc.addField(ArtistIndexField.END_AREA, "-");

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
            {
                MbDocument doc = new MbDocument();
                doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
                doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
                writer.addDocument(doc.getLuceneDocument());
            }

            writer.close();
            SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.ARTIST));
            SearchServer ss = new ArtistSearch(searcherManager);
            Results results = ss.search(query, 0, 1);
        }
        catch (NullPointerException npe)
        {
            SearchServerServlet sss = new SearchServerServlet();
            result = sss.isUnescapedBackslashIssue(npe.getStackTrace(), query);
        }
        assertTrue(result);
    }
}