package org.musicbrainz.search;

/**
 * Fields created in Lucene Search Index
 */
public enum LabelIndexFieldName {

    LABEL_ID("laid"),
    LABEL("label"),
    COUNTRY("country"),
    CODE("code"),
    ALIAS("alias"),
    SORTNAME("sortname"),
    BEGIN("begin"),
    END("end"),
    COMMENT("comment"),
    TYPE("type"),;


    private String fieldname;

    LabelIndexFieldName(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }


}