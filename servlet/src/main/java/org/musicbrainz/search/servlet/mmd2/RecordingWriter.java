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
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.ArtistCreditHelper;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
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

    public Metadata write(Results results) throws IOException {


        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        RecordingList recordingList = of.createRecordingList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Recording recording = of.createRecording();

            recording.setId(doc.get(RecordingIndexField.RECORDING_ID));
            recording.setScore(String.valueOf((int)(result.score * 100)));
            String name = doc.get(RecordingIndexField.RECORDING_OUTPUT);

            if (name != null) {
                recording.setTitle(name);
            }

            String comment = doc.get(RecordingIndexField.COMMENT);
            if (comment != null) {
                recording.setDisambiguation(comment);
            }

            String duration = doc.get(RecordingIndexField.DURATION);
            if (duration != null) {
                recording.setLength(BigInteger.valueOf(NumericUtils.prefixCodedToInt(duration)));
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
                String[] releaseTypes       = doc.getValues(RecordingIndexField.RELEASE_TYPE);
                String[] releaseIds         = doc.getValues(RecordingIndexField.RELEASE_ID);
                String[] releaseStatus      = doc.getValues(RecordingIndexField.RELEASE_STATUS);
                String[] releaseCountry     = doc.getValues(RecordingIndexField.COUNTRY);
                String[] releaseDate        = doc.getValues(RecordingIndexField.RELEASE_DATE);
                String[] trackNos           = doc.getValues(RecordingIndexField.TRACKNUM);
                String[] numTracks          = doc.getValues(RecordingIndexField.NUM_TRACKS);
                String[] trackName          = doc.getValues(RecordingIndexField.TRACK_OUTPUT);
                String[] mediumPos          = doc.getValues(RecordingIndexField.POSITION);
                String[] numTracksRelease   = doc.getValues(RecordingIndexField.NUM_TRACKS_RELEASE);
                String[] releaseVA          = doc.getValues(RecordingIndexField.RELEASE_AC_VA);
                String[] mediumFormat       = doc.getValues(RecordingIndexField.FORMAT);
                String[] trackArtistCredits = doc.getValues(RecordingIndexField.TRACK_ARTIST_CREDIT);

                ReleaseList releaseList = of.createReleaseList();
                for(int i=0;i<releaseNames.length;i++) {

                    Release release = of.createRelease();
                    release.setId(releaseIds[i]);
                    release.setTitle(releaseNames[i]);
                    if (!releaseStatus[i].equals("-")) {
                        release.setStatus(releaseStatus[i]);
                    }

                    if (!releaseDate[i].equals("-")) {
                        release.setDate(releaseDate[i].toLowerCase(Locale.US));
                    }

                    if (!releaseCountry[i].equals("-")) {
                        release.setCountry(releaseCountry[i]);
                    }

                    if (!releaseVA[i].equals("-")) {
                        release.setArtistCredit(createVariousArtistsCredit());
                    }

                    ReleaseGroup rg = of.createReleaseGroup();
                    release.setReleaseGroup(rg);
                    if (!releaseTypes[i].equals("-")) {
                        release.getReleaseGroup().setType(releaseTypes[i]);
                    }

                    org.musicbrainz.mmd2.Medium.TrackList.Track track = of.createMediumTrackListTrack();
                    track.setTitle(trackName[i]);

                    if (!trackArtistCredits[i].equals("-")) {
                        ArtistCredit tac = ArtistCreditHelper.unserialize(trackArtistCredits[i]);
                        track.setArtistCredit(tac);
                    }

                    org.musicbrainz.mmd2.Medium.TrackList releaseTrackList = of.createMediumTrackList();
                    releaseTrackList.setOffset(BigInteger.valueOf(NumericUtils.prefixCodedToInt(trackNos[i]) - 1));
                    releaseTrackList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numTracks[i])));
                    releaseTrackList.getDefTrack().add(track);
                    Medium medium = of.createMedium();
                    medium.setPosition(new BigInteger(mediumPos[i]));
                    medium.setTrackList(releaseTrackList);
                    if(!mediumFormat[i].equals("-"))
                    {
                        medium.setFormat(mediumFormat[i]);
                    }
                    MediumList mediumList = of.createMediumList();
                    mediumList.setTrackCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numTracksRelease[i])));
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

            recordingList.getRecording().add(recording);
        }
        recordingList.setCount(BigInteger.valueOf(results.totalHits));
        recordingList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setRecordingList(recordingList);
        return metadata;
    }



}