package org.musicbrainz.search.servlet.mmd2;

import org.musicbrainz.mmd2.EntityList;
import org.musicbrainz.mmd2.Metadata;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Take the output from multiple results sets and merged into single output
 *
 */
public class AllWriter extends ResultsWriter {

    Results artistResults;
    Results releaseResults;
    Results releaseGroupResults;
    Results labelResults;
    Results recordingResults;
    Results workResults;

    public AllWriter(Results artistResults,
                     Results releaseResults,
                     Results releaseGroupResults,
                     Results labelResults,
                     Results recordingResults,
                     Results workResults) {
        this.artistResults=artistResults;
        this.releaseResults=releaseResults;
        this.releaseGroupResults=releaseGroupResults;
        this.labelResults=labelResults;
        this.recordingResults=recordingResults;
        this.workResults=workResults;
    }

    //TODO we dont need this method but have to put in because need to subclass from ReleaseWriter
    public void write(Metadata metadata, Results results) throws IOException {
    }


    public Metadata write(Results results) throws IOException {

        List<Result> allResults = new ArrayList<Result>();

        //Sort by best max score, then set this as the max score for each entity
        List<Results> resultsList = new ArrayList<Results>();
        resultsList.add(artistResults);
        resultsList.add(releaseResults);
        resultsList.add(releaseGroupResults);
        resultsList.add(labelResults);
        resultsList.add(recordingResults);
        resultsList.add(workResults);

        //Find the best score
        Collections.sort(resultsList);
        Collections.reverse(resultsList);
        float bestMaxScore= resultsList.get(0).getMaxScore();
        for(Results next:resultsList)
        {
            next.setMaxScore(bestMaxScore);
            //Calculate Normalized Scores and set resourcetype
            for(Result nextResult:next.results)
            {
                nextResult.setNormalizedScore(bestMaxScore);
                nextResult.setResourceType(next.getResourceType());
            }
            allResults.addAll(next.results);
        }
        //Now sort merged results
        Collections.sort(allResults);
        Collections.reverse(allResults);

        //Create entitylist stuff
        ObjectFactory of  = new ObjectFactory();
        Metadata metadata       = of.createMetadata();
        EntityList entityList   = of.createEntityList();
        metadata.setEntityList(entityList);

        ArtistWriter artistWriter = new ArtistWriter();
        ReleaseWriter releaseWriter = new ReleaseWriter();
        ReleaseGroupWriter releaseGroupWriter = new ReleaseGroupWriter();
        LabelWriter labelWriter = new LabelWriter();
        RecordingWriter recordingWriter = new RecordingWriter();
        WorkWriter workWriter = new WorkWriter();

        //Now use the correct writer to write the result
        for(Result result:allResults)
        {
            switch(result.getResourceType())
            {
                case ARTIST:
                    artistWriter.write(entityList.getArtistAndReleaseAndReleaseGroup(), result);
                    break;

                case LABEL:
                    labelWriter.write(entityList.getArtistAndReleaseAndReleaseGroup(), result);
                    break;

                case RELEASE:
                    releaseWriter.write(entityList.getArtistAndReleaseAndReleaseGroup(), result);
                    break;

                case RELEASE_GROUP:
                    releaseGroupWriter.write(entityList.getArtistAndReleaseAndReleaseGroup(), result);
                    break;

                case RECORDING:
                    recordingWriter.write(entityList.getArtistAndReleaseAndReleaseGroup(), result);
                    break;

                case WORK:
                    workWriter.write(entityList.getArtistAndReleaseAndReleaseGroup(), result);
                    break;

            }
        }

        //Then write total matches
        int totalHits=0;
        int offset= resultsList.get(0).getOffset();
        for(Results next:resultsList)
        {
            totalHits+= next.getTotalHits();
        }
        entityList.setCount(BigInteger.valueOf(totalHits));
        entityList.setOffset(BigInteger.valueOf(offset));

        return metadata;
    }
}
