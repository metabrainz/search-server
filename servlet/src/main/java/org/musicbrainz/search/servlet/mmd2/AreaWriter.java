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
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.AreaIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;


public class AreaWriter extends ResultsWriter {


    /**
     * @param metadata
     * @param results
     * @throws java.io.IOException
     */
    public void write(Metadata metadata, Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();
        AreaList areaList = of.createAreaList();

        for (Result result : results.results) {
            result.setNormalizedScore(results.getMaxScore());
        }
        write(areaList.getArea(), results);

        areaList.setCount(BigInteger.valueOf(results.getTotalHits()));
        areaList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setAreaList(areaList);
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
        //TODO DefAreaElementInner not defined as @Xmlrootelement so we have to wrap in list
        AreaList areaList = (AreaList) MMDSerializer.unserialize(doc.get(AreaIndexField.AREA_STORE), AreaList.class);
        DefAreaElementInner area= areaList.getArea().get(0);
        area.setScore(String.valueOf(result.getNormalizedScore()));
        list.add(area);
    }

    /**
     * Overridden to ensure all attributes are set for each alias
     *
     * @param metadata
     */
    @Override
    public void adjustForJson(Metadata metadata) {

        if (metadata.getAreaList().getArea().size()>0) {
            for(DefAreaElementInner area:metadata.getAreaList().getArea()) {
                if(area.getAliasList()!=null) {
                    for (Alias alias : area.getAliasList().getAlias()) {
                        //On Xml output as primary, but in json they have changed to true/false
                        if (alias.getPrimary() == null) {
                            alias.setPrimary("false");
                        }
                        else {
                            alias.setPrimary("true");
                        }
                    }
                }
            }
        }

    }
}