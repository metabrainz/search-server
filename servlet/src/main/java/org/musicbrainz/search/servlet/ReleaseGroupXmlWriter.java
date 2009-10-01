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

package org.musicbrainz.search.servlet;

import com.jthink.brainz.mmd.Artist;
import com.jthink.brainz.mmd.Metadata;
import com.jthink.brainz.mmd.ObjectFactory;
import com.jthink.brainz.mmd.ReleaseGroup;
import com.jthink.brainz.mmd.ReleaseGroupList;
import org.apache.commons.lang.StringUtils;
import org.musicbrainz.search.index.ReleaseGroupIndexField;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;

public class ReleaseGroupXmlWriter extends XmlWriter {


    public Metadata write(Results results) throws IOException {

        ObjectFactory of = new ObjectFactory();

        Metadata metadata = of.createMetadata();
        ReleaseGroupList releaseGroupList = of.createReleaseGroupList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            ReleaseGroup releaseGroup = of.createReleaseGroup();
            releaseGroup.setId(doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));

            releaseGroup.getOtherAttributes().put(getScore(), String.valueOf((int) (result.score * 100)));

            String name = doc.get(ReleaseGroupIndexField.RELEASEGROUP);
            if (name != null) {
                releaseGroup.setTitle(name);

            }

            String type = doc.get(ReleaseGroupIndexField.TYPE);
            if (type != null) {
                releaseGroup.getType().add(StringUtils.capitalize(type));
            }


            String artistName = doc.get(ReleaseGroupIndexField.ARTIST_V1);
            if (artistName != null) {

                Artist artist = of.createArtist();
                artist.setName(artistName);
                artist.setId(doc.get(ReleaseGroupIndexField.ARTIST_ID));
                releaseGroup.setArtist(artist);
            }


            releaseGroupList.getReleaseGroup().add(releaseGroup);
        }
        releaseGroupList.setCount(BigInteger.valueOf(results.totalHits));
        releaseGroupList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setReleaseGroupList(releaseGroupList);
        return metadata;
    }

}