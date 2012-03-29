package org.musicbrainz.search.servlet;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;
import org.musicbrainz.mmd2.Metadata;
import org.musicbrainz.mmd2.Recording;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class LoadIT extends AbstractIntegration {

    static AtomicLong totalMatches   = new AtomicLong(0);
    static AtomicLong totalReleases  = new AtomicLong(0);

    static AtomicLong totalTime      = new AtomicLong(0);
    static AtomicLong totalData      = new AtomicLong(0);
    
    final static int  PARALLELISM   = 20;
    final static int  NO_OF_QUERIES = 200;

    public LoadIT() {
        super();
    }

    /**
     * Run test three times so can check results are consistent and compare performance of cold to warmed up server
     *
     * Modify PARALLELISM to put different load on search server
     *
     * Modify NO_OF_QUERIES to change no of searches performed
     *
     * @throws Exception
     */

    @Test
    public void testSearchMultipleRecordingSearchesinParallel() throws Exception {

        //20 Queries in parallel
        ExecutorService es = Executors.newFixedThreadPool(PARALLELISM);
        for(int i=1;i<=NO_OF_QUERIES;i++)
        {
            es.submit(new RunSearch(i));
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);
        System.out.println("Run1:Time:" + totalTime
                + ":Matches:" + totalMatches
                + ":Releases in top25:" + totalReleases
                + ":Data:" + totalData);

        totalMatches.set(0);
        totalReleases.set(0);
        totalTime.set(0);
        totalData.set(0);

        //20 Queries in parallel
        es = Executors.newFixedThreadPool(PARALLELISM);
        for(int i=1;i<=NO_OF_QUERIES;i++)
        {
            es.submit(new RunSearch(i));
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);

        System.out.println("Run2:Time:" + totalTime
                + ":Matches:" + totalMatches
                + ":Releases in top25:" + totalReleases
                + ":Data:" + totalData);

        totalMatches.set(0);
        totalReleases.set(0);
        totalTime.set(0);
        totalData.set(0);

        //20 Queries in parallel
        es = Executors.newFixedThreadPool(PARALLELISM);
        for(int i=1;i<=NO_OF_QUERIES;i++)
        {
            es.submit(new RunSearch(i));
        }
        es.shutdown();
        es.awaitTermination(1, TimeUnit.HOURS);

        System.out.println("Run3:Time:" + totalTime
                + ":Matches:" + totalMatches
                + ":Releases in top25:" + totalReleases
                + ":Data:" + totalData);

    }

    class RunSearch implements Callable<Boolean> {
        
        private int qdur;
        public RunSearch(int qdur) {
            this.qdur=qdur;
        }

        public Boolean call() throws Exception {
            Metadata metadata;
            StopWatch clock = new StopWatch();
            clock.start();
            BufferedInputStream bis;
            URL url = new URL("http://localhost:8080/?type=recording&query=qdur:"+qdur);
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            int responseCode = uc.getResponseCode();
            assertEquals(responseCode,HttpURLConnection.HTTP_OK);
            bis = new BufferedInputStream(uc.getInputStream());
            totalData.getAndAdd(bis.available());

            metadata = (Metadata) context.createUnmarshaller().unmarshal(bis);
            clock.stop();
            assertTrue(clock.getTime() < 5000);
            totalTime.getAndAdd(clock.getTime());

            assertTrue(metadata.getRecordingList().getRecording().size()>0);
            totalMatches.getAndAdd(metadata.getRecordingList().getCount().intValue());

            int releases = 0;
            for(Recording recording:metadata.getRecordingList().getRecording())
            {
                releases+=recording.getReleaseList().getRelease().size();
            }
            totalReleases.getAndAdd(releases);
            return true;
        }

    }


}