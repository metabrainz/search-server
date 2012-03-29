package org.musicbrainz.search.servlet;

import org.junit.Test;
import org.musicbrainz.mmd2.Metadata;

import static org.junit.Assert.assertTrue;


public class WorkIT extends AbstractIntegration {

    public WorkIT() {
        super();
    }

    @Test
    public void testSearchForWork() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=work&query=fred");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    @Test
    public void testSearchForWorkDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=fred");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    @Test
    public void testSearchForWorkDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=love");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    @Test
    public void testSearchForWorkDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=the");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    @Test
    public void testSearchForWorkDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=love+rocket");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }
}