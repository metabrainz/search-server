package org.musicbrainz.search;

import org.apache.lucene.queryParser.QueryParser;

import java.util.List;

/**
 * Fields created in Lucene Search Index
 */
public enum ArtistIndexFieldName {
    ALIAS("alias"),
    ARTIST_ID("arid"),
    ARTIST("artist"),
    SORTNAME("sortname"),
    BEGIN("begin"),
    END("end"),
    COMMENT("comment"),
    TYPE("type"),;


    private String fieldname;

    ArtistIndexFieldName(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }


}