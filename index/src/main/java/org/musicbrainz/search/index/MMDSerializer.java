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
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.musicbrainz.mmd2.Metadata;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;


/**
 * Handles Serializing classes in the MMD for storing within Index, and deserializing back into
 * the original class and little difference in space as stored fields are compressed by lucene anyway.
 */
public class MMDSerializer {


    static final JAXBContext            context                 = initContext();
    static final NamespacePrefixMapper prefixMapper            = new PreferredMapper();

    private static JAXBContext initContext() {
        try {
            return JAXBContext.newInstance("org.musicbrainz.mmd2");
        }
        catch (JAXBException ex) {
            //Unable to initilize jaxb context, should never happen
            throw new RuntimeException(ex);
        }
    }

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

    public static String serialize(Object o) {
        try {
            StringWriter sw = new StringWriter();
            Marshaller m = context.createMarshaller();
            m.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
            m.marshal(o, sw);
            return sw.toString();
        }
        catch (JAXBException je) {
            throw new RuntimeException(je);
        }
    }

    public static Object unserialize(String string, Class classType) {
        try {
            Unmarshaller m = context.createUnmarshaller();
            //m.setProperty("com.sun.xml.bind.namespacePrefixMapper", prefixMapper);
            return m.unmarshal(new StringReader(string));

        }
        catch (JAXBException ex) {
            throw new RuntimeException(ex);
        }
    }
}
