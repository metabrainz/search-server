/*
 Copyright (c) 2010 Paul Taylor
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
  2. Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
  3. Neither the name of the MusicBrainz project nor the names of the
     contributors may be used to endorse or promote products derived from
     this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.musicbrainz.search.index;

import com.google.common.base.Strings;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;

/**
 * Boost areas that are country so always listed before areas of the same name that are not countries
 */
public class AreaBoostDoc {

    //Double the score of this doc if it comes up in search
    private static float AREA_DOC_BOOST = 2.0f;
    private final static String  AREA_TYPE = "Country";

    public static void boost(String areaType, Document doc) {
        if(!Strings.isNullOrEmpty(areaType) && areaType.equals(AREA_TYPE)) {
            for(IndexableField indexablefield:doc.getFields())
            {
                if(indexablefield.name().equals(AreaIndexField.AREA.getName()))
                {
                    Field field = (Field)indexablefield;
                    field.setBoost(AREA_DOC_BOOST);
                    break;
                }
            }
        }
    }
}
