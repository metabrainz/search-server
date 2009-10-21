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
import org.apache.commons.lang.StringUtils;
import org.musicbrainz.search.index.TrackIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.MbDocument;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.mmd2.*;

import java.io.IOException;
import java.math.BigInteger;

public class TrackXmlWriter extends XmlWriter {

    public Metadata write(Results results) throws IOException {


        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        RecordingList recordingList = of.createRecordingList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Recording recording = of.createRecording();

            recording.setId(doc.get(TrackIndexField.TRACK_ID));

            recording.getOtherAttributes().put(getScore(), String.valueOf((int) (result.score * 100)));

            String name = doc.get(TrackIndexField.TRACK);

            if (name != null) {
                recording.setTitle(name);
            }

            String duration = doc.get(TrackIndexField.DURATION);
            if (duration != null) {
                recording.setLength(BigInteger.valueOf(NumericUtils.prefixCodedToInt(duration)));
            }

            String[] artistIds          = doc.getValues(TrackIndexField.ARTIST_ID);
            String[] artistNames        = doc.getValues(TrackIndexField.ARTIST_NAME);
            String[] artistJoinPhrases  = doc.getValues(TrackIndexField.ARTIST_JOINPHRASE);
            String[] artistSortNames    = doc.getValues(TrackIndexField.ARTIST_SORTNAME);
            String[] artistCreditNames  = doc.getValues(TrackIndexField.ARTIST_NAMECREDIT);

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

            String releaseName = doc.get(TrackIndexField.RELEASE);
            if (releaseName != null) {
                Release release = of.createRelease();
                release.setId(doc.get(TrackIndexField.RELEASE_ID));
                release.setTitle(releaseName);

                String type = doc.get(TrackIndexField.RELEASE_TYPE);
                ReleaseGroup rg = of.createReleaseGroup();
                release.setReleaseGroup(rg);
                if (type != null) {
                    release.getReleaseGroup().getType().add(StringUtils.capitalize(type));
                }


                /* TODO not supported in MMD
                String trackNo = doc.get(TrackIndexField.TRACKNUM);
                String tracks = doc.get(TrackIndexField.NUM_TRACKS);
                if (trackNo != null) {
                    TrackList releaseTrackList = of.createTrackList();
                    releaseTrackList.setOffset(BigInteger.valueOf(NumericUtils.prefixCodedToInt(trackNo) - 1));
                    if (tracks != null) {
                        releaseTrackList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(tracks)));
                    }
                    release.setTrackList(releaseTrackList);

                }
                ReleaseList releaseList = of.createReleaseList();
                releaseList.getRelease().add(release);
                recording.setReleaseList(releaseList);
                */

                //TODO dosnt exist in MMD
                //recording.setRelease()

            }
            recordingList.getRecording().add(recording);
        }
        recordingList.setCount(BigInteger.valueOf(results.totalHits));
        recordingList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setRecordingList(recordingList);
        return metadata;
    }



}