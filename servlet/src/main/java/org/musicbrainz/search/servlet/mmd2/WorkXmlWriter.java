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
import org.musicbrainz.search.index.WorkIndexField;
import org.musicbrainz.search.MbDocument;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;


import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class WorkXmlWriter extends XmlWriter {


    public Metadata write(Results results) throws IOException {

        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        WorkList workList = of.createWorkList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            Work work = of.createWork();
            work.setId(doc.get(WorkIndexField.WORK_ID));
            work.getOtherAttributes().put(getScore(), String.valueOf((int) (result.score * 100)));

            String name = doc.get(WorkIndexField.WORK);
            if (name != null) {
                work.setTitle(name);

            }

            String type = doc.get(WorkIndexField.TYPE);
            if (type != null) {
                work.setType(type.toLowerCase(Locale.US));
            }

            String iswc = doc.get(WorkIndexField.ISWC);
            if (iswc != null) {
                work.setIswc(iswc);
            }


            String[] artistIds          = doc.getValues(WorkIndexField.ARTIST_ID);
            String[] artistNames        = doc.getValues(WorkIndexField.ARTIST_NAME);
            String[] artistJoinPhrases  = doc.getValues(WorkIndexField.ARTIST_JOINPHRASE);
            String[] artistSortNames    = doc.getValues(WorkIndexField.ARTIST_SORTNAME);
            String[] artistCreditNames  = doc.getValues(WorkIndexField.ARTIST_NAMECREDIT);

            ArtistCredit ac = of.createArtistCredit();
            for (int i = 0; i < artistIds.length; i++) {
                Artist     artist   = of.createArtist();
                artist.setId(artistIds[i]);
                artist.setName(artistNames[i]);
                artist.setSortName(artistSortNames[i]);
                NameCredit nc = of.createNameCredit();
                nc.setArtist(artist);
                if(!artistJoinPhrases[i].equals("-")) {
                    nc.setJoinphrase(artistJoinPhrases[i]);
                }
                if(!artistCreditNames[i].equals(artistNames[i])) {
                    nc.setName(artistCreditNames[i]);
                }
                ac.getNameCredit().add(nc);
                work.setArtistCredit(ac);
            }
            workList.getWork().add(work);
        }
        workList.setCount(BigInteger.valueOf(results.totalHits));
        workList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setWorkList(workList);
        return metadata;
    }

}