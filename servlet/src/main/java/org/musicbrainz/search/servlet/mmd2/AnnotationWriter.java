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


import org.musicbrainz.mmd2.Annotation;
import org.musicbrainz.mmd2.AnnotationList;
import org.musicbrainz.mmd2.Metadata;
import org.musicbrainz.mmd2.ObjectFactory;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.index.AnnotationIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;


public class AnnotationWriter extends ResultsWriter {



    public Metadata write(Results results) throws IOException {
        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        AnnotationList annotationList = of.createAnnotationList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Annotation annotation= of.createAnnotation();
            annotation.setName(doc.get(AnnotationIndexField.NAME));
            annotation.setText(doc.get(AnnotationIndexField.TEXT));
            annotation.setType(doc.get(AnnotationIndexField.TYPE));
            annotation.setEntity(doc.get(AnnotationIndexField.MBID));
            annotation.setScore(String.valueOf((int)(result.score * 100)));
            annotationList.getAnnotation().add(annotation);

        }
        annotationList.setCount(BigInteger.valueOf(results.totalHits));
        annotationList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setAnnotationList(annotationList);
        return metadata;
    }


}