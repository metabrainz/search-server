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

package org.musicbrainz.search;

import com.jthink.brainz.mmd.Artist;
import com.jthink.brainz.mmd.Metadata;
import com.jthink.brainz.mmd.ObjectFactory;
import com.jthink.brainz.mmd.Release;
import com.jthink.brainz.mmd.ReleaseList;
import com.jthink.brainz.mmd.Track;
import com.jthink.brainz.mmd.TrackList;
import org.apache.lucene.document.Document;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;

public class TrackXmlWriter extends XmlWriter {

    public void write(PrintWriter out, Results results) throws IOException {


        try {

            Marshaller m = context.createMarshaller();
            ObjectFactory of = new ObjectFactory();

            Metadata metadata = of.createMetadata();
            TrackList trackList = of.createTrackList();

            for (Result result : results.results) {
                Document doc = result.doc;
                Track track = of.createTrack();
                track.setId(doc.get(TrackIndexField.TRACK_ID.getName()));
                track.getOtherAttributes().put(new QName("ext:score"), String.valueOf((int) (result.score * 100)));

                String name = doc.get(TrackIndexField.TRACK.getName());
                if (name != null) {
                    track.setTitle(name);
                }

                String duration = doc.get(TrackIndexField.DURATION.getName());
                if (duration != null) {
                    track.setDuration(BigInteger.valueOf(Long.parseLong(duration)));
                }


                String artistName = doc.get(TrackIndexField.ARTIST.getName());
                if (artistName != null) {

                    Artist artist = of.createArtist();
                    artist.setName(artistName);
                    artist.setId(doc.get(ReleaseIndexField.ARTIST_ID.getName()));
                    track.setArtist(artist);
                }

                String releaseName = doc.get(TrackIndexField.RELEASE.getName());
                if (releaseName != null) {
                    Release release = of.createRelease();
                    release.setId(doc.get(TrackIndexField.RELEASE_ID.getName()));
                    release.setTitle(releaseName);

                    String trackNo = doc.get(TrackIndexField.TRACKNUM.getName());
                    String tracks = doc.get(TrackIndexField.NUM_TRACKS.getName());
                    if (trackNo != null) {
                        TrackList releaseTrackList = of.createTrackList();
                        releaseTrackList.setOffset(BigInteger.valueOf(Long.parseLong(trackNo)));
                        if (tracks != null) {
                            releaseTrackList.setCount(BigInteger.valueOf(Long.parseLong(tracks)));
                        }
                        release.setTrackList(releaseTrackList);

                    }
                    ReleaseList releaseList = of.createReleaseList();
                    releaseList.getRelease().add(release);
                    track.setReleaseList(releaseList);
                }
                trackList.getTrack().add(track);
            }
            trackList.setCount(BigInteger.valueOf(results.results.size()));
            trackList.setOffset(BigInteger.valueOf(results.offset));
            metadata.setTrackList(trackList);
            m.marshal(metadata, out);
        }
        catch (JAXBException je) {
            throw new IOException(je);
        }
    }

}
