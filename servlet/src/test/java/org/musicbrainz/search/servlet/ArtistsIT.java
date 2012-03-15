package org.musicbrainz.search.servlet;

import org.musicbrainz.mmd2.Metadata;

public class ArtistsIT extends AbstractIntegration {

    public ArtistsIT(String testName) {
        super(testName);
    }

    public void testSearchForArtist() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=artist&query=fred");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    public void testSearchForArtistV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=artist&query=fred&version=1");
        assertTrue(metadata.getArtistList().getArtist().size() > 0);
    }

    public void testSearchForArtistDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=fred");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    public void testSearchForArtistDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=love");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    public void testSearchForArtistDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=the");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    public void testSearchForArtistDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=love+rocket");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }
}
