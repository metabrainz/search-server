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
import org.musicbrainz.search.index.ArtistCreditHelper;
import org.musicbrainz.search.index.RecordingIndexField;
import org.musicbrainz.search.index.ReleaseGroupIndexField;
import org.musicbrainz.search.index.ReleaseIndexField;
import org.musicbrainz.search.servlet.Result;
import org.musicbrainz.search.servlet.Results;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class ReleaseGroupWriter extends ResultsWriter {

    public void write(Metadata metadata, Results results) throws IOException {

        ObjectFactory of = new ObjectFactory();
        ReleaseGroupList releaseGroupList = of.createReleaseGroupList();

        for (Result result : results.results) {
            MbDocument doc = result.doc;
            ReleaseGroup releaseGroup = of.createReleaseGroup();
            releaseGroup.setId(doc.get(ReleaseGroupIndexField.RELEASEGROUP_ID));
            releaseGroup.setScore(String.valueOf((int)(result.score * 100)));
            String name = doc.get(ReleaseGroupIndexField.RELEASEGROUP);
            if (name != null) {
                releaseGroup.setTitle(name);
            }

            String comment = doc.get(ReleaseGroupIndexField.COMMENT);
            if (comment != null) {
                releaseGroup.setDisambiguation(comment);
            }

            String type = doc.get(ReleaseGroupIndexField.TYPE);
            if(type!=null) {
                releaseGroup.setType(type);
            }

            if(doc.get(ReleaseGroupIndexField.ARTIST_CREDIT)!=null) {
                ArtistCredit ac = ArtistCreditHelper.unserialize(doc.get(ReleaseGroupIndexField.ARTIST_CREDIT));
                releaseGroup.setArtistCredit(ac);
            }

            String[] releaseIds          = doc.getValues(ReleaseGroupIndexField.RELEASE_ID);
            String[] releaseNames        = doc.getValues(ReleaseGroupIndexField.RELEASE);
            ReleaseList releaseList = of.createReleaseList();
            releaseList.setCount(BigInteger.valueOf(releaseIds.length));
            for(int i =0; i< releaseIds.length; i++) {
                Release release = of.createRelease();
                release.setId(releaseIds[i]);
                release.setTitle(releaseNames[i]);
                releaseList.getRelease().add(release);
            }
            releaseGroup.setReleaseList(releaseList);
            releaseGroupList.getReleaseGroup().add(releaseGroup);

            String[] tags       = doc.getValues(ReleaseGroupIndexField.TAG);
            String[] tagCounts  = doc.getValues(ReleaseGroupIndexField.TAGCOUNT);
            if(tags.length>0)
            {
                TagList tagList = of.createTagList();
                for(int i = 0;i<tags.length;i++) {
                    Tag tag = of.createTag();
                    tag.setName(tags[i]);
                    tag.setCount(new BigInteger(tagCounts[i]));
                    tagList.getTag().add(tag);
                }
                releaseGroup.setTagList(tagList);
            }
        }
        releaseGroupList.setCount(BigInteger.valueOf(results.totalHits));
        releaseGroupList.setOffset(BigInteger.valueOf(results.offset));
        metadata.setReleaseGroupList(releaseGroupList);
    }
}