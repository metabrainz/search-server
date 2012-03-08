package org.musicbrainz.search.servlet;

import org.musicbrainz.mmd2.Metadata;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class LoadIT extends AbstractIntegration {

    public LoadIT(String testName) {
        super(testName);
    }

    public void testSearchMultipleRecordingSearchesinParallel() throws Exception {
        //20 Queries in parallel
        ExecutorService es = Executors.newFixedThreadPool(20);
        for(int i=1;i<200;i++)
        {
            es.submit(new RunSearch(i));
        }
    }

    class RunSearch implements Callable<Boolean> {
        private int qdur;
        public RunSearch(int qdur) {
            this.qdur=qdur;
        }

        public Boolean call() throws Exception {
            Metadata metadata = doSearch("http://localhost:8080/?type=recording&query=qdur:"+qdur);
            assertTrue(metadata.getRecordingList().getRecording().size()>0);
            System.out.println(qdur+":"+metadata.getRecordingList().getCount());
            return true;
        }

    }


}