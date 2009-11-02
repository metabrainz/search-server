package org.musicbrainz.search.servlet;
import junit.framework.TestCase;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.util.NumericUtils;
import org.apache.velocity.app.Velocity;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.servlet.ReleaseSearch;
import org.musicbrainz.search.servlet.mmd1.ReleaseMmd1XmlWriter;
import org.musicbrainz.search.servlet.ResourceType;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.ResultsWriter;
import org.musicbrainz.search.servlet.SearchServer;
import org.musicbrainz.search.servlet.SearchServerServlet;
import org.musicbrainz.search.servlet.mmd2.ReleaseXmlWriter;
import org.musicbrainz.search.analysis.PerFieldEntityAnalyzer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindReleaseTest extends TestCase {

    private SearchServer ss;


    public FindReleaseTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        SearchServerServlet.setUpVelocity();
        Velocity.init();
        RAMDirectory ramDir = new RAMDirectory();
        PerFieldAnalyzerWrapper analyzer = new PerFieldEntityAnalyzer(ReleaseIndexField.class);
        IndexWriter writer = new IndexWriter(ramDir, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);

        MbDocument doc = new MbDocument();
        doc.addField(ReleaseIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        doc.addField(ReleaseIndexField.RELEASE, "Our Glorious 5 Year Plan");
        doc.addField(ReleaseIndexField.SCRIPT, "Latn");
        doc.addField(ReleaseIndexField.LANGUAGE, "eng");
        doc.addField(ReleaseIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        doc.addField(ReleaseIndexField.ARTIST, "Farming Incident");
        doc.addField(ReleaseIndexField.ARTIST_NAME, "Farming Incident");
        doc.addField(ReleaseIndexField.ARTIST_NAMECREDIT, "Farming Incident");
        doc.addField(ReleaseIndexField.ARTIST_SORTNAME, "Incident, Farming");
        doc.addField(ReleaseIndexField.ARTIST_JOINPHRASE, "-");

        //Medium 1
        doc.addNumericField(ReleaseIndexField.NUM_TRACKS_MEDIUM, 10);
        doc.addNumericField(ReleaseIndexField.NUM_DISCIDS_MEDIUM, 1);
        doc.addField(ReleaseIndexField.FORMAT, "Vinyl");
        //Medium 2
        doc.addNumericField(ReleaseIndexField.NUM_TRACKS_MEDIUM, 7);
        doc.addNumericField(ReleaseIndexField.NUM_DISCIDS_MEDIUM, 2);
        doc.addField(ReleaseIndexField.FORMAT, "-");
        doc.addNumericField(ReleaseIndexField.NUM_TRACKS, 17);
        doc.addNumericField(ReleaseIndexField.NUM_DISCIDS, 3);

        doc.addField(ReleaseIndexField.STATUS, "Official");
        doc.addField(ReleaseIndexField.TYPE, "album");
        doc.addField(ReleaseIndexField.AMAZON_ID, "B00004Y6O9");

        doc.addField(ReleaseIndexField.COUNTRY, "gb");

        doc.addField(ReleaseIndexField.DATE, "2005");
        doc.addField(ReleaseIndexField.BARCODE, "07599273202");

        //Multiples allowed of these
        doc.addField(ReleaseIndexField.CATALOG_NO, "WRATHCD25");
        doc.addField(ReleaseIndexField.LABEL, "Wrath Records");

        doc.addField(ReleaseIndexField.CATALOG_NO, "LP001");
        doc.addField(ReleaseIndexField.LABEL, "Major Records");


        writer.addDocument(doc.getLuceneDocument());

        //Release with Multiple Artists
        doc = new MbDocument();
        doc.addField(ReleaseIndexField.RELEASE_ID, "0011c128-b1f2-300e-88cc-c33c30dce704");
        doc.addField(ReleaseIndexField.RELEASE, "Epics");
        doc.addField(ReleaseIndexField.SCRIPT, "Taml");
        doc.addField(ReleaseIndexField.LANGUAGE, "fra");
        doc.addField(ReleaseIndexField.TYPE, ReleaseGroupType.SINGLE.getName());
        doc.addField(ReleaseIndexField.ARTIST, "Erich Kunzel and Cincinnati Pops");
        doc.addField(ReleaseIndexField.ARTIST_ID, "99845d0c-f239-4051-a6b1-4b5e9f7ede0b");
        doc.addField(ReleaseIndexField.ARTIST_NAME, "Erich Kunzel");
        doc.addField(ReleaseIndexField.ARTIST_SORTNAME, "Kunzel, Eric");
        doc.addField(ReleaseIndexField.ARTIST_JOINPHRASE, "and");
        doc.addField(ReleaseIndexField.ARTIST_NAMECREDIT, "Erich Kunzel");

        doc.addField(ReleaseIndexField.ARTIST_ID, "d8fbd94c-cd06-4e8b-a559-761ad969d07e");
        doc.addField(ReleaseIndexField.ARTIST_NAME, "The Cincinnati Pops Orchestra");
        doc.addField(ReleaseIndexField.ARTIST_SORTNAME, "Cincinnati Pops Orchestra, The");
        doc.addField(ReleaseIndexField.ARTIST_JOINPHRASE, "-");
        doc.addField(ReleaseIndexField.ARTIST_NAMECREDIT, "Cincinnati Pops");

        doc.addNumericField(ReleaseIndexField.NUM_TRACKS_MEDIUM, 14);
        doc.addNumericField(ReleaseIndexField.NUM_DISCIDS_MEDIUM, 1);
        doc.addField(ReleaseIndexField.STATUS, "Promotion");
        doc.addNumericField(ReleaseIndexField.NUM_TRACKS, 14);
        doc.addNumericField(ReleaseIndexField.NUM_DISCIDS, 1);
        doc.addField(ReleaseIndexField.FORMAT, "CD");

        doc.addField(ReleaseIndexField.COUNTRY, "us");
        doc.addField(ReleaseIndexField.DATE, "2003-09-23");
        writer.addDocument(doc.getLuceneDocument());

        writer.close();
        Map<ResourceType, IndexSearcher> searchers = new HashMap<ResourceType, IndexSearcher>();
        searchers.put(ResourceType.RELEASE, new IndexSearcher(ramDir,true));
        ss = new ReleaseSearch(new IndexSearcher(ramDir,true));
    }

    public void testFindReleaseById() throws Exception {
        Results res = ss.search("reid:\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals(2, doc.getFields(ReleaseIndexField.NUM_TRACKS_MEDIUM).length);
        assertEquals(2, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(2, doc.getFields(ReleaseIndexField.NUM_DISCIDS_MEDIUM).length);
        assertEquals(1, NumericUtils.prefixCodedToInt(doc.get(ReleaseIndexField.NUM_DISCIDS_MEDIUM)));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));


    }

    public void testFindReleaseByName() throws Exception {
        Results res = ss.search("release:\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
    }

    public void testFindReleaseByDefault() throws Exception {
        Results res = ss.search("\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));

    }

    public void testFindReleaseByArtistName() throws Exception {
        Results res = ss.search("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));

    }


    public void testFindReleaseByArtistSortname() throws Exception {
        Results res = ss.search("sortname:\"Incident, Farming\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));

    }


    public void testFindReleaseByFormat() throws Exception {
        Results res = ss.search("format:Vinyl", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));

    }

    public void testFindReleaseByCatNo() throws Exception {
        Results res = ss.search("catno:WRATHCD25", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
    }

    public void testFindReleaseByBarcodeWithoutZero() throws Exception {
        Results res = ss.search("barcode:7599273202", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);
        assertEquals("07599273202", doc.get(ReleaseIndexField.BARCODE));
    }

    public void testFindReleaseByBarcodeWithZero() throws Exception {
        Results res = ss.search("barcode:07599273202", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);
        assertEquals("07599273202", doc.get(ReleaseIndexField.BARCODE));
    }


    public void testFindReleaseByAsin() throws Exception {
        Results res = ss.search("asin:B00004Y6O9", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }

     public void testFindReleaseByAsinLowercase() throws Exception {
        Results res = ss.search("asin:b00004y6O9", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }

    /** Works as is even though lang code not analysed because lang code always lowercase
     *
     * @throws Exception
     */
    public void testFindReleaseByLanguage() throws Exception {
        Results res = ss.search("lang:eng", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }

    /** Works as is even though lang code not analysed because lang code always lowercase
     *
     * @throws Exception
     */
    public void testFindReleaseByLanguageUppercase() throws Exception {
        Results res = ss.search("lang:ENG", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }

     /**
     *
     * @throws Exception
     */
    public void testFindReleaseByScript() throws Exception {
        Results res = ss.search("script:latn", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }

    /**
     *
     * @throws Exception
     */
    public void testFindReleaseByScriptUppercase() throws Exception {
        Results res = ss.search("script:LATN", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
    }

    /*
      * @throws Exception
      */
     public void testFindReleaseByCountry() throws Exception {
         Results res = ss.search("country:gb", 0, 10);
         assertEquals(1, res.totalHits);
         Result result = res.results.get(0);
         MbDocument doc = result.doc;
         assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
         assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
         assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
      }

    /*
     * @throws Exception
     */
    public void testFindReleaseByNumTracks() throws Exception {
        Results res = ss.search("tracks:17", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }

    /*
     * @throws Exception
     */
    public void testFindReleaseByTracksOnMedium() throws Exception {
        Results res = ss.search("tracksmedium:10", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }

    /*
        * @throws Exception
        */
       public void testFindReleaseByNumDiscOnMedium() throws Exception {
           Results res = ss.search("discidsmedium:2", 0, 10);
           assertEquals(1, res.totalHits);
           Result result = res.results.get(0);
           MbDocument doc = result.doc;
           assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
           assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
           assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        }

    /*
        * @throws Exception
        */
       public void testFindReleaseByNumDisc() throws Exception {
           Results res = ss.search("discids:3", 0, 10);
           assertEquals(1, res.totalHits);
           Result result = res.results.get(0);
           MbDocument doc = result.doc;
           assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
           assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
           assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        }
     /**
      *
      * @throws Exception
      */
     public void testFindReleaseByCountryUppercase() throws Exception {
         Results res = ss.search("country:GB", 0, 10);
         assertEquals(1, res.totalHits);
         Result result = res.results.get(0);
         MbDocument doc = result.doc;
         assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
         assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
         assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
     }


    public void testFindReleaseByDate() throws Exception {
            Results res = ss.search("date:2005", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
            assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
            assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        }

     public void testFindReleaseByTypeLowercase() throws Exception {
        Results res = ss.searchLucene("type:\"album\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseByTypeTitleCase() throws Exception {
        Results res = ss.searchLucene("type:\"Album\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseByNumericType() throws Exception {
           Results res = ss.searchLucene("type:1", 0, 10);
           assertEquals(1, res.totalHits);
           Result result = res.results.get(0);
           MbDocument doc = result.doc;
           assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
           assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
           assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
           assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
       }

     public void testFindReleaseByStatusLowercase() throws Exception {
        Results res = ss.searchLucene("status:\"official\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseByStatusTitleCase() throws Exception {
        Results res = ss.searchLucene("status:\"Official\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }
    public void testFindReleaseByNumericstatus() throws Exception {
           Results res = ss.searchLucene("status:1", 0, 10);
           assertEquals(1, res.totalHits);
           Result result = res.results.get(0);
           MbDocument doc = result.doc;
           assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
           assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST_NAME));
           assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
           assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
       }

    public void testFindReleaseGroupByArtist2() throws Exception {
        Results res = ss.searchLucene("artist:\"Erich Kunzel\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseIndexField.RELEASE_ID));
        assertEquals("Epics", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Erich Kunzel and Cincinnati Pops",doc.get(ReleaseGroupIndexField.ARTIST));

    }

    public void testFindReleaseGroupByAllArtist2() throws Exception {
        Results res = ss.searchLucene("artist:\"Erich Kunzel and Cincinnati Pops\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("0011c128-b1f2-300e-88cc-c33c30dce704", doc.get(ReleaseIndexField.RELEASE_ID));
        assertEquals("Epics", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Erich Kunzel and Cincinnati Pops",doc.get(ReleaseGroupIndexField.ARTIST));
    }

    
    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release/?type=xml&query=%22Our%20Glorious%205%20Year%20Plan%22
     *
     * @throws Exception
     */
    public void testOutputAsMmdv1Xml() throws Exception {

        Results res = ss.searchLucene("release:\"Our Glorious 5 Year Plan\"", 0, 1);
        ResultsWriter writer = new ReleaseMmd1XmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("language=\"ENG\""));
        assertTrue(output.contains("script=\"Latn\""));
        assertTrue(output.contains("type=\"Album Official\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<disc-list count=\"3\""));
        assertTrue(output.contains("<track-list count=\"17\""));
        assertTrue(output.contains("date=\"2005\""));
        assertTrue(output.contains("country=\"GB\""));
        assertTrue(output.contains("format=\"Vinyl\""));
        assertTrue(output.contains("<asin>B00004Y6O9</asin>"));

        assertTrue(output.contains("<label><name>Wrath Records</name></label>"));
        assertTrue(output.contains("catalog-number=\"WRATHCD25\""));

    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release/?type=xml&query=%22Our%20Glorious%205%20Year%20Plan%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

        Results res = ss.searchLucene("release:\"Our Glorious 5 Year Plan\"", 0, 1);
        ResultsWriter writer = new ReleaseXmlWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("<language>eng</language>"));
        assertTrue(output.contains("<script>latn</script>"));
        assertTrue(output.contains("<release-group type=\"album\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<disc-list count=\"1\""));
        assertTrue(output.contains("<track-list count=\"10\""));
        assertTrue(output.contains("<date>2005</date>"));
        assertTrue(output.contains("<country>gb</country>"));
        assertTrue(output.contains("<format>vinyl</format>"));
        assertTrue(output.contains("<asin>07599273202</asin>"));

        assertTrue(output.contains("<label><name>Wrath Records</name></label>"));
        assertTrue(output.contains("<catalog-number>WRATHCD25</catalog-number>"));

    }
}