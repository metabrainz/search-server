package org.musicbrainz.search.servlet;

import org.junit.Test;
import org.musicbrainz.mmd2.Metadata;

import static org.junit.Assert.assertTrue;

public class ArtistsIT extends AbstractIntegration {

    public ArtistsIT() {
        super();
    }


    @Test
    public void testSearchForArtist() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=artist&query=fred");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    @Test
    public void testSearchForArtistV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=artist&query=fred&version=1");
        assertTrue(metadata.getArtistList().getArtist().size() > 0);
    }

    @Test
    public void testSearchForArtistDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=fred");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    @Test
    public void testSearchForArtistDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=love");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    @Test
    public void testSearchForArtistDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=the");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }

    @Test
    public void testSearchForArtistDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=artist&query=love+rocket");
        assertTrue(metadata.getArtistList().getArtist().size()>0);
    }
}
