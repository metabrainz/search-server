package org.musicbrainz.search;

import org.apache.lucene.queryParser.QueryParser;

import java.util.List;

/**
 * Fields created in Lucene Search Index
 */
public enum TrackIndexFieldName {

    TRACK_ID("trid"),
    TRACK("track"),
    ARTIST_ID("arid"),
    ARTIST("artist"),
    RELEASE_ID("reid"),
    RELEASE("release"),
    RELEASE_TYPE("type"),
    NUM_TRACKS("tracks"),
    DURATION("dur"),
    QUANTIZED_DURATION("qdur"),
    TRACKNUM("tnum");

    private String fieldname;

    TrackIndexFieldName(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }


}
