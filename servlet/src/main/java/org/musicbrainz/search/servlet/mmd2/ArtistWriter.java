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


import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Locale;


public class ArtistWriter extends ResultsWriter
{

    public void write(Metadata metadata, Results results) throws IOException
    {
        ObjectFactory of = new ObjectFactory();
        ArtistList artistList = of.createArtistList();

        for(Result result:results.results)
        {
            result.setNormalizedScore(results.getMaxScore());
        }
        write(artistList.getArtist(), results);

        artistList.setCount(BigInteger.valueOf(results.getTotalHits()));
        artistList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setArtistList(artistList);
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

        MbDocument doc = result.getDoc();
        Artist artist = of.createArtist();

        artist.setId(doc.get(ArtistIndexField.ARTIST_ID));
        artist.setScore(String.valueOf(result.getNormalizedScore()));

        String artype = doc.get(ArtistIndexField.TYPE);
        if (isNotUnknown(artype))
        {
            artist.setType(artype);
        }

        String gender = doc.get(ArtistIndexField.GENDER);
        if (isNotUnknown(gender))
        {
            artist.setGender(gender);
        }

        String country = doc.get(ArtistIndexField.COUNTRY);
        if (isNotUnknown(country))
        {
            artist.setCountry(country.toUpperCase(Locale.US));

        }

        String name = doc.get(ArtistIndexField.ARTIST);
        if (name != null)
        {
            artist.setName(name);
        }

        String[] ipiCodes = doc.getValues(ArtistIndexField.IPI);
        if (ipiCodes.length > 0)
        {
            IpiList ipiList = of.createIpiList();
            for (int i = 0; i < ipiCodes.length; i++)
            {
                ipiList.getIpi().add(ipiCodes[i]);
            }
            artist.setIpiList(ipiList);
        }

        String sortname = doc.get(ArtistIndexField.SORTNAME);
        if (sortname != null)
        {
            artist.setSortName(sortname);

        }

        String begin = doc.get(ArtistIndexField.BEGIN);
        String end = doc.get(ArtistIndexField.END);
        String ended = doc.get(ArtistIndexField.ENDED);

        LifeSpan lifespan = of.createLifeSpan();
        artist.setLifeSpan(lifespan);

        if (begin != null)
        {
            lifespan.setBegin(begin);
        }

        if (end != null)
        {
            lifespan.setEnd(end);
        }
        lifespan.setEnded(ended);

        String comment = doc.get(ArtistIndexField.COMMENT);
        if (isNotNoValue(comment))
        {
            artist.setDisambiguation(comment);
        }

        String[] aliases = doc.getValues(ArtistIndexField.ALIAS);
        if (aliases.length > 0)
        {
            AliasList aliasList = of.createAliasList();
            for (int i = 0; i < aliases.length; i++)
            {
                Alias alias = of.createAlias();
                alias.setContent(aliases[i]);
                aliasList.getAlias().add(alias);
            }
            artist.setAliasList(aliasList);
        }

        String[] tags = doc.getValues(ArtistIndexField.TAG);
        String[] tagCounts = doc.getValues(ArtistIndexField.TAGCOUNT);
        if (tags.length > 0)
        {
            TagList tagList = of.createTagList();
            for (int i = 0; i < tags.length; i++)
            {
                Tag tag = of.createTag();
                tag.setName(tags[i]);
                tag.setCount(new BigInteger(tagCounts[i]));
                tagList.getTag().add(tag);
            }
            artist.setTagList(tagList);
        }
        list.add(artist);
    }
}