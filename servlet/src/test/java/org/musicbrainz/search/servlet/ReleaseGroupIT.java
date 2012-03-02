package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;


public class ReleaseGroupIT extends AbstractIntegration {

    public ReleaseGroupIT(String testName) {
        super(testName);
    }

    public void testSearchForReleaseGroup() throws Exception {
        StopWatch clock = new StopWatch();
        clock.start();
        Metadata metadata = doSearch("http://localhost:8080/?type=releasegroup&query=fred");
        clock.stop();
        System.out.println(this.getName()+":"+clock.getTime());
        assertTrue(clock.getTime() < 5000);
        assertTrue(metadata.getReleaseGroupList().getReleaseGroup().size()>0);
    }
}