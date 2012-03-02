package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;


public class RecordingIT extends AbstractIntegration {

    public RecordingIT(String testName) {
        super(testName);
    }

    public void testSearchForRecording() throws Exception {
        StopWatch clock = new StopWatch();
        clock.start();
        Metadata metadata = doSearch("http://localhost:8080/?type=recording&query=fred");
        clock.stop();
        System.out.println(this.getName()+":"+clock.getTime());
        assertTrue(clock.getTime() < 5000);
        assertTrue(metadata.getRecordingList().getRecording().size()>0);
    }
}