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

import org.musicbrainz.mmd2.*;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.MMDSerializer;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.index.WorkIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;

public class WorkWriter extends ResultsWriter {


    public Metadata write(Results results) throws IOException {

        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        WorkList workList = of.createWorkList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Work work = of.createWork();
            work.setId(doc.get(WorkIndexField.WORK_ID));
            work.setScore(String.valueOf((int)(result.score * 100)));

            String name = doc.get(WorkIndexField.WORK);
            if (name != null) {
                work.setTitle(name);
            }

            String comment = doc.get(WorkIndexField.COMMENT);
            if (comment != null) {
                work.setDisambiguation(comment);
            }

            String type = doc.get(WorkIndexField.TYPE);
            if (type != null) {
                work.setType(type);
            }

            String iswc = doc.get(WorkIndexField.ISWC);
            if (iswc != null) {
                work.setIswc(iswc);
            }

            String artistRelation = doc.get(WorkIndexField.ARTIST_RELATION);
            if(artistRelation!=null)
            {
                RelationList rc = (RelationList) MMDSerializer.unserialize(artistRelation, RelationList.class);
                work.getRelationList().add(rc);
            }

            String[] aliases = doc.getValues(WorkIndexField.ALIAS);
            if(aliases.length>0)
            {
                AliasList aliasList = of.createAliasList();
                for(int i = 0;i<aliases.length;i++) {
                    Alias alias = of.createAlias();
                    alias.getContent().add(aliases[i]);
                    aliasList.getAlias().add(alias);
                }
                work.setAliasList(aliasList);
            }

            String[] tags       = doc.getValues(WorkIndexField.TAG);
            String[] tagCounts  = doc.getValues(WorkIndexField.TAGCOUNT);
            if(tags.length>0)
            {
               TagList tagList = of.createTagList();
               for(int i = 0;i<tags.length;i++) {
                   Tag tag = of.createTag();
                   tag.setName(tags[i]);
                   tag.setCount(new BigInteger(tagCounts[i]));
                   tagList.getTag().add(tag);
               }
               work.setTagList(tagList);
            }


            workList.getWork().add(work);
        }
        workList.setCount(BigInteger.valueOf(results.totalHits));
        workList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setWorkList(workList);
        return metadata;
    }

}