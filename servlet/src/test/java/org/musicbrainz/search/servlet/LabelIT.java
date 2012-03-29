package org.musicbrainz.search.servlet;

import org.junit.Test;
import org.musicbrainz.mmd2.Metadata;

import static org.junit.Assert.assertTrue;

public class LabelIT extends AbstractIntegration {


    public LabelIT() {
        super();
    }

    @Test
    public void testSearchForLabel() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=label&query=fred");
        assertTrue(metadata.getLabelList().getLabel().size()>0);
    }

    @Test
    public void testSearchForLabelV1() throws Exception {
        com.jthink.brainz.mmd.Metadata metadata = doSearchV1("http://localhost:8080/?type=label&query=fred&version=1");
        assertTrue(metadata.getLabelList().getLabel().size()>0);
    }

    @Test
    public void testSearchForLabelDismax() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=label&query=fred");
        assertTrue(metadata.getLabelList().getLabel().size()>0);
    }

    @Test
    public void testSearchForLabelDismaxPopularTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=label&query=love");
        assertTrue(metadata.getLabelList().getLabel().size()>0);
    }

    /** Will not do fuzzy because term length to short
     *
     * @throws Exception
     */
    @Test
    public void testSearchForLabelDismaxNoFuzzy() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=label&query=the");
        assertTrue(metadata.getLabelList().getLabel().size()>0);
    }

    @Test
    public void testSearchForLabelDismaxMultiTerm() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?dismax=true&type=label&query=love+rocket");
        assertTrue(metadata.getLabelList().getLabel().size()>0);
    }
}