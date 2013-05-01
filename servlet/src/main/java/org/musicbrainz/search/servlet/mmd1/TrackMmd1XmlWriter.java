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

package org.musicbrainz.search.servlet.mmd1;

import com.google.common.base.Strings;
import com.jthink.brainz.mmd.*;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.mmd2.ArtistCredit;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.ArtistCreditHelper;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;

public class TrackMmd1XmlWriter extends Mmd1XmlWriter {

    public Metadata write(Results results) throws IOException {


        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        TrackList trackList = of.createTrackList();

        for (Result result : results.results) {
            MbDocument doc = result.getDoc();
            Track track = of.createTrack();
            result.setNormalizedScore(results.getMaxScore());
            track.getOtherAttributes().put(getScore(), String.valueOf(result.getNormalizedScore()));


            org.musicbrainz.mmd2.Recording recordingv2
                    = (org.musicbrainz.mmd2.Recording) MMDSerializer.unserialize(doc.get(RecordingIndexField.RECORDING_STORE), org.musicbrainz.mmd2.Recording.class);

            track.setId(recordingv2.getId());

            if (!Strings.isNullOrEmpty(recordingv2.getTitle())) {
                track.setTitle(recordingv2.getTitle());
            }

            if (recordingv2.getLength()!=null) {
                track.setDuration(recordingv2.getLength());
            }

            ArtistCredit acv2 = recordingv2.getArtistCredit();
            if(acv2!=null) {
                if (acv2.getNameCredit().size() > 0) {
                    Artist artist = of.createArtist();
                    artist.setName(acv2.getNameCredit().get(0).getArtist().getName());
                    artist.setId(acv2.getNameCredit().get(0).getArtist().getId());
                    artist.setSortName(acv2.getNameCredit().get(0).getArtist().getSortName());
                    track.setArtist(artist);
                }
            }

            if(recordingv2.getReleaseList()!=null) {
                ReleaseList releaseList = of.createReleaseList();
                for(org.musicbrainz.mmd2.Release releasev2:recordingv2.getReleaseList().getRelease()) {
                    Release release = of.createRelease();
                    release.setId(releasev2.getId());
                    release.setTitle(releasev2.getTitle());
                    release.getType().add(StringUtils.capitalize(releasev2.getReleaseGroup().getType()));

                    TrackList releaseTrackList = of.createTrackList();
                    releaseTrackList.setOffset(releasev2.getMediumList().getMedium().get(0).getTrackList().getOffset());
                    releaseTrackList.setCount(releasev2.getMediumList().getMedium().get(0).getTrackList().getCount());
                    release.setTrackList(releaseTrackList);
                    releaseList.getRelease().add(release);
                }
                track.setReleaseList(releaseList);
                trackList.getTrack().add(track);
            }
        }
        trackList.setCount(BigInteger.valueOf(results.getTotalHits()));
        trackList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setTrackList(trackList);
        return metadata;
    }



}
