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
import org.musicbrainz.search.index.LabelIndexField;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class LabelWriter extends ResultsWriter {

    public Metadata write(Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        LabelList labelList = of.createLabelList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Label label = of.createLabel();
            label.setId(doc.get(LabelIndexField.LABEL_ID));
            String type = doc.get(LabelIndexField.TYPE);
            if(type!=null) {
                label.setType(type.toLowerCase((Locale.US)));
            }
            label.setScore(String.valueOf((int)(result.score * 100)));
            String name = doc.get(LabelIndexField.LABEL);
            if (name != null) {
                label.setName(name);

            }

            String code = doc.get(LabelIndexField.CODE);
            if (code != null) {
                label.setLabelCode(new BigInteger(code));

            }

            String sortname = doc.get(LabelIndexField.SORTNAME);
            if (sortname != null) {
                label.setSortName(sortname);

            }

            String begin = doc.get(LabelIndexField.BEGIN);
            String end = doc.get(LabelIndexField.END);
            if (begin != null || end != null) {
                LifeSpan lifespan = of.createLifeSpan();
                if (begin != null) {
                    lifespan.setBegin(begin);

                }
                if (end != null) {
                    lifespan.setEnd(end);

                }
                label.setLifeSpan(lifespan);

            }

            String comment = doc.get(LabelIndexField.COMMENT);
            if (comment != null) {
                label.setDisambiguation(comment);
            }

            String[] aliases = doc.getValues(LabelIndexField.ALIAS);
            if(aliases.length>0)
            {
                AliasList aliasList = of.createAliasList();
                for(int i = 0;i<aliases.length;i++) {
                    Alias alias = of.createAlias();
                    alias.getContent().add(aliases[i]);
                    aliasList.getAlias().add(alias);
                }
                label.setAliasList(aliasList);
            }
            
            labelList.getLabel().add(label);

        }
        labelList.setCount(BigInteger.valueOf(results.totalHits));
        labelList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setLabelList(labelList);
        return metadata;
    }
}