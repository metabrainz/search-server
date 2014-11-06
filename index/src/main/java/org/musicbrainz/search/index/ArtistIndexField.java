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

package org.musicbrainz.search.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.musicbrainz.search.analysis.CaseInsensitiveKeywordAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzKeepAccentsAnalyzer;
import org.musicbrainz.search.analysis.MusicbrainzWithPosGapAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum ArtistIndexField implements IndexField {

	ID			    ("_id",			MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ALIAS		    ("alias",		MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    ARTIST_ID	    ("arid",		MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST		    ("artist",		MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new MusicbrainzAnalyzer()),
    ARTIST_ACCENT   ("artistaccent",MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzKeepAccentsAnalyzer()),
    BEGIN		    ("begin",		MusicBrainzFieldTypes.TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    COMMENT		    ("comment",		MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED),
    COUNTRY         ("country",     MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    END			    ("end",			MusicBrainzFieldTypes.TEXT_NOT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ENDED           ("ended",       MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    GENDER          ("gender",      MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    IPI             ("ipi",         MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    ISNI            ("isni",        MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    SORTNAME	    ("sortname",	MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED),
    TAG		        ("tag",		    MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzWithPosGapAnalyzer()),
    TYPE		    ("type",		MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED_NO_NORMS, new CaseInsensitiveKeywordAnalyzer()),
    AREA		    ("area",		MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzAnalyzer()),
    BEGIN_AREA		("beginarea",   MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzAnalyzer()),
    END_AREA		("endarea",	    MusicBrainzFieldTypes.TEXT_NOT_STORED_ANALYZED, new MusicbrainzAnalyzer()),
    ARTIST_STORE    ("artiststore", MusicBrainzFieldTypes.TEXT_STORED_NOT_INDEXED),
    ;


    private String name;
    private Analyzer analyzer;
    private FieldType fieldType;

    private ArtistIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private ArtistIndexField(String name, FieldType fieldType, Analyzer analyzer) {
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