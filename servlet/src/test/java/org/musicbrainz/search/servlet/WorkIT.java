package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;


public class WorkIT extends AbstractIntegration {

    public WorkIT(String testName) {
        super(testName);
    }

    public void testSearchForWork() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=work&query=fred");
        assertTrue(metadata.getWorkList().getWork().size()>0);
    }
}