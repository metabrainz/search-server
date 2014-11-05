/* Copyright (c) 2009 Paul Taylor
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


import org.musicbrainz.mmd2.Cdstub;
import org.musicbrainz.mmd2.CdstubList;
import org.musicbrainz.mmd2.Metadata;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.CDStubIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;


public class CDStubWriter extends ResultsWriter {

    /**
     *
     * @param metadata
     * @param results
     * @throws IOException
     */
    public void write(Metadata metadata, Results results) throws IOException {

        ObjectFactory of = new ObjectFactory();
        CdstubList cdstubList = of.createCdstubList();

        for (Result result : results.results) {
            MbDocument doc = result.getDoc();
            Cdstub cdstub = of.createCdstub();

            String artist = doc.get(CDStubIndexField.ARTIST);
            if (artist!= null)
            {
                cdstub.setArtist(artist);
            }
            else
            {
                cdstub.setArtist("");
            }

            String title = doc.get(CDStubIndexField.TITLE);
            if (title!= null)
            {
                cdstub.setTitle(title);
            }
            else
            {
                cdstub.setTitle("");
            }

            String discid = doc.get(CDStubIndexField.DISCID);
            if (discid!= null)
            {
                cdstub.setId(discid);
            }
            else
            {
                cdstub.setId("");
            }

            String comment = doc.get(CDStubIndexField.COMMENT);
            if (isNotNoValue(comment))
            {
                cdstub.setComment(comment);
            }

            String barcode = doc.get(CDStubIndexField.BARCODE);
            if (barcode!= null)
            {
                cdstub.setBarcode(barcode);
            }

            String numTracks = doc.get(CDStubIndexField.NUM_TRACKS);
            if(numTracks!=null)
            {
                org.musicbrainz.mmd2.Cdstub.TrackList trackList = of.createCdstubTrackList();
                trackList.setCount(new BigInteger(numTracks));
                cdstub.setTrackList(trackList);
            }


            result.setNormalizedScore(results.getMaxScore());
            cdstub.setScore(String.valueOf(result.getNormalizedScore()));
            cdstubList.getCdstub().add(cdstub);
        }
        cdstubList.setCount(BigInteger.valueOf(results.getTotalHits()));
        cdstubList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setCdstubList(cdstubList);
    }
}