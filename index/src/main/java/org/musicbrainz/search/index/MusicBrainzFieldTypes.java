/*
 Copyright (c) 2013 Paul Taylor
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

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class MusicBrainzFieldTypes
{
    public static FieldType TEXT_STORED_ANALYZED                = new FieldType(TextField.TYPE_STORED);
    public static FieldType TEXT_STORED_NOT_INDEXED             = new FieldType(TextField.TYPE_STORED);
    public static FieldType TEXT_STORED_ANALYZED_NO_NORMS       = new FieldType(TextField.TYPE_STORED);
    public static FieldType TEXT_STORED_NOT_ANALYZED            = new FieldType(StringField.TYPE_STORED);
    public static FieldType TEXT_STORED_NOT_ANALYZED_NO_NORMS   = new FieldType(StringField.TYPE_STORED);
    public static FieldType TEXT_NOT_STORED_ANALYZED            = new FieldType(TextField.TYPE_STORED);
    public static FieldType TEXT_NOT_STORED_ANALYZED_NO_NORMS   = new FieldType(TextField.TYPE_STORED);
    public static FieldType TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS = new FieldType(StringField.TYPE_NOT_STORED);

    static
    {

        TEXT_STORED_ANALYZED.freeze();

        TEXT_STORED_NOT_INDEXED.setIndexed(false);
        TEXT_STORED_NOT_INDEXED.freeze();

        TEXT_STORED_ANALYZED_NO_NORMS.setOmitNorms(true);
        TEXT_STORED_ANALYZED_NO_NORMS.freeze();

        TEXT_STORED_NOT_ANALYZED.setOmitNorms(false);
        TEXT_STORED_NOT_ANALYZED.freeze();

        TEXT_STORED_NOT_ANALYZED_NO_NORMS.freeze();

        TEXT_NOT_STORED_ANALYZED.setStored(false);
        TEXT_NOT_STORED_ANALYZED.freeze();

        TEXT_NOT_STORED_ANALYZED_NO_NORMS.setStored(false);
        TEXT_NOT_STORED_ANALYZED_NO_NORMS.setOmitNorms(true);
        TEXT_NOT_STORED_ANALYZED_NO_NORMS.freeze();

        TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS.freeze();
    }
}
