package org.musicbrainz.search.servlet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.SearcherManager;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.junit.Before;
import org.junit.Test;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.*;
import org.musicbrainz.search.servlet.mmd1.ReleaseGroupType;
import org.musicbrainz.search.servlet.mmd1.ReleaseMmd1XmlWriter;

/**
 * Assumes an index has been built stored and in the data folder, I've picked a fairly obscure bside so hopefully
 * will not get added to another release
 */
public class FindReleaseTest {

    protected void checkTerm(IndexReader ir, IndexField field, String value) throws IOException {

        Fields fields = MultiFields.getFields(ir);
        Terms terms = fields.terms(field.getName());
        TermsEnum termsEnum = terms.iterator();
        termsEnum.next();
        assertEquals(value, termsEnum.term().utf8ToString());
    }

    private AbstractSearchServer ss;
    private AbstractDismaxSearchServer sd;

    @Before
    public void setUp() throws Exception {
        RAMDirectory ramDir = new RAMDirectory();
        ObjectFactory of = new ObjectFactory();

        Analyzer analyzer = DatabaseIndex.getAnalyzer(ReleaseIndexField.class);
        IndexWriterConfig writerConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(ramDir, writerConfig);
        {
            MbDocument doc = new MbDocument();
            Release release = of.createRelease();
            doc.addField(ReleaseIndexField.RELEASE_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");
            release.setId("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");

            doc.addField(ReleaseIndexField.RELEASEGROUP_ID, "1d9e8ed6-3893-4d3b-aa7d-6cd79609e333");
            ReleaseGroup rg = of.createReleaseGroup();
            rg.setId("1d9e8ed6-3893-4d3b-aa7d-6cd79609e333");
            release.setReleaseGroup(rg);

            doc.addField(ReleaseIndexField.RELEASE, "Our Glorious 5 Year Plan");
            release.setTitle("Our Glorious 5 Year Plan");

            doc.addField(ReleaseIndexField.SCRIPT, "Latn");
            TextRepresentation tr = of.createTextRepresentation();
            release.setTextRepresentation(tr);
            tr.setScript("Latn");
            doc.addField(ReleaseIndexField.LANGUAGE, "eng");
            tr.setLanguage("eng");

            doc.addField(ReleaseIndexField.ARTIST_ID, "4302e264-1cf0-4d1f-aca7-2a6f89e34b36");

            doc.addField(ReleaseIndexField.ARTIST, "Farming Incident");
            doc.addField(ReleaseIndexField.ARTIST_NAME, "Farming Incident");
            doc.addField(ReleaseIndexField.ARTIST_NAMECREDIT, "Farming Incident");
            ArtistCredit ac = of.createArtistCredit();
            NameCredit nc = of.createNameCredit();
            Artist artist = of.createArtist();
            artist.setId("4302e264-1cf0-4d1f-aca7-2a6f89e34b36");
            artist.setName("Farming Incident");
            artist.setSortName("Incident, Farming");
            nc.setArtist(artist);
            ac.getNameCredit().add(nc);
            release.setArtistCredit(ac);

            doc.addField(ReleaseIndexField.COMMENT, "demo");
            release.setDisambiguation("demo");

            doc.addField(ReleaseIndexField.PRIMARY_TYPE, "Album");
            release.getReleaseGroup().setType("Compilation");
            PrimaryType pt = new PrimaryType();
            pt.setContent("Album");
            release.getReleaseGroup().setPrimaryType(pt);
            doc.addField(ReleaseIndexField.TYPE, "Compilation");
            doc.addField(ReleaseIndexField.SECONDARY_TYPE, "Live");
            doc.addField(ReleaseIndexField.SECONDARY_TYPE, "Compilation");
            SecondaryTypeList stl = of.createSecondaryTypeList();
            SecondaryType st = new SecondaryType();
            st.setContent("Live");
            stl.getSecondaryType().add(st);
            st = new SecondaryType();
            st.setContent("Compilation");
            stl.getSecondaryType().add(st);
            release.getReleaseGroup().setSecondaryTypeList(stl);


            doc.addField(ReleaseIndexField.PACKAGING, "Jewel Case");
            release.setPackaging("Jewel Case");

            doc.addField(ReleaseIndexField.QUALITY, ReleaseQuality.HIGH.toString());
            doc.addField(ReleaseIndexField.TAG, "punk");
            TagList tagList = of.createTagList();
            Tag tag = of.createTag();
            tag.setName("punk");
            tag.setCount(new BigInteger("10"));
            tagList.getTag().add(tag);
            release.setTagList(tagList);

            MediumList mediumList = of.createMediumList();
            //Medium 1
            {
                doc.addNumericField(ReleaseIndexField.NUM_TRACKS_MEDIUM, 10);
                doc.addNumericField(ReleaseIndexField.NUM_DISCIDS_MEDIUM, 1);
                doc.addField(ReleaseIndexField.FORMAT, "Vinyl");
                Medium medium = of.createMedium();
                Format format = new Format();
                format.setContent("Vinyl");
                medium.setFormat(format);
                org.musicbrainz.mmd2.Medium.TrackList trackList = of.createMediumTrackList();
                trackList.setCount(BigInteger.valueOf(10));
                medium.setTrackList(trackList);
                DiscList discList = of.createDiscList();
                discList.setCount(BigInteger.valueOf(1));
                medium.setDiscList(discList);
                mediumList.getMedium().add(medium);
            }
            //Medium 2
            {
                doc.addNumericField(ReleaseIndexField.NUM_TRACKS_MEDIUM, 7);
                doc.addNumericField(ReleaseIndexField.NUM_DISCIDS_MEDIUM, 2);
                doc.addField(ReleaseIndexField.FORMAT, Index.NO_VALUE);
                Medium medium = of.createMedium();
                org.musicbrainz.mmd2.Medium.TrackList trackList = of.createMediumTrackList();
                trackList.setCount(BigInteger.valueOf(7));
                medium.setTrackList(trackList);
                DiscList discList = of.createDiscList();
                discList.setCount(BigInteger.valueOf(2));
                medium.setDiscList(discList);
                mediumList.getMedium().add(medium);
            }

            doc.addNumericField(ReleaseIndexField.NUM_TRACKS, 17);
            doc.addNumericField(ReleaseIndexField.NUM_DISCIDS, 3);
            doc.addNumericField(ReleaseIndexField.NUM_MEDIUMS, 2);
            mediumList.setTrackCount(BigInteger.valueOf(17));
            mediumList.setCount(BigInteger.valueOf(2));
            release.setMediumList(mediumList);

            doc.addField(ReleaseIndexField.STATUS, "Official");
            Status stat = new Status();
            stat.setContent("Official");
            release.setStatus(stat);

            doc.addField(ReleaseIndexField.AMAZON_ID, "B00004Y6O9");
            release.setAsin("B00004Y6O9");

            doc.addField(ReleaseIndexField.COUNTRY, "GB");
            doc.addField(ReleaseIndexField.DATE, "2005");
            ReleaseEventList rel = of.createReleaseEventList();
            ReleaseEvent     re  = of.createReleaseEvent();
            DefAreaElementInner areaInner = of.createDefAreaElementInner();
            Iso31661CodeList    isoList   = of.createIso31661CodeList();
            isoList.getIso31661Code().add("GB");
            areaInner.setIso31661CodeList(isoList);
            areaInner.setId("1fa8aa07-c688-1f7c-734b-4d82e528b09b");
            areaInner.setName("United Kingdom");
            areaInner.setSortName("United Kingdom");
            re.setDate("2005");
            re.setArea(areaInner);
            rel.getReleaseEvent().add(re);
            release.setReleaseEventList(rel);
            release.setDate("2005");
            release.setCountry("GB");

            doc.addField(ReleaseIndexField.BARCODE, "07599273202");
            release.setBarcode("07599273202");

            //Multiples allowed of these
            LabelInfoList labelInfoList = of.createLabelInfoList();

            {
                doc.addField(ReleaseIndexField.CATALOG_NO, "WRATHCD-25");
                doc.addField(ReleaseIndexField.LABEL, "Wrath Records");
                doc.addField(ReleaseIndexField.LABEL_ID, Index.NO_VALUE);
                LabelInfo li = of.createLabelInfo();
                Label label = of.createLabel();
                li.setLabel(label);
                li.setCatalogNumber("WRATHCD-25");
                label.setName("Wrath Records");
                labelInfoList.getLabelInfo().add(li);
            }

            {
                doc.addField(ReleaseIndexField.CATALOG_NO, "CAT WITH SPACE");
                LabelInfo li = of.createLabelInfo();
                li.setCatalogNumber("CAT WITH SPACE");
                labelInfoList.getLabelInfo().add(li);
            }

            {
                doc.addField(ReleaseIndexField.CATALOG_NO, "LP-001");
                doc.addField(ReleaseIndexField.LABEL, "Major Records");
                doc.addField(ReleaseIndexField.LABEL_ID, "c1dfaf9c-d498-4f6c-b040-f7714315fcea");
                LabelInfo li = of.createLabelInfo();
                Label label = of.createLabel();
                li.setLabel(label);
                li.setCatalogNumber("LP-001");
                label.setName("Major Records");
                label.setId("c1dfaf9c-d498-4f6c-b040-f7714315fcea");
                labelInfoList.getLabelInfo().add(li);
            }
            release.setLabelInfoList(labelInfoList);


            doc.addNumericField(ReleaseIndexField.NUM_MEDIUMS, 2);


            doc.addField(ReleaseIndexField.RELEASE_STORE, MMDSerializer.serialize(release));
            writer.addDocument(doc.getLuceneDocument());
        }

        //Another Release with Multiple Artists
        {
            MbDocument doc = new MbDocument();
            Release release = of.createRelease();
            doc.addField(ReleaseIndexField.RELEASE_ID, "0011c128-b1f2-300e-88cc-c33c30dce704");
            release.setId("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386");

            doc.addField(ReleaseIndexField.RELEASE, "Epics");

            doc.addField(ReleaseIndexField.SCRIPT, "Taml");
            doc.addField(ReleaseIndexField.LANGUAGE, "fra");
            doc.addField(ReleaseIndexField.TYPE, ReleaseGroupType.SINGLE.getName());
            doc.addField(ReleaseIndexField.ARTIST, "Erich Kunzel and Cincinnati Pops");
            doc.addField(ReleaseIndexField.ARTIST_ID, "99845d0c-f239-4051-a6b1-4b5e9f7ede0b");
            doc.addField(ReleaseIndexField.ARTIST_NAME, "Erich Kunzel");
            doc.addField(ReleaseIndexField.ARTIST_NAMECREDIT, "Erich Kunzel");

            doc.addField(ReleaseIndexField.ARTIST_ID, "d8fbd94c-cd06-4e8b-a559-761ad969d07e");
            doc.addField(ReleaseIndexField.ARTIST_NAME, "The Cincinnati Pops Orchestra");
            doc.addField(ReleaseIndexField.ARTIST_NAMECREDIT, "Cincinnati Pops");

            ArtistCredit ac = of.createArtistCredit();
            NameCredit nc = of.createNameCredit();
            Artist artist = of.createArtist();
            artist = of.createArtist();
            artist.setId("99845d0c-f239-4051-a6b1-4b5e9f7ede0b");
            artist.setName("Erich Kunzel");
            artist.setSortName("Kunzel, Eric");
            nc.setJoinphrase("and");
            nc.setArtist(artist);
            ac.getNameCredit().add(nc);

            nc = of.createNameCredit();
            artist = of.createArtist();
            artist.setId("d8fbd94c-cd06-4e8b-a559-761ad969d07e");
            artist.setName("The Cincinnati Pops Orchestra");
            artist.setSortName("Cincinnati Pops Orchestra, The");
            nc.setArtist(artist);
            nc.setName("Cincinnati Pops");
            ac.getNameCredit().add(nc);

            doc.addField(ReleaseGroupIndexField.ARTIST_CREDIT, MMDSerializer.serialize(ac));

            doc.addNumericField(ReleaseIndexField.NUM_TRACKS_MEDIUM, 14);
            doc.addNumericField(ReleaseIndexField.NUM_DISCIDS_MEDIUM, 1);
            doc.addField(ReleaseIndexField.STATUS, "Promotion");
            doc.addNumericField(ReleaseIndexField.NUM_TRACKS, 14);
            doc.addNumericField(ReleaseIndexField.NUM_DISCIDS, 1);
            doc.addField(ReleaseIndexField.FORMAT, "CD");

            doc.addField(ReleaseIndexField.COUNTRY, "US");
            doc.addField(ReleaseIndexField.DATE, "2003-09-23");
            doc.addNumericField(ReleaseIndexField.NUM_MEDIUMS, 1);
            doc.addField(ReleaseIndexField.BARCODE, ReleaseIndex.BARCODE_NONE);

            doc.addField(ReleaseIndexField.RELEASE_STORE, MMDSerializer.serialize(release));
            writer.addDocument(doc.getLuceneDocument());
        }

        {
            MbDocument doc = new MbDocument();
            doc.addField(MetaIndexField.META, MetaIndexField.META_VALUE);
            doc.addNumericField(MetaIndexField.LAST_UPDATED, new Date().getTime());
            writer.addDocument(doc.getLuceneDocument());
        }

        writer.close();
        Map<ResourceType, IndexSearcher> searchers = new HashMap<ResourceType, IndexSearcher>();
        searchers.put(ResourceType.RELEASE, new IndexSearcher(DirectoryReader.open(ramDir)));

        SearcherManager searcherManager = new SearcherManager(ramDir, new MusicBrainzSearcherFactory(ResourceType.RELEASE));
        ss = new ReleaseSearch(searcherManager);
        sd = new ReleaseDismaxSearch(ss);
    }

    protected String getReleaseId(MbDocument doc) {
        Release release = (Release) MMDSerializer.unserialize(doc.get(ReleaseIndexField.RELEASE_STORE), Release.class);
        return release.getId();
    }

    @Test
    public void testFindReleaseById() throws Exception {
        Results res = ss.search("reid:\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByName() throws Exception {
        Results res = ss.search("release:\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByDismax1() throws Exception {
        Results res = sd.search("Wrath", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByDismax2() throws Exception {
        Results res = sd.search("Farming", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByDismax3() throws Exception {
        Results res = sd.search("Incident", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByDismax3Short() throws Exception {
        Results res = sd.search("Inci", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByDismax3TooShort() throws Exception {
        Results res = sd.search("Inc", 0, 10);
        assertEquals(0, res.getTotalHits());
    }

    @Test
    public void testFindReleaseByDismax4() throws Exception {
        Results res = sd.search("Our Glorious", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByDefault() throws Exception {
        Results res = ss.search("\"Our Glorious 5 Year Plan\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindReleaseByArtistName() throws Exception {
        Results res = ss.search("artist:\"Farming Incident\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByFormat() throws Exception {
        Results res = ss.search("format:Vinyl", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindReleaseByQuality() throws Exception {
        Results res = ss.search("quality:high", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindReleaseByQuality2() throws Exception {
        Results res = ss.search("quality:HIGH", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindReleaseByQuality3() throws Exception {
        Results res = ss.search("quality:low", 0, 10);
        assertEquals(0, res.getTotalHits());
    }

    @Test
    public void testFindReleaseByCatNo() throws Exception {
        Results res = ss.search("catno:WRATHCD-25", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByCatNoWithSpaces() throws Exception {
        Results res = ss.search("catno:\"CAT WITH SPACE\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByCatNoWithSpacesIgnoringingSpaces() throws Exception {
        Results res = ss.search("catno:\"CATWITHSPACE\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByCatNoWithSpacesIgnoringingSpacesWildcard() throws Exception {
        Results res = ss.search("catno:CATWITHSPACE*", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
    @Test
    public void testFindReleaseByCatNoWithoutHyphen() throws Exception {
        Results res = ss.search("catno:WRATHCD25", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
    }
    */

    @Test
    public void testFindReleaseByCatNoAsterisk() throws Exception {
        Results res = ss.search("catno:WRATHCD-25*", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
    @Test
    public void testFindReleaseByCatNoIgnoreHypens() throws Exception {
        Results res = ss.search("catno:LP001", 0, 10);
        assertEquals(1, res.getTotalHits());
        Result result = res.results.get(0);
        MbDocument doc = result.getDoc();
        assertEquals("Our Glorious 5 Year Plan", doc.get(ReleaseIndexField.RELEASE));
        assertEquals("Wrath Records", doc.get(ReleaseIndexField.LABEL));
    }
    */
    @Test
    public void testFindReleaseByBarcodeWithoutZero() throws Exception {
        Results res = ss.search("barcode:7599273202", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByBarcodeWithZero() throws Exception {
        Results res = ss.search("barcode:07599273202", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByAsin() throws Exception {
        Results res = ss.search("asin:B00004Y6O9", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByAsinLowercase() throws Exception {
        Results res = ss.search("asin:b00004y6O9", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /**
     * Works as is even though lang code not analysed because lang code always lowercase
     *
     * @throws Exception
     */
    @Test
    public void testFindReleaseByLanguage() throws Exception {
        Results res = ss.search("lang:eng", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByPackaging() throws Exception {
        Results res = ss.search("packaging:\"jewel case\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /**
     * Works as is even though lang code not analysed because lang code always lowercase
     *
     * @throws Exception
     */
    @Test
    public void testFindReleaseByLanguageUppercase() throws Exception {
        Results res = ss.search("lang:ENG", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testFindReleaseByScript() throws Exception {
        Results res = ss.search("script:latn", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testFindReleaseByComment() throws Exception {
        Results res = ss.search("comment:demo", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testFindReleaseByScriptUppercase() throws Exception {
        Results res = ss.search("script:LATN", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
     * @throws Exception
     */
    @Test
    public void testFindReleaseByCountry() throws Exception {
        Results res = ss.search("country:gb", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
     * @throws Exception
     */
    @Test
    public void testFindReleaseWithNoBarcode() throws Exception {
        Results res = ss.search("barcode:none", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
     * @throws Exception
     */
    @Test
    public void testFindReleaseWithNotKnownBarcode() throws Exception {
        Results res = ss.search("barcode:\\-", 0, 10);
        assertEquals(0, res.getTotalHits());
    }

    /*
     * @throws Exception
     */
    @Test
    public void testFindReleaseByNumTracks() throws Exception {
        Results res = ss.search("tracks:17", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
     * @throws Exception
     */
    @Test
    public void testFindReleaseByTracksOnMedium() throws Exception {
        Results res = ss.search("tracksmedium:10", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
     * @throws Exception
     */
    @Test
    public void testFindReleaseByNumDiscOnMedium() throws Exception {
        Results res = ss.search("discidsmedium:2", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /*
     * @throws Exception
     */
    @Test
    public void testFindReleaseByNumDisc() throws Exception {
        Results res = ss.search("discids:3", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    /**
     * @throws Exception
     */
    @Test
    public void testFindReleaseByCountryUppercase() throws Exception {
        Results res = ss.search("country:GB", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }


    @Test
    public void testFindReleaseByDate() throws Exception {
        Results res = ss.search("date:2005", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByTypeLowercase() throws Exception {
        Results res = ss.search("type:\"compilation\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseBySecondaryTypeFirst() throws Exception {
        Results res = ss.search("secondarytype:\"Live\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseBySecondaryTypeSecond() throws Exception {
        Results res = ss.search("secondarytype:\"Compilation\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByTypeTitleCase() throws Exception {
        Results res = ss.search("type:\"Compilation\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByRgid() throws Exception {
        Results res = ss.search("rgid:1d9e8ed6-3893-4d3b-aa7d-6cd79609e333", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByNumericType() throws Exception {
        Results res = ss.search("type:4", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByStatusLowercase() throws Exception {
        Results res = ss.search("status:\"official\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByStatusTitleCase() throws Exception {
        Results res = ss.search("status:\"Official\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByNumericstatus() throws Exception {
        Results res = ss.search("status:1", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseGroupByArtist2() throws Exception {
        Results res = ss.search("artist:\"Erich Kunzel\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));

    }

    @Test
    public void testFindReleaseGroupByAllArtist2() throws Exception {
        Results res = ss.search("artist:\"Erich Kunzel and Cincinnati Pops\"", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));
    }

    @Test
    public void testFindReleaseByNumberofMediums() throws Exception {
        Results res = ss.search("mediums:2", 0, 10);
        assertEquals(1, res.getTotalHits());
    }

    @Test
    public void testFindReleaseByLabelId() throws Exception {
        Results res = ss.search("laid:c1dfaf9c-d498-4f6c-b040-f7714315fcea", 0, 10);
        assertEquals(1, res.getTotalHits());
    }

    @Test
    public void testNumericRangeQuery() throws Exception {
        Results res = ss.search("tracksmedium:[7 TO 17]", 0, 10);
        assertEquals(2, res.getTotalHits());
    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release/?type=xml&query=%22Our%20Glorious%205%20Year%20Plan%22
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsMmdv1Xml() throws Exception {

        Results res = ss.search("release:\"Our Glorious 5 Year Plan\"", 0, 1);
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
        assertTrue(output.contains("type=\"Compilation Official\""));
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
        assertTrue(output.contains("catalog-number=\"WRATHCD-25\""));

    }

    /**
     * Tests get same results as
     * http://musicbrainz.org/ws/1/release/?type=xml&query=%22Our%20Glorious%205%20Year%20Plan%22
     *
     * @throws Exception
     */
    @Test
    public void testOutputAsXml() throws Exception {

        Results res = ss.search("release:\"Our Glorious 5 Year Plan\"", 0, 1);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_XML, true);
        pr.close();
        String output = sw.toString();
        System.out.println("Xml is" + output);
        assertTrue(output.contains("count=\"1\""));
        assertTrue(output.contains("offset=\"0\""));
        assertTrue(output.contains("xmlns:ext=\"http://musicbrainz.org/ns/ext#-2.0\""));
        assertTrue(output.contains("id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("<language>eng</language>"));
        assertTrue(output.contains("<script>Latn</script>"));
        assertTrue(output.contains("type=\"Compilation\""));
        assertTrue(output.contains("<primary-type>Album</primary-type>"));

        assertTrue(output.contains("id=\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e333\""));
        assertTrue(output.contains("<title>Our Glorious 5 Year Plan</title>"));
        assertTrue(output.contains("<name>Farming Incident</name>"));
        assertTrue(output.contains("<sort-name>Incident, Farming</sort-name>"));
        assertTrue(output.contains("<disambiguation>demo</disambiguation>"));
        assertTrue(output.contains("artist id=\"4302e264-1cf0-4d1f-aca7-2a6f89e34b36\""));
        assertTrue(output.contains("<disc-list count=\"1\""));
        assertTrue(output.contains("<track-list count=\"10\""));
        assertTrue(output.contains("<date>2005</date>"));
        assertTrue(output.contains("<country>GB</country>"));
        assertTrue(output.contains("<area id=\"1fa8aa07-c688-1f7c-734b-4d82e528b09b\">"));
        assertTrue(output.contains("<name>United Kingdom</name>"));
        assertTrue(output.contains("<sort-name>United Kingdom</sort-name>"));
        assertTrue(output.contains("<format>Vinyl</format>"));
        assertTrue(output.contains("<packaging>Jewel Case</packaging>"));
        assertTrue(output.contains("<asin>B00004Y6O9</asin>"));
        assertTrue(output.contains("<track-count>17</track-count>"));
        assertTrue(output.contains("<name>Wrath Records</name>"));
        assertTrue(output.contains("<label id=\"c1dfaf9c-d498-4f6c-b040-f7714315fcea\">"));
        assertTrue(output.contains("<name>Major Records</name>"));
        assertTrue(output.contains("<catalog-number>WRATHCD-25</catalog-number>"));
        assertTrue(output.contains("<medium-list count=\"2\">"));
        assertTrue(output.contains("<secondary-type>Live</secondary-type>"));
        assertTrue(output.contains("<secondary-type>Compilation</secondary-type>"));
        assertTrue(output.contains("<name>punk</name>"));
    }

    @Test
    public void testOutputJson() throws Exception {

        Results res = ss.search("release:\"Our Glorious 5 Year Plan\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON);
        pr.close();

        String output = sw.toString();
        System.out.println("Json is" + output);

        assertTrue(output.contains("id\":\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"type\":\"Compilation\""));
        assertTrue(output.contains("title\":\"Our Glorious 5 Year Plan\""));
        assertTrue(output.contains("\"status\":\"Official\""));
        assertTrue(output.contains("\"language\":\"eng\""));
        assertTrue(output.contains("\"script\":\"Latn\""));
        assertTrue(output.contains("\"barcode\":\"07599273202\""));
        assertTrue(output.contains("\"asin\":\"B00004Y6O9\""));
        assertTrue(output.contains("\"track-count\":17"));
        assertTrue(output.contains("\"secondary-type-list\":{\"secondary-type\":[\"Live\",\"Compilation\"]}}"));
        assertTrue(output.contains("{\"release-event\":[{\"date\":\"2005\""));
        assertTrue(output.contains("\"name\":\"United Kingdom\","));
        assertTrue(output.contains("\"packaging\":\"Jewel Case\","));
        assertTrue(output.contains("\"sort-name\":\"United Kingdom\","));
        assertTrue(output.contains("\"id\":\"1fa8aa07-c688-1f7c-734b-4d82e528b09b\","));
    }

    @Test
    public void testOutputJsonNew() throws Exception {

        Results res = ss.search("release:\"Our Glorious 5 Year Plan\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New is" + output);
        assertTrue(output.contains("releases"));
        assertTrue(output.contains("id\":\"1d9e8ed6-3893-4d3b-aa7d-6cd79609e386\""));
        assertTrue(output.contains("title\":\"Our Glorious 5 Year Plan\""));
        assertTrue(output.contains("\"status\":\"Official\""));
        assertTrue(output.contains("\"language\":\"eng\""));
        assertTrue(output.contains("\"script\":\"Latn\""));
        assertTrue(output.contains("\"barcode\":\"07599273202\""));
        assertTrue(output.contains("\"asin\":\"B00004Y6O9\""));
        assertTrue(output.contains("\"secondary-types\":[\"Live\",\"Compilation\"]"));
        assertTrue(output.contains("\"count\":1"));
        assertTrue(output.contains("\"offset\":0,"));
        assertTrue(output.contains("\"track-count\":17"));
        assertTrue(output.contains("\"media\""));
        assertTrue(output.contains("\"disc-count\":1"));
        assertTrue(output.contains("\"track-count\":7"));
        assertTrue(output.contains("\"label-info\""));
        assertTrue(output.contains("\"catalog-number\":\"WRATHCD-25\""));
        assertTrue(output.contains("\"packaging\":\"Jewel Case\","));
        assertTrue(output.contains("\"primary-type\":\"Album\""));
        assertTrue(output.contains("\"release-events\":[{\"date\":\"2005\""));
        assertTrue(output.contains("\"secondary-types\":[\"Live\",\"Compilation\"]}"));
        assertTrue(output.contains("\"name\":\"United Kingdom\","));
        assertTrue(output.contains("\"sort-name\":\"United Kingdom\","));
        assertTrue(output.contains("\"id\":\"1fa8aa07-c688-1f7c-734b-4d82e528b09b\","));

    }

    @Test
    public void testOutputJsonNewPretty() throws Exception {

        Results res = ss.search("release:\"Our Glorious 5 Year Plan\"", 0, 10);
        ResultsWriter writer = ss.getMmd2Writer();
        StringWriter sw = new StringWriter();
        PrintWriter pr = new PrintWriter(sw);
        writer.write(pr, res, SearchServerServlet.RESPONSE_JSON_NEW, true);
        pr.close();

        String output = sw.toString();
        System.out.println("Json New  Pretty is" + output);
        assertTrue(output.contains("\"count\" : 1"));
    }


    @Test
    public void testFindReleaseByTag() throws Exception {
        Results res = ss.search("tag:punk", 0, 10);
        assertEquals(1, res.getTotalHits());
        assertEquals("1d9e8ed6-3893-4d3b-aa7d-6cd79609e386", getReleaseId(res.results.get(0).getDoc()));

    }

}
