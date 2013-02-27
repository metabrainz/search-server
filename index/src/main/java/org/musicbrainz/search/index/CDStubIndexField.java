/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Aurélien Mino

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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.FieldType;
import org.musicbrainz.search.analysis.StripLeadingZeroesAnalyzer;
import org.musicbrainz.search.analysis.TitleAnalyzer;

/**
 * Fields created in Lucene Search Index
 */
public enum CDStubIndexField implements IndexField {

	ID              ("_id",         MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ARTIST          ("artist",      MusicBrainzFieldTypes.TEXT_STORED_ANALYZED),
    TITLE           ("title",       MusicBrainzFieldTypes.TEXT_STORED_ANALYZED, new TitleAnalyzer()),
    BARCODE         ("barcode",     MusicBrainzFieldTypes.TEXT_STORED_ANALYZED_NO_NORMS, new StripLeadingZeroesAnalyzer()),
    COMMENT         ("comment",     MusicBrainzFieldTypes.TEXT_STORED_ANALYZED),
    NUM_TRACKS      ("tracks",      MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    DISCID          ("discid",      MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS, new KeywordAnalyzer()),
    ADDED           ("added",       MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED_NO_NORMS),
    ;

    private String name;
    private Analyzer analyzer;

    private FieldType fieldType;

    private CDStubIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private CDStubIndexField(String name, FieldType fieldType, Analyzer analyzer) {
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