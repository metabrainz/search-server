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
import org.musicbrainz.search.index.Index;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.SearchServer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;


public class ReleaseWriter extends ResultsWriter {



    public void write(Metadata metadata, Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();
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

            String comment = doc.get(ReleaseIndexField.COMMENT);
            if (isNotNoValue(comment)) {
                release.setDisambiguation(comment);
            }

            String type = doc.get(ReleaseIndexField.TYPE);
            ReleaseGroup rg = of.createReleaseGroup();
            release.setReleaseGroup(rg);
            if (isNotUnknown(type)){
                release.getReleaseGroup().setType(type);
            }

            String rgid = doc.get(ReleaseIndexField.RELEASEGROUP_ID);
            if (rgid != null) {
                release.getReleaseGroup().setId(rgid);
            }

            String status = doc.get(ReleaseIndexField.STATUS);
            if (isNotUnknown(status)) {
                release.setStatus(status);
            }

            String country = doc.get(ReleaseIndexField.COUNTRY);
            if (isNotUnknown(country)){

                release.setCountry(country);
            }

            String date = doc.get(ReleaseIndexField.DATE);
            if (date != null) {
                release.setDate(date);
            }

            String barcode = doc.get(ReleaseIndexField.BARCODE);
            if (isNotNoValue(barcode)) {
                release.setBarcode(barcode);
            }

            String asin = doc.get(ReleaseIndexField.AMAZON_ID);
            if (isNotNoValue(asin)) {
                release.setAsin(asin);
            }

            TextRepresentation tr = of.createTextRepresentation();
            String script = doc.get(ReleaseIndexField.SCRIPT);
            if (isNotUnknown(script)) {
                tr.setScript(script);
            }

            String lang = doc.get(ReleaseIndexField.LANGUAGE);
            if (isNotUnknown(lang)) {
                tr.setLanguage(lang.toLowerCase(Locale.US));
            }

            if (script != null || lang != null) {
                release.setTextRepresentation(tr);
            }

            String[] labelNames = doc.getValues(ReleaseIndexField.LABEL);
            //Releases can only have multiple label/catno combinations
            if (labelNames.length > 0) {
                LabelInfoList labelInfoList = of.createLabelInfoList();
                String[] catnos = doc.getValues(ReleaseIndexField.CATALOG_NO);
                String[] labelIds = doc.getValues(ReleaseIndexField.LABEL_ID);
                for (int i = 0; i < labelNames.length; i++) {
                    LabelInfo labelInfo = of.createLabelInfo();

                    if (isNotNoValue(labelNames[i]) || isNotNoValue(labelIds[i])) {
                        Label label = of.createLabel();
                        labelInfo.setLabel(label);
                    
                        if (isNotNoValue(labelNames[i])) {
                            label.setName(labelNames[i]);
                        }
                        
                        if (isNotNoValue(labelIds[i])) {
                            label.setId(labelIds[i]);
                        }
                    }
                    
                    if (isNotNoValue(catnos[i])) {
                        labelInfo.setCatalogNumber(catnos[i]);
                    }
                    labelInfoList.getLabelInfo().add(labelInfo);
                }
                release.setLabelInfoList(labelInfoList);
            }

            if(doc.get(ReleaseIndexField.ARTIST_CREDIT)!=null) {
                ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseIndexField.ARTIST_CREDIT));
                release.setArtistCredit(ac);
            }

            String numTracksOnRelease = doc.get(ReleaseIndexField.NUM_TRACKS);
            if(numTracksOnRelease!=null)
            {
                MediumList mediumList = of.createMediumList();
                mediumList.setTrackCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numTracksOnRelease)));

                String[] formats          = doc.getValues(ReleaseIndexField.FORMAT);
                String[] numTracks        = doc.getValues(ReleaseIndexField.NUM_TRACKS_MEDIUM);
                String[] numDiscIds       = doc.getValues(ReleaseIndexField.NUM_DISCIDS_MEDIUM);
                for (int i = 0; i < formats.length; i++) {

                    Medium medium = of.createMedium();

                    if(isNotNoValue(formats[i])) {
                        medium.setFormat(formats[i]);
                    }
                    org.musicbrainz.mmd2.Medium.TrackList trackList = of.createMediumTrackList();
                    trackList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numTracks[i])));
                    medium.setTrackList(trackList);

                    DiscList discList = of.createDiscList();
                    discList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(numDiscIds[i])));
                    medium.setDiscList(discList);

                    mediumList.getMedium().add(medium);
                }
                mediumList.setCount(BigInteger.valueOf(formats.length));
                release.setMediumList(mediumList);
            }
            releaseList.getRelease().add(release);
        }
        releaseList.setCount(BigInteger.valueOf(results.totalHits));
        releaseList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setReleaseList(releaseList);
    }
}