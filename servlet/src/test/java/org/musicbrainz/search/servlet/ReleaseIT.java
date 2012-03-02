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
}