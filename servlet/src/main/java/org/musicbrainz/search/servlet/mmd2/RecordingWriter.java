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

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.NumericUtils;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.ArtistCreditHelper;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

public class RecordingWriter extends ResultsWriter {

    private static final String VARIOUS_ARTISTS_GUID = "89ad4ac3-39f7-470e-963a-56509c546377";
    private static final String VARIOUS_ARTISTS_NAME = "Various Artists";

    /**
     * Create various artist credits
     *
     * @return
     */
    private ArtistCredit createVariousArtistsCredit()
    {
        ObjectFactory of = new ObjectFactory();
        Artist       artist   = of.createArtist();
        artist.setId(VARIOUS_ARTISTS_GUID);
        artist.setName(VARIOUS_ARTISTS_NAME);
        NameCredit   naCredit = of.createNameCredit();
        naCredit.setArtist(artist);
        ArtistCredit vaCredit = of.createArtistCredit();
        vaCredit.getNameCredit().add(naCredit);
        return vaCredit;
    }


    public void write(Metadata metadata, Results results) throws IOException
    {
        ObjectFactory of = new ObjectFactory();
        RecordingList recordingList = of.createRecordingList();

        for(Result result:results.results)
        {
            result.setNormalizedScore(results.getMaxScore());
        }
        write(recordingList.getRecording(), results);

        recordingList.setCount(BigInteger.valueOf(results.getTotalHits()));
        recordingList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setRecordingList(recordingList);
    }
    public void write(List list, Results results) throws IOException
    {
        for (Result result : results.results)
        {
            write(list, result);
        }
    }

    public void write(List list, Result result) throws IOException
    {
        ObjectFactory of = new ObjectFactory();
        RecordingList recordingList = of.createRecordingList();

            MbDocument doc = result.getDoc();
            Recording recording = of.createRecording();

            recording.setId(doc.get(RecordingIndexField.RECORDING_ID));
            recording.setScore(String.valueOf(result.getNormalizedScore()));
            String name = doc.get(RecordingIndexField.RECORDING_OUTPUT);

            if (name != null) {
                recording.setTitle(name);
            }

            String comment = doc.get(RecordingIndexField.COMMENT);
            if (isNotNoValue(comment)) {
                recording.setDisambiguation(comment);
            }

            String duration = doc.get(RecordingIndexField.RECORDING_DURATION_OUTPUT);
            if (duration != null) {
                recording.setLength(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(duration))));
            }

            String[] isrcs              = doc.getValues(RecordingIndexField.ISRC);
            if(isrcs.length>0) {
                IsrcList isrcList = of.createIsrcList();
                for (int i = 0; i < isrcs.length; i++) {
                    Isrc isrc = of.createIsrc();
                    isrc.setId(isrcs[i]);
                    isrcList.getIsrc().add(isrc);
                }
                recording.setIsrcList(isrcList);
            }

            String[] puids              = doc.getValues(RecordingIndexField.PUID);
            if(puids.length>0) {
                PuidList puidList = of.createPuidList();
                for (int i = 0; i < puids.length; i++) {
                    Puid puid = of.createPuid();
                    puid.setId(puids[i]);
                    puidList.getPuid().add(puid);
                }
                recording.setPuidList(puidList);
            }

            if(doc.get(RecordingIndexField.ARTIST_CREDIT)!=null) {
                ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(RecordingIndexField.ARTIST_CREDIT));
                recording.setArtistCredit(ac);
            }

            String[] releaseNames       = doc.getValues(RecordingIndexField.RELEASE);

            if(releaseNames.length>0)
            {
                String[] rgTypes                = doc.getValues(RecordingIndexField.RELEASE_TYPE);
                String[] rgIds                  = doc.getValues(RecordingIndexField.RELEASEGROUP_ID);
                String[] rgPrimaryTypes         = doc.getValues(RecordingIndexField.RELEASE_PRIMARY_TYPE);
                String[] rgSecondaryTypes       = doc.getValues(RecordingIndexField.SECONDARY_TYPE_OUTPUT);
                String[] releaseIds             = doc.getValues(RecordingIndexField.RELEASE_ID);
                String[] releaseStatus          = doc.getValues(RecordingIndexField.RELEASE_STATUS);
                String[] releaseCountry         = doc.getValues(RecordingIndexField.COUNTRY);
                String[] releaseDate            = doc.getValues(RecordingIndexField.RELEASE_DATE);
                String[] trackPos               = doc.getValues(RecordingIndexField.TRACKNUM);
                String[] trackNos               = doc.getValues(RecordingIndexField.NUMBER);
                String[] numTracks              = doc.getValues(RecordingIndexField.NUM_TRACKS);
                String[] trackName              = doc.getValues(RecordingIndexField.TRACK_OUTPUT);
                String[] mediumPos              = doc.getValues(RecordingIndexField.POSITION);
                String[] numTracksRelease       = doc.getValues(RecordingIndexField.NUM_TRACKS_RELEASE);
                String[] releaseVA              = doc.getValues(RecordingIndexField.RELEASE_AC_VA);
                String[] mediumFormat           = doc.getValues(RecordingIndexField.FORMAT);
                String[] trackArtistCredits     = doc.getValues(RecordingIndexField.TRACK_ARTIST_CREDIT);
                String[] trackDurations         = doc.getValues(RecordingIndexField.TRACK_DURATION_OUTPUT);

                ReleaseList releaseList = of.createReleaseList();
                for(int i=0;i<releaseNames.length;i++) {

                    Release release = of.createRelease();
                    release.setId(releaseIds[i]);
                    release.setTitle(releaseNames[i]);
                    if (isNotNoValue(releaseStatus[i])) {
                        release.setStatus(releaseStatus[i]);
                    }

                    if (isNotNoValue(releaseDate[i])) {
                        release.setDate(releaseDate[i].toLowerCase(Locale.US));
                    }

                    if (isNotNoValue(releaseCountry[i])) {
                        release.setCountry(releaseCountry[i]);
                    }

                    if (isNotNoValue(releaseVA[i])) {
                        release.setArtistCredit(createVariousArtistsCredit());
                    }

                    ReleaseGroup rg = of.createReleaseGroup();
                    release.setReleaseGroup(rg);
                    if (isNotUnknown(rgIds[i])) {
                        rg.setId(rgIds[i]);
                    }

                    if (isNotNoValue(rgTypes[i])) {
                        rg.setType(rgTypes[i]);
                    }

                    if(isNotUnknown(rgPrimaryTypes[i])) {
                        rg.setPrimaryType(rgPrimaryTypes[i]);
                    }

                    if(isNotNoValue(rgSecondaryTypes[i])) {
                        SecondaryTypeList stl = (SecondaryTypeList)MMDSerializer
                                .unserialize(rgSecondaryTypes[i],SecondaryTypeList.class);
                        release.getReleaseGroup().setSecondaryTypeList(stl);
                    }

                    org.musicbrainz.mmd2.Medium.TrackList.Track track = of.createMediumTrackListTrack();
                    track.setTitle(trackName[i]);

                    if (isNotNoValue(trackNos[i])) {
                        track.setNumber(trackNos[i]);
                    }

                    if (isNotNoValue(trackDurations[i])) {
                        track.setLength(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(trackDurations[i]))));
                    }

                    if (isNotNoValue(trackArtistCredits[i])) {
                        ArtistCredit tac = ArtistCreditHelper.unserialize(trackArtistCredits[i]);
                        track.setArtistCredit(tac);
                    }

                    org.musicbrainz.mmd2.Medium.TrackList releaseTrackList = of.createMediumTrackList();
                    releaseTrackList.setOffset(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(trackPos[i])) - 1));
                    releaseTrackList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(numTracks[i]))));
                    releaseTrackList.getDefTrack().add(track);
                    Medium medium = of.createMedium();
                    medium.setPosition(new BigInteger(mediumPos[i]));
                    medium.setTrackList(releaseTrackList);
                    if(isNotNoValue(mediumFormat[i]))
                    {
                        medium.setFormat(mediumFormat[i]);
                    }
                    MediumList mediumList = of.createMediumList();
                    mediumList.setTrackCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(numTracksRelease[i]))));
                    mediumList.getMedium().add(medium);
                    release.setMediumList(mediumList);
                    releaseList.getRelease().add(release);
                }
                recording.setReleaseList(releaseList);
            }

            String[] tags       = doc.getValues(RecordingIndexField.TAG);
            String[] tagCounts  = doc.getValues(RecordingIndexField.TAGCOUNT);
            if(tags.length>0)
            {
                TagList tagList = of.createTagList();
                for(int i = 0;i<tags.length;i++) {
                    Tag tag = of.createTag();
                    tag.setName(tags[i]);
                    tag.setCount(new BigInteger(tagCounts[i]));
                    tagList.getTag().add(tag);
                }
                recording.setTagList(tagList);
            }

            list.add(recording);
    }
}