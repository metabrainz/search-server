package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;


public class LabelIT extends AbstractIntegration {


    public LabelIT(String testName) {
        super(testName);
    }

    public void testSearchForLabel() throws Exception {
        Metadata metadata = doSearch("http://localhost:8080/?type=label&query=fred");
        assertTrue(metadata.getLabelList().getLabel().size()>0);
    }
}