package org.musicbrainz.search;

import org.apache.lucene.queryParser.QueryParser;

import java.util.List;

/**
 * Fields created in Lucene Search Index
 */
public enum ReleaseIndexFieldName {

    ARTIST_ID("arid"),
    ARTIST("artist"),
    RELEASE_ID("reid"),
    RELEASE("release"),
    NUM_TRACKS("tracks"),
    BARCODE("barcode"),
    CATALOG_NO("catno"),
    LABEL("label"),
    DATE("date"),
    COUNTRY("country"),
    NUM_DISC_IDS("discids"),
    AMAZON_ID("asin"),
    SCRIPT("script"),
    LANGUAGE("language"),;


    private String fieldname;

    ReleaseIndexFieldName(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }


}