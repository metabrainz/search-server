/* Copyright (c) 2009 Lukas Lalinsky
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the MusicBrainz project nor the names of the
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.servlet.mmd2;

import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.mmd2.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class RecordingXmlWriter extends XmlWriter {

    public Metadata write(Results results) throws IOException {


        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        RecordingList recordingList = of.createRecordingList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Recording recording = of.createRecording();

            recording.setId(doc.get(RecordingIndexField.RECORDING_ID));

            recording.getOtherAttributes().put(getScore(), String.valueOf((int) (result.score * 100)));

            String name = doc.get(RecordingIndexField.RECORDING);

            if (name != null) {
                recording.setTitle(name);
            }

            String duration = doc.get(RecordingIndexField.DURATION);
            if (duration != null) {
                recording.setLength(BigInteger.valueOf(NumericUtils.prefixCodedToInt(duration)));
            }

            String[] artistIds          = doc.getValues(RecordingIndexField.ARTIST_ID);
            String[] artistNames        = doc.getValues(RecordingIndexField.ARTIST_NAME);
            String[] artistJoinPhrases  = doc.getValues(RecordingIndexField.ARTIST_JOINPHRASE);
            String[] artistSortNames    = doc.getValues(RecordingIndexField.ARTIST_SORTNAME);
            String[] artistCreditNames  = doc.getValues(RecordingIndexField.ARTIST_NAMECREDIT);

            ArtistCredit ac = of.createArtistCredit();
            for (int i = 0; i < artistIds.length; i++) {

                Artist     artist   = of.createArtist();
                artist.setId(artistIds[i]);
                artist.setName(artistNames[i]);
                artist.setSortName(artistSortNames[i]);
                NameCredit nc = of.createNameCredit();
                nc.setArtist(artist);
                if(!artistJoinPhrases[i].equals("-")) {
                    nc.setJoinphrase(artistJoinPhrases[i]);
                }
                if(!artistCreditNames[i].equals(artistNames[i])) {
                    nc.setName(artistCreditNames[i]);
                }
                ac.getNameCredit().add(nc);
                recording.setArtistCredit(ac);
            }

            String[] releaseNames  = doc.getValues(RecordingIndexField.RELEASE);
            String[] releaseIds    = doc.getValues(RecordingIndexField.RELEASE_ID);
            String[] releaseTypes  = doc.getValues(RecordingIndexField.RELEASE_TYPE);
            String[] trackNos      = doc.getValues(RecordingIndexField.TRACKNUM);
            String[] numTracks     = doc.getValues(RecordingIndexField.NUM_TRACKS);

            if(releaseNames.length>0)
            {
                ReleaseList releaseList = of.createReleaseList();
                for(int i=0;i<releaseNames.length;i++) {

                    Release release = of.createRelease();
                    release.setId(releaseIds[i]);
                    release.setTitle(releaseNames[i]);

                    ReleaseGroup rg = of.createReleaseGroup();
                    release.setReleaseGroup(rg);
                    if (!releaseTypes[i].equals("-")) {
                        release.getReleaseGroup().getType().add(releaseTypes[i].toLowerCase(Locale.US));
                    }

                    TrackList releaseTrackList = of.createTrackList();
                    releaseTrackList.setOffset(BigInteger.valueOf(NumericUtils.prefixCodedToInt(trackNos[i]) - 1));
                    releaseTrackList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numTracks[i])));
                    Medium medium = of.createMedium();
                    medium.setTrackList(releaseTrackList);

                    MediumList mediumList = of.createMediumList();
                    mediumList.getMedium().add(medium);
                    release.setMediumList(mediumList);
                    releaseList.getRelease().add(release);
                }
                recording.setReleaseList(releaseList);
            }
            recordingList.getRecording().add(recording);
        }
        recordingList.setCount(BigInteger.valueOf(results.totalHits));
        recordingList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setRecordingList(recordingList);
        return metadata;
    }



}