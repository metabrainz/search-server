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

import org.musicbrainz.mmd2.Metadata;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.mmd2.Tag;
import org.musicbrainz.mmd2.TagList;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.TagIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Write Folksonomy Tags
 *
 */
public class TagWriter extends ResultsWriter {

    /**
     *
     * @param metadata
     * @param results
     * @throws IOException
     */
    public void write(Metadata metadata, Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();
        TagList  tagList = of.createTagList();

        for (Result result : results.results) {
            MbDocument doc = result.getDoc();
            Tag tag = of.createTag();
            tag.setName(doc.get(TagIndexField.TAG));;
            result.setNormalizedScore(results.getMaxScore());
            tag.setScore(String.valueOf(result.getNormalizedScore()));
            tagList.getTag().add(tag);
        }
        tagList.setCount(BigInteger.valueOf(results.getTotalHits()));
        tagList.setOffset(BigInteger.valueOf(results.getOffset()));
        metadata.setTagList(tagList);
    }
}