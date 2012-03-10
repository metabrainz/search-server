package org.musicbrainz.search.servlet;

import org.musicbrainz.mmd2.Metadata;


public class WorkIT extends AbstractIntegration {

    public WorkIT(String testName) {
        super(testName);
    }

    public void testSearchForWork() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=work&query=fred");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    public void testSearchForWorkDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=fred");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    public void testSearchForWorkDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=love");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    public void testSearchForWorkDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=the");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }

    public void testSearchForWorkDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=work&query=love+rocket");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }
}