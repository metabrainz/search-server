package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;


public class ReleaseGroupIT extends AbstractIntegration {

    public ReleaseGroupIT(String testName) {
        super(testName);
    }

    public void testSearchForReleaseGroup() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=release-group&query=fred");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    public void testSearchForReleaseGroupV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=release-group&query=fred&version=1");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    public void testSearchForReleaseGroupDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=fred");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    public void testSearchForReleaseGroupDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=love");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    public void testSearchForReleaseGroupDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=the");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    public void testSearchForReleaseGroupDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=love+rocket");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }
}