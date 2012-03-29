package org.musicbrainz.search.servlet;

import org.junit.Test;
import org.musicbrainz.mmd2.Metadata;

import static org.junit.Assert.assertTrue;

public class ReleaseIT extends AbstractIntegration {

    public ReleaseIT() {
        super();
    }

    @Test
    public void testSearchForRelease() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=release&query=fred");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    @Test
    public void testSearchForReleaseV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=release&query=fred&version=1");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    @Test
    public void testSearchForReleaseDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=fred");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    @Test
    public void testSearchForReleaseDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=love");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    @Test
    public void testSearchForReleaseDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=the");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }

    @Test
    public void testSearchForReleaseDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=release&query=love+rocket");
        assertTrue(metadata.getReleaseList().getRelease().size()>0);
    }
}