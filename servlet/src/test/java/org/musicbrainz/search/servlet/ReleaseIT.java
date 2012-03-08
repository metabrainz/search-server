package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;


public class ReleaseIT extends AbstractIntegration {

    public ReleaseIT(String testName) {
        super(testName);
    }

    public void testSearchForRelease() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=release&query=fred");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    public void testSearchForReleaseV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=release&query=fred&version=1");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    public void testSearchForReleaseDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=fred");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    public void testSearchForReleaseDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=love");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    public void testSearchForReleaseDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=the");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    public void testSearchForReleaseDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=love+rocket");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }
}