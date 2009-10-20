package org.musicbrainz.search.servlet;
import junit.framework.TestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.velocity.app.Velocity;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.servlet.MbDocument;
import org.musicbrainz.search.servlet.ReleaseSearch;
import org.musicbrainz.search.servlet.mmd1.ReleaseMmd1XmlWriter;
import org.musicbrainz.search.servlet.ResourceType;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.ResultsWriter;
import org.musicbrainz.search.servlet.SearchServer;
import org.musicbrainz.search.servlet.SearchServerServlet;
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

        Document doc = new Document();
        Index.addFieldToDocument(doc, ReleaseIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
        Index.addFieldToDocument(doc, ReleaseIndexField.RELEASE, "Our Glorious 5 Year Plan");
        Index.addFieldToDocument(doc, ReleaseIndexField.SCRIPT, "Latn");
        Index.addFieldToDocument(doc, ReleaseIndexField.LANGUAGE, "eng");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST, "Farming Incident");
        Index.addFieldToDocument(doc, ReleaseIndexField.ARTIST_SORTNAME, "Incident, Farming");
        Index.addFieldToDocument(doc, ReleaseIndexField.NUM_TRACKS, "10");
        Index.addFieldToDocument(doc, ReleaseIndexField.NUM_DISC_IDS, "1");
        Index.addFieldToDocument(doc, ReleaseIndexField.STATUS, "Official");
        Index.addFieldToDocument(doc, ReleaseIndexField.TYPE, "album");
        Index.addFieldToDocument(doc, ReleaseIndexField.AMAZON_ID, "B00004Y6O9");

        Index.addFieldToDocument(doc, ReleaseIndexField.COUNTRY, "gb");
        Index.addFieldToDocument(doc, ReleaseIndexField.FORMAT, "Vinyl");
        Index.addFieldToDocument(doc, ReleaseIndexField.DATE, "2005");
        Index.addFieldToDocument(doc, ReleaseIndexField.BARCODE, "07599273202");

        //Per Event
        Index.addFieldToDocument(doc, ReleaseIndexField.CATALOG_NO, "WRATHCD25");
        Index.addFieldToDocument(doc, ReleaseIndexField.LABEL, "Wrath Records");



        writer.addDocument(doc);
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));

    }

    public void testFindReleaseByDefault() throws Exception {
        Results res = ss.search("\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));

    }

    public void testFindReleaseByArtistName() throws Exception {
        Results res = ss.search("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));

    }


    public void testFindReleaseByArtistSortname() throws Exception {
        Results res = ss.search("sortname:\"Incident, Farming\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));

    }


    public void testFindReleaseByFormat() throws Exception {
        Results res = ss.search("format:Vinyl", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));

    }

    public void testFindReleaseByCatNo() throws Exception {
        Results res = ss.search("catno:WRATHCD25", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));

    }

    public void testFindReleaseByBarcodeWithoutZero() throws Exception {
        Results res = ss.search("barcode:7599273202", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
     }

     public void testFindReleaseByAsinLowercase() throws Exception {
        Results res = ss.search("asin:b00004y6O9", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
         assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
         assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
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
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
        assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
        assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

        assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
        assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
        assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
        assertEquals("2005", doc.get(ReleaseIndexField.DATE));
        assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
        assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
        assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
        assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
        assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
        assertEquals("album", doc.get(ReleaseIndexField.TYPE));
        assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
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
         assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
         assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
         assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
         assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
         assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
        assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

         assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
         assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
         assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
         assertEquals("2005", doc.get(ReleaseIndexField.DATE));
         assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
         assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
         assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
         assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
         assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
         assertEquals("album", doc.get(ReleaseIndexField.TYPE));
         assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
         assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
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
         assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
         assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
         assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
         assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
         assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
         assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);

         assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
         assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
         assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
         assertEquals("2005", doc.get(ReleaseIndexField.DATE));
         assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
         assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
         assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
         assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
         assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
         assertEquals("album", doc.get(ReleaseIndexField.TYPE));
         assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
         //assertEquals("B00004Y6O9", doc.get(ReleaseIndexField.AMAZON_ID));
     }


    public void testFindReleaseByDate() throws Exception {
            Results res = ss.search("date:2005", 0, 10);
            assertEquals(1, res.totalHits);
            Result result = res.results.get(0);
            MbDocument doc = result.doc;
            assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
            assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
            assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
            assertEquals("10", doc.get(ReleaseIndexField.NUM_TRACKS));
            assertEquals(1, doc.getFields(ReleaseIndexField.CATALOG_NO).length);
            assertEquals("WRATHCD25", doc.get(ReleaseIndexField.CATALOG_NO));
            assertEquals(1, doc.getFields(ReleaseIndexField.BARCODE).length);
                    
            assertEquals(1, doc.getFields(ReleaseIndexField.COUNTRY).length);
            assertEquals("gb", doc.get(ReleaseIndexField.COUNTRY));
            assertEquals(1, doc.getFields(ReleaseIndexField.DATE).length);
            assertEquals("2005", doc.get(ReleaseIndexField.DATE));
            assertEquals(1, doc.getFields(ReleaseIndexField.NUM_DISC_IDS).length);
            assertEquals("1", doc.get(ReleaseIndexField.NUM_DISC_IDS));
            assertEquals("eng", doc.get(ReleaseIndexField.LANGUAGE));
            assertEquals("Latn", doc.get(ReleaseIndexField.SCRIPT));
            assertEquals("Official", doc.get(ReleaseIndexField.STATUS));
            assertEquals("album", doc.get(ReleaseIndexField.TYPE));
            assertEquals("Vinyl", doc.get(ReleaseIndexField.FORMAT));
        }

     public void testFindReleaseByTypeLowercase() throws Exception {
        Results res = ss.searchLucene("type:\"album\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseByTypeTitleCase() throws Exception {
        Results res = ss.searchLucene("type:\"Album\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseByNumericType() throws Exception {
           Results res = ss.searchLucene("type:1", 0, 10);
           assertEquals(1, res.totalHits);
           Result result = res.results.get(0);
           MbDocument doc = result.doc;
           assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
           assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
           assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
           assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
       }

     public void testFindReleaseByStatusLowercase() throws Exception {
        Results res = ss.searchLucene("status:\"official\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }

    public void testFindReleaseByStatusTitleCase() throws Exception {
        Results res = ss.searchLucene("status:\"Official\"", 0, 10);
        assertEquals(1, res.totalHits);
        Result result = res.results.get(0);
        MbDocument doc = result.doc;
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
        assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
    }
    public void testFindReleaseByNumericstatus() throws Exception {
           Results res = ss.searchLucene("status:1", 0, 10);
           assertEquals(1, res.totalHits);
           Result result = res.results.get(0);
           MbDocument doc = result.doc;
           assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
           assertEquals("Farming Incident", doc.get(ReleaseIndexField.ARTIST));
           assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
           assertEquals("album", doc.get(ReleaseGroupIndexField.TYPE));
       }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release/?type=xml&query=%22Our%20Glorious%205%20Year%20Plan%22
     *
     * @throws Exception
     */
    public void testOutputAsXml() throws Exception {

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
        assertTrue(output.contains("<disc-list count=\"1\""));
        assertTrue(output.contains("<track-list count=\"10\""));
        assertTrue(output.contains("date=\"2005\""));
        assertTrue(output.contains("country=\"GB\""));
        assertTrue(output.contains("format=\"Vinyl\""));
        assertTrue(output.contains("<asin>B00004Y6O9</asin>"));

        assertTrue(output.contains("<label><name>Wrath Records</name></label>"));
//TODO check if there is a good reason for this
//assertTrue(output.contains("barcode=\"-\""));       Code doesnt show if '-' but looks like should looking at main server
        assertTrue(output.contains("catalog-number=\"WRATHCD25\""));

    }
}