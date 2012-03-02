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
}