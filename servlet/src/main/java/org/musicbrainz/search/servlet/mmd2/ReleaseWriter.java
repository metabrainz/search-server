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
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;


public class ReleaseWriter extends ResultsWriter {


    /**
     *
     * @param metadata
     * @param results
     * @throws IOException
     */
    public void write(Metadata metadata, Results results) throws IOException
    {
        ObjectFactory of = new ObjectFactory();
        ReleaseList releaseList = of.createReleaseList();

        for(Result result:results.results)
        {
            result.setNormalizedScore(results.getMaxScore());
        }
        write(releaseList.getRelease(), results);

        releaseList.setCount(BigInteger.valueOf(results.getTotalHits()));
        releaseList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setReleaseList(releaseList);
    }

    /**
     *
     * @param list
     * @param results
     * @throws IOException
     */
    public void write(List list, Results results) throws IOException
    {
        for (Result result : results.results)
        {
            write(list, result);
        }
    }

    /**
     *
     * @param list
     * @param result
     * @throws IOException
     */
    public void write(List list, Result result) throws IOException
    {
        ObjectFactory of = new ObjectFactory();

            MbDocument doc = result.getDoc();
            Release release = of.createRelease();
            release.setId(doc.get(ReleaseIndexField.RELEASE_ID));
            release.setScore(String.valueOf(result.getNormalizedScore()));

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

            String primaryType = doc.get(ReleaseGroupIndexField.PRIMARY_TYPE);
            if(isNotUnknown(primaryType )) {
                release.getReleaseGroup().setPrimaryType(primaryType );
            }

            String[] secondaryTypes = doc.getValues(ReleaseIndexField.SECONDARY_TYPE);
            if(secondaryTypes.length>0) {
                SecondaryTypeList stl = of.createSecondaryTypeList();
                for(int i =0; i< secondaryTypes.length; i++) {
                    stl.getSecondaryType().add(secondaryTypes[i]);
                }
                release.getReleaseGroup().setSecondaryTypeList(stl);
            }

            String rgid = doc.get(ReleaseIndexField.RELEASEGROUP_ID);
            if (rgid != null) {
                release.getReleaseGroup().setId(rgid);
            }

            String status = doc.get(ReleaseIndexField.STATUS);
            if (isNotUnknown(status)) {
                release.setStatus(status);
            }

            String[] countrys = doc.getValues(ReleaseIndexField.COUNTRY);
            String[] dates    = doc.getValues(ReleaseIndexField.DATE);
            ReleaseEventList rel = of.createReleaseEventList();
            for(int i=0;i<countrys.length; i++)
            {
                String nextCountry = countrys[i];
                String nextDate = dates[i];
                if(isNotUnknown(nextCountry) || isNotUnknown(nextDate))
                {
                    ReleaseEvent re = of.createReleaseEvent();
                    if(isNotUnknown(nextCountry))
                    {
                        re.setCountry(nextCountry);
                    }
                    if(isNotUnknown(nextDate))
                    {
                        re.setDate(nextDate);
                    }
                    rel.getReleaseEvent().add(re);
                }
            }
            if(rel.getReleaseEvent().size()>0) {
                release.setReleaseEventList(rel);
            }

            //For backwards compatability
            if (isNotUnknown(countrys[0])){

                release.setCountry(countrys[0]);
            }
            if (isNotUnknown(dates[0])){

                release.setDate(dates[0]);
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
                mediumList.setTrackCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(numTracksOnRelease))));

                String[] formats          = doc.getValues(ReleaseIndexField.FORMAT);
                String[] numTracks        = doc.getValues(ReleaseIndexField.NUM_TRACKS_MEDIUM);
                String[] numDiscIds       = doc.getValues(ReleaseIndexField.NUM_DISCIDS_MEDIUM);
                for (int i = 0; i < formats.length; i++) {

                    Medium medium = of.createMedium();

                    if(isNotNoValue(formats[i])) {
                        medium.setFormat(formats[i]);
                    }
                    org.musicbrainz.mmd2.Medium.TrackList trackList = of.createMediumTrackList();
                    trackList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(numTracks[i]))));
                    medium.setTrackList(trackList);

                    DiscList discList = of.createDiscList();
                    discList.setCount(BigInteger.valueOf(NumericUtils.prefixCodedToInt(new BytesRef(numDiscIds[i]))));
                    medium.setDiscList(discList);

                    mediumList.getMedium().add(medium);
                }
                mediumList.setCount(BigInteger.valueOf(formats.length));
                release.setMediumList(mediumList);
            }

            String[] tags       = doc.getValues(ReleaseIndexField.TAG);
            String[] tagCounts  = doc.getValues(ReleaseIndexField.TAGCOUNT);
            if(tags.length>0)
            {
                TagList tagList = of.createTagList();
                for(int i = 0;i<tags.length;i++) {
                    Tag tag = of.createTag();
                    tag.setName(tags[i]);
                    tag.setCount(new BigInteger(tagCounts[i]));
                    tagList.getTag().add(tag);
                }
                release.setTagList(tagList);
            }

            list.add(release);
        }
}