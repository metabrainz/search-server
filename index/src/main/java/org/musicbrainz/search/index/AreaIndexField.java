/* Copyright (c) 2013 Paul Taylor
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

package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.FieldType;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum AreaIndexField implements IndexField {

	ID		    ("_id",		    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    AREA        ("area",        MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new MusicbrainzAnalyzer()),
    AREA_ID	    ("aid",		    MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    AREA_STORE  ("areastore",   MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    SORTNAME    ("sortname",    MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new MusicbrainzAnalyzer()),
    TYPE		("type",		MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    ;

    private String name;
    private Analyzer analyzer;
    private FieldType fieldType;

    private AreaIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private AreaIndexField(String name, FieldType fieldType, Analyzer analyzer) {
        this(name, fieldType);
        this.analyzer = analyzer;
    }

    public String getName() {
        return name;
    }

    public Analyzer getAnalyzer() {
        return analyzer;
    }

    public FieldType getFieldType()
    {
        return fieldType;
    }


}