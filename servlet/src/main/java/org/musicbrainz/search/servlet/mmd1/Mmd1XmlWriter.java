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

package org.musicbrainz.search.servlet.mmd1;

import com.jthink.brainz.mmd.Metadata;
import org.musicbrainz.search.servlet.RequestParameter;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.ResultsWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.EnumMap;

public abstract class Mmd1XmlWriter extends ResultsWriter {

    static final JAXBContext context = initContext();

    public String getMimeType() {
        return "application/xml; charset=UTF-8";
    }

    /**
     *
     * @return
     */
    protected QName getScore()
    {
        return new QName("http://musicbrainz.org/ns/ext#-1.0","score","ext");
    }

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance("com.jthink.brainz.mmd");
        }
        catch (JAXBException ex) {
            //Unable to initilize jaxb context, should never happen
            throw new RuntimeException(ex);
        }
    }

    /**
     * Put results into an XML representation class whereby it can be manipulated further or
     * converted to an output stream
     *
     * @param results
     * @return
     * @throws IOException
     */
    public abstract Metadata write(Results results) throws IOException;


    /**
     * Write the results to provider writer
     *
     * @param out
     * @param results
     * @throws IOException
     */
    public void write(PrintWriter out, Results results, EnumMap<RequestParameter,String> extraInfoMap) throws IOException {
        try {
            Metadata metadata = write(results);
            Marshaller m = context.createMarshaller();
            m.marshal(metadata, out);
        }
        catch (JAXBException je) {
            throw new IOException(je);
        }
    }
}
