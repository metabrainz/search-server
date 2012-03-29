package org.musicbrainz.search.servlet;

import org.junit.Test;
import org.musicbrainz.mmd2.Metadata;

import static org.junit.Assert.assertTrue;

public class ReleaseGroupIT extends AbstractIntegration {

    public ReleaseGroupIT() {
        super();
    }

    @Test
    public void testSearchForReleaseGroup() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=release-group&query=fred");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    @Test
    public void testSearchForReleaseGroupV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=release-group&query=fred&version=1");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    @Test
    public void testSearchForReleaseGroupDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=fred");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    @Test
    public void testSearchForReleaseGroupDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=love");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    @Test
    public void testSearchForReleaseGroupDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=the");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }

    @Test
    public void testSearchForReleaseGroupDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release-group&query=love+rocket");
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }
}