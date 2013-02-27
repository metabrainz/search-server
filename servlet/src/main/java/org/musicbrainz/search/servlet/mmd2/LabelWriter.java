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
import org.musicbrainz.search.index.ArtistIndexField;
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class LabelWriter extends ResultsWriter {

    public void write(Metadata metadata, Results results) throws IOException {

        ObjectFactory of = new ObjectFactory();
        LabelList labelList = of.createLabelList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Label label = of.createLabel();
            label.setId(doc.get(LabelIndexField.LABEL_ID));
            String type = doc.get(LabelIndexField.TYPE);
            if (isNotUnknown(type)){
                label.setType(type);
            }
            label.setScore(calculateNormalizedScore(result, results.maxScore));
            String name = doc.get(LabelIndexField.LABEL);
            if (name != null) {
                label.setName(name);
            }

            String[] ipiCodes = doc.getValues(LabelIndexField.IPI);
            if (ipiCodes.length > 0) {
                IpiList ipiList = of.createIpiList();
                for (int i = 0; i < ipiCodes.length; i++) {
                    ipiList.getIpi().add(ipiCodes[i]);
                }
                label.setIpiList(ipiList);
            }

            String code = doc.get(LabelIndexField.CODE);
            if (isNotNoValue(code)) {
                label.setLabelCode(new BigInteger(code));

            }

            String countryCode = doc.get(LabelIndexField.COUNTRY);
            if (isNotUnknown(countryCode)){
                label.setCountry(countryCode.toUpperCase(Locale.US));
            }

            String sortname = doc.get(LabelIndexField.SORTNAME);
            if (sortname != null) {
                label.setSortName(sortname);
            }

            String begin = doc.get(LabelIndexField.BEGIN);
            String end = doc.get(LabelIndexField.END);
            String ended = doc.get(LabelIndexField.ENDED);

            LifeSpan lifespan = of.createLifeSpan();
            label.setLifeSpan(lifespan);

            if (begin != null) {
                lifespan.setBegin(begin);
            }

            if (end != null) {
                lifespan.setEnd(end);
            }
            lifespan.setEnded(ended);

            String comment = doc.get(LabelIndexField.COMMENT);
            if (isNotNoValue(comment)) {
                label.setDisambiguation(comment);
            }

            String[] aliases = doc.getValues(LabelIndexField.ALIAS);
            if(aliases.length>0)
            {
                AliasList aliasList = of.createAliasList();
                for(int i = 0;i<aliases.length;i++) {
                    Alias alias = of.createAlias();
                    alias.setContent(aliases[i]);
                    aliasList.getAlias().add(alias);
                }
                label.setAliasList(aliasList);
            }

            String[] tags       = doc.getValues(LabelIndexField.TAG);
            String[] tagCounts  = doc.getValues(LabelIndexField.TAGCOUNT);
            if(tags.length>0)
            {
                TagList tagList = of.createTagList();
                for(int i = 0;i<tags.length;i++) {
                    Tag tag = of.createTag();
                    tag.setName(tags[i]);
                    tag.setCount(new BigInteger(tagCounts[i]));
                    tagList.getTag().add(tag);
                }
                label.setTagList(tagList);
            }

            
            labelList.getLabel().add(label);

        }
        labelList.setCount(BigInteger.valueOf(results.totalHits));
        labelList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setLabelList(labelList);
    }
}