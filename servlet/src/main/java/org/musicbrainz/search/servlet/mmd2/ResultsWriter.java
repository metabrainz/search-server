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


import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.musicbrainz.mmd2.Metadata;
import org.musicbrainz.search.servlet.ErrorMessage;
import org.musicbrainz.search.servlet.Results;
import org.musicbrainz.search.servlet.SearchServerServlet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class ResultsWriter extends org.musicbrainz.search.servlet.ResultsWriter {

    static final JAXBContext            context         = initContext();
    static final NamespacePrefixMapper  prefixMapper    = new PreferredMapper();
    static final JSONJAXBContext        jsoncontext     = initJsonContext();

    public String getMimeType() {
          return "application/xml; charset=UTF-8";
      }

    public String getJsonMimeType() {
          return "application/json; charset=UTF-8";
      }
    
    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance("org.musicbrainz.mmd2");
        }
        catch (JAXBException ex) {
            //Unable to initilize jaxb context, should never happen
            throw new RuntimeException(ex);
        }
    }

    private static JSONJAXBContext initJsonContext() {
        try {
            return new JSONJAXBContext(JSONConfiguration.natural().build(),
            "org.musicbrainz.mmd2");
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
     * @throws java.io.IOException
     */
    public abstract Metadata write(Results results) throws IOException;


    /**
     * Write the results to provider writer as Xml
     *
     * @param out
     * @param results
     * @throws java.io.IOException
     */
    public void write(PrintWriter out, Results results,String outputFormat) throws IOException {
        if(outputFormat.equals(SearchServerServlet.RESPONSE_XML)) {

            try {
                Metadata metadata = write(results);
                Marshaller m = context.createMarshaller();
                m.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
                m.marshal(metadata, out);
            }
            catch (JAXBException je) {
                throw new IOException(je);
            }
        }
        else if(outputFormat.equals(SearchServerServlet.RESPONSE_JSON)) {
            try {
                Metadata metadata = write(results);
                JSONMarshaller m = jsoncontext.createJSONMarshaller();
                m.marshallToJSON(metadata, out);
            }
            catch (JAXBException je) {
                throw new IOException(je);
            }
        }
        else {
           throw new RuntimeException(ErrorMessage.NO_HANDLER_FOR_TYPE_AND_FORMAT.getMsg(this.getClass(), outputFormat));
        }
    }

    /**
     * Required to map score to ext namespace now that score defined properly because by default JAXB creates
     * namespaces with names ns1,ns2..
     */
    public static class PreferredMapper extends NamespacePrefixMapper {
        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if(namespaceUri.equals("http://musicbrainz.org/ns/ext#-2.0"))
            {
                return "ext";
            }
            return null;
        }
    }

}