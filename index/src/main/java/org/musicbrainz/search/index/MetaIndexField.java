/*
 * MusicBrainz Search Server
 * Copyright (C) 2009  Paul Taylor

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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;

/**
 * Fields common to all Lucene Search Index
 */
public enum MetaIndexField implements IndexField {

    LAST_UPDATED			("index_lastupdate",	MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED),
    REPLICATION_SEQUENCE	("index_repseq",		MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED),
    SCHEMA_SEQUENCE			("index_schseq",		MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED),
    LAST_CHANGE_SEQUENCE	("index_changeseq",		MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED),
    // Dumb field always filled with '1', but that easily allow to find the meta document
    META					("index_meta",			MusicBrainzFieldTypes.TEXT_STORED_NOT_ANALYZED),
    ;

    private String name;
    private Analyzer analyzer;
    private FieldType fieldType;

    public static final String META_VALUE = "1";

    private MetaIndexField(String name, FieldType fieldType) {
        this.name = name;
        this.fieldType=fieldType;
    }

    private MetaIndexField(String name, FieldType fieldType, Analyzer analyzer) {
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

