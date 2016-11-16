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
import com.jthink.brainz.mmd.Artist;
import com.jthink.brainz.mmd.DiscList;
import com.jthink.brainz.mmd.Event;
import com.jthink.brainz.mmd.Label;
import com.jthink.brainz.mmd.Metadata;
import com.jthink.brainz.mmd.ObjectFactory;
import com.jthink.brainz.mmd.Release;
import com.jthink.brainz.mmd.ReleaseEventList;
import com.jthink.brainz.mmd.ReleaseList;
import com.jthink.brainz.mmd.TextRepresentation;
import org.apache.commons.lang.StringUtils;
import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class ReleaseMmd1XmlWriter extends Mmd1XmlWriter {


    public Metadata write(Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        ReleaseList releaseList = of.createReleaseList();

        for (Result result : results.results) {
            MbDocument doc = result.getDoc();

            Release release = of.createRelease();
            result.setNormalizedScore(results.getMaxScore());
            release.getOtherAttributes().put(getScore(), String.valueOf(result.getNormalizedScore()));

            org.musicbrainz.mmd2.Release releasev2
                    = (org.musicbrainz.mmd2.Release) MMDSerializer.unserialize(doc.get(ReleaseIndexField.RELEASE_STORE), org.musicbrainz.mmd2.Release.class);
            release.setId(releasev2.getId());
            release.getType().add(StringUtils.capitalize(releasev2.getReleaseGroup().getType()));

            if (!Strings.isNullOrEmpty(releasev2.getStatus().getContent())) {
                release.getType().add(releasev2.getStatus().getContent());
            }

            if (!Strings.isNullOrEmpty(releasev2.getTitle())) {
                release.setTitle(releasev2.getTitle());
            }

            if (!Strings.isNullOrEmpty(releasev2.getAsin())) {
                release.setAsin(releasev2.getAsin());
            }

            TextRepresentation tr = of.createTextRepresentation();
            org.musicbrainz.mmd2.TextRepresentation tr2 = releasev2.getTextRepresentation();
            if (tr2 != null) {
                if (!Strings.isNullOrEmpty(tr2.getScript())) {
                    tr.setScript(tr2.getScript());
                }

                if (!Strings.isNullOrEmpty(tr2.getLanguage())) {
                    tr.setLanguage(tr2.getLanguage().toUpperCase(Locale.US));
                }
                release.setTextRepresentation(tr);
            }

            if (!Strings.isNullOrEmpty(releasev2.getCountry())) {
                release.setAsin(releasev2.getAsin());
            }

            if (!Strings.isNullOrEmpty(releasev2.getDate())) {
                release.setAsin(releasev2.getAsin());
            }

            if (!Strings.isNullOrEmpty(releasev2.getBarcode())) {
                release.setAsin(releasev2.getAsin());
            }

            if (!Strings.isNullOrEmpty(releasev2.getAsin())) {
                release.setAsin(releasev2.getAsin());
            }

            //Just use format of first medium
            Medium firstMediumv2 = releasev2.getMediumList().getMedium().get(0);
            LabelInfoList lilv2 = releasev2.getLabelInfoList();
            if (lilv2!=null && !lilv2.getLabelInfo().isEmpty()) {
                ReleaseEventList eventList = of.createReleaseEventList();
                for (LabelInfo liv2 : lilv2.getLabelInfo()) {
                    Event event = of.createEvent();

                    if (!Strings.isNullOrEmpty(liv2.getCatalogNumber())) {
                        event.setCatalogNumber(liv2.getCatalogNumber());
                    }

                    org.musicbrainz.mmd2.Label labelv2 = liv2.getLabel();
                    if (labelv2 != null) {
                        Label label = of.createLabel();
                        event.setLabel(label);
                        if (!Strings.isNullOrEmpty(labelv2.getId())) {
                            label.setId(labelv2.getId());
                        }

                        if (!Strings.isNullOrEmpty(labelv2.getName())) {
                            label.setName(labelv2.getName());
                        }
                    }

                    if (!Strings.isNullOrEmpty(releasev2.getCountry())) {
                        event.setCountry(StringUtils.upperCase(releasev2.getCountry()));
                    }

                    if (!Strings.isNullOrEmpty(releasev2.getDate())) {
                        event.setDate(releasev2.getDate());
                    }

                    if (!Strings.isNullOrEmpty(releasev2.getBarcode())) {
                        event.setBarcode(releasev2.getBarcode());
                    }

                    if (!Strings.isNullOrEmpty(firstMediumv2.getFormat().getContent())) {
                        event.setFormat(firstMediumv2.getFormat().getContent());
                    }
                    eventList.getEvent().add(event);
                }
                release.setReleaseEventList(eventList);
            } else {
                ReleaseEventList eventList = of.createReleaseEventList();
                Event event = of.createEvent();
                eventList.getEvent().add(event);
                release.setReleaseEventList(eventList);

                if (!Strings.isNullOrEmpty(releasev2.getCountry())) {
                    event.setCountry(StringUtils.upperCase(releasev2.getCountry()));
                }

                if (!Strings.isNullOrEmpty(releasev2.getDate())) {
                    event.setDate(releasev2.getDate());
                }

                if (!Strings.isNullOrEmpty(releasev2.getBarcode())) {
                    event.setBarcode(releasev2.getBarcode());
                }

                if (firstMediumv2 != null && !Strings.isNullOrEmpty(firstMediumv2.getFormat().getContent())) {
                    event.setFormat(firstMediumv2.getFormat().getContent());
                }
            }

            ArtistCredit acv2 = releasev2.getArtistCredit();
            if(acv2!=null) {
                if (acv2.getNameCredit().size() > 0) {
                    Artist artist = of.createArtist();
                    artist.setName(acv2.getNameCredit().get(0).getArtist().getName());
                    artist.setId(acv2.getNameCredit().get(0).getArtist().getId());
                    artist.setSortName(acv2.getNameCredit().get(0).getArtist().getSortName());
                    release.setArtist(artist);
                }
            }

            int totaldiscIds=0;
            MediumList mediumListv2 = releasev2.getMediumList();
            TrackList trackList = of.createTrackList();
            trackList.setCount(mediumListv2.getTrackCount());
            release.setTrackList(trackList);

            for(Medium mediumv2:mediumListv2.getMedium())
            {
                totaldiscIds+=mediumv2.getDiscList().getCount().intValue();
            }
            DiscList discList = of.createDiscList();
            discList.setCount(BigInteger.valueOf(totaldiscIds));
            release.setDiscList(discList);

            releaseList.getRelease().add(release);
        }
        releaseList.setCount(BigInteger.valueOf(results.getTotalHits()));
        releaseList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setReleaseList(releaseList);
        return metadata;
    }
}
