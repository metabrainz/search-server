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
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;


public class ReleaseWriter extends ResultsWriter {


    public Metadata write(Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        ReleaseList releaseList = of.createReleaseList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Release release = of.createRelease();
            release.setId(doc.get(ReleaseIndexField.RELEASE_ID));
            release.setScore(String.valueOf((int)(result.score * 100)));

            String name = doc.get(ReleaseIndexField.RELEASE);
            if (name != null) {
                release.setTitle(name);
            }

            String type = doc.get(ReleaseIndexField.TYPE);
            ReleaseGroup rg = of.createReleaseGroup();
            release.setReleaseGroup(rg);
            if (type != null) {
                release.getReleaseGroup().getType().add(type.toLowerCase(Locale.US));
            }

            String status = doc.get(ReleaseIndexField.STATUS);
            if (status != null) {
                release.setStatus(status.toLowerCase(Locale.US));
            }

            String country = doc.get(ReleaseIndexField.COUNTRY);
            if (country != null) {
                release.setCountry(country.toLowerCase(Locale.US));
            }

            String date = doc.get(ReleaseIndexField.DATE);
            if (date != null) {
                release.setDate(date);
            }

            String barcode = doc.get(ReleaseIndexField.BARCODE);
            if (barcode != null) {
                release.setBarcode(barcode);
            }

            String asin = doc.get(ReleaseIndexField.AMAZON_ID);
            if (asin != null) {
                release.setAsin(asin);
            }



            TextRepresentation tr = of.createTextRepresentation();
            String script = doc.get(ReleaseIndexField.SCRIPT);
            if (script != null) {
                tr.setScript(script.toLowerCase(Locale.US));
            }

            String lang = doc.get(ReleaseIndexField.LANGUAGE);
            if (lang != null) {
                tr.setLanguage(lang.toLowerCase(Locale.US));
            }

            if (script != null || lang != null) {
                release.setTextRepresentation(tr);
            }

            String[] labels = doc.getValues(ReleaseIndexField.LABEL);
            //Releases can only have multiple label/catno combinations
            if (labels.length > 0) {
                LabelInfoList labelInfoList = of.createLabelInfoList();
                String[] catnos = doc.getValues(ReleaseIndexField.CATALOG_NO);
                for (int i = 0; i < labels.length; i++) {
                    LabelInfo labelInfo = of.createLabelInfo();

                    if (!labels[i].equals("-")) {
                        Label label = of.createLabel();
                        label.setName(labels[i]);
                        labelInfo.setLabel(label);
                    }

                    if (!catnos[i].equals("-")) {
                        labelInfo.setCatalogNumber(catnos[i]);
                    }
                    labelInfoList.getLabelInfo().add(labelInfo);
                }
                release.setLabelInfoList(labelInfoList);
            }

            String[] artistIds = doc.getValues(ReleaseIndexField.ARTIST_ID);
            String[] artistNames = doc.getValues(ReleaseIndexField.ARTIST_NAME);
            String[] artistJoinPhrases = doc.getValues(ReleaseIndexField.ARTIST_JOINPHRASE);
            String[] artistSortNames = doc.getValues(ReleaseIndexField.ARTIST_SORTNAME);
            String[] artistCreditNames = doc.getValues(ReleaseIndexField.ARTIST_NAMECREDIT);

            ArtistCredit ac = of.createArtistCredit();
            for (int i = 0; i < artistIds.length; i++) {

                Artist artist = of.createArtist();
                artist.setId(artistIds[i]);
                artist.setName(artistNames[i]);
                artist.setSortName(artistSortNames[i]);
                NameCredit nc = of.createNameCredit();
                nc.setArtist(artist);
                if (!artistJoinPhrases[i].equals("-")) {
                    nc.setJoinphrase(artistJoinPhrases[i]);
                }
                if (!artistCreditNames[i].equals(artistNames[i])) {
                    nc.setName(artistCreditNames[i]);
                }
                ac.getNameCredit().add(nc);
                release.setArtistCredit(ac);
            }

            MediumList mediumList = of.createMediumList();
            String[] formats = doc.getValues(ReleaseIndexField.FORMAT);
            String[] numTracks = doc.getValues(ReleaseIndexField.NUM_TRACKS_MEDIUM);
            String[] numDiscIds = doc.getValues(ReleaseIndexField.NUM_DISCIDS_MEDIUM);
            for (int i = 0; i < formats.length; i++) {

                Medium medium = of.createMedium();
                medium.setFormat(formats[i].toLowerCase(Locale.US));

                TrackList trackList = of.createTrackList();
                trackList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numTracks[i])));
                medium.setTrackList(trackList);

                DiscList discList = of.createDiscList();
                discList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numDiscIds[i])));
                medium.setDiscList(discList);

                mediumList.getMedium().add(medium);
            }
            release.setMediumList(mediumList);
            releaseList.getRelease().add(release);
        }
        releaseList.setCount(BigInteger.valueOf(results.totalHits));
        releaseList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setReleaseList(releaseList);
        return metadata;

    }
}