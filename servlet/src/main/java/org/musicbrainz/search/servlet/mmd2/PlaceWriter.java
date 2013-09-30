/* Copyright (c) 2013 Paul Taylor
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


import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.PlaceIndexField;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


public class PlaceWriter extends ResultsWriter {


    /**
     * @param metadata
     * @param results
     * @throws java.io.IOException
     */
    public void write(Metadata metadata, Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();
        PlaceList areaList = of.createPlaceList();

        for (Result result : results.results) {
            result.setNormalizedScore(results.getMaxScore());
        }
        write(areaList.getPlace(), results);

        areaList.setCount(BigInteger.valueOf(results.getTotalHits()));
        areaList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setPlaceList(areaList);
    }

    /**
     * @param list
     * @param results
     * @throws java.io.IOException
     */
    public void write(List list, Results results) throws IOException {
        for (Result result : results.results) {
            write(list, result);
        }
    }

    /**
     * @param list
     * @param result
     * @throws java.io.IOException
     */
    public void write(List list, Result result) throws IOException {
        MbDocument doc = result.getDoc();
        Place place = (Place) MMDSerializer.unserialize(doc.get(PlaceIndexField.PLACE_STORE), Place.class);
        place.setScore(String.valueOf(result.getNormalizedScore()));
        list.add(place);
    }
}