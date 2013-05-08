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
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;


public class ArtistMmd1XmlWriter extends Mmd1XmlWriter {



    public Metadata write(Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        ArtistList artistList = of.createArtistList();

        for (Result result : results.results) {
            MbDocument doc = result.getDoc();
            Artist artist = of.createArtist();

            result.setNormalizedScore(results.getMaxScore());
            artist.getOtherAttributes().put(getScore(), String.valueOf(result.getNormalizedScore()));

            org.musicbrainz.mmd2.Artist artistv2
                    = (org.musicbrainz.mmd2.Artist) MMDSerializer.unserialize(doc.get(ArtistIndexField.ARTIST_STORE), org.musicbrainz.mmd2.Artist.class);
            artist.setId(artistv2.getId());
            artist.setType(StringUtils.capitalize(artistv2.getType()));
            artist.setName(artistv2.getName());

            if(!Strings.isNullOrEmpty(artistv2.getSortName())) {
                artist.setSortName(artistv2.getSortName());
            }

            if(!Strings.isNullOrEmpty(artistv2.getDisambiguation())) {
                artist.setDisambiguation(artistv2.getDisambiguation());
            }

            if(artistv2.getLifeSpan()!=null) {
                String begin = artistv2.getLifeSpan().getBegin();
                String end = artistv2.getLifeSpan().getEnd();
                if (begin != null || end != null) {
                    LifeSpan lifespan = of.createLifeSpan();
                    if (begin != null) {
                        lifespan.setBegin(begin);

                    }
                    if (end != null) {
                        lifespan.setEnd(end);

                    }
                    artist.setLifeSpan(lifespan);
               }
            }

            artistList.getArtist().add(artist);

        }
        artistList.setCount(BigInteger.valueOf(results.getTotalHits()));
        artistList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setArtistList(artistList);
        return metadata;
    }


}
