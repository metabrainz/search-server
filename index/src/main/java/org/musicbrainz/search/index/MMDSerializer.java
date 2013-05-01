/*
 * MusicBrainz Search Server
 * Copyright (C) 2010  Paul Taylor

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package org.musicbrainz.search.index;

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;
import com.sun.jersey.api.json.JSONMarshaller;
import com.sun.jersey.api.json.JSONUnmarshaller;

import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


/**
 * Handles Serializing classes in the MMD for storing within Index, and deserializing back into
 * the original class.
 *
 * We use JSON rather than XML or Java Serialization because it is the quickest and takes less space.
 */
public class MMDSerializer {

    static final JSONJAXBContext jsoncontext = initJsonContext();


    /**
     * @return context for marshalling as JSON in a way that allows unmarshaling
     */
    private static JSONJAXBContext initJsonContext() {
        Map<String, String> nsMap = new HashMap<String, String>();
        nsMap.put("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        nsMap.put("http://musicbrainz.org/ns/mmd-2.0#", "mmd");
        nsMap.put("http://musicbrainz.org/ns/ext#-2.0", "ext");


        try {
            return new JSONJAXBContext(JSONConfiguration.mapped().rootUnwrapping(false).xml2JsonNs(nsMap).build()
                    ,
                    "org.musicbrainz.mmd2");
        }
        catch (JAXBException ex) {
            //Unable to initialize jaxb context, should never happen
            throw new RuntimeException(ex);
        }
    }


    /**
     * Serialize using json, the most compact solution
     *
     * @param object
     * @return
     */
    public static String serialize(Object object) {
        try {

            StringWriter sw = new StringWriter();
            JSONMarshaller m = jsoncontext.createJSONMarshaller();
            m.marshallToJSON(object, sw);
            return sw.toString();

        }
        catch (JAXBException je) {
            throw new RuntimeException(je);
        }
    }


    public static Object unserialize(String string, Class classType) {
        try {
            JSONUnmarshaller m = jsoncontext.createJSONUnmarshaller();
            return m.unmarshalFromJSON(new StringReader(string), classType);

        }
        catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }

}
