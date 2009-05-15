package org.musicbrainz.search;

/**
 * Created by IntelliJ IDEA.
 * User: paul
 * Date: May 14, 2009
 * Time: 4:44:49 PM
 * To change this template use File | Settings | File Templates.
 */
public enum LabelType {
    UNKNOWN("unknown"),
    DISTRIBUTOR("distributor"),
    HOLDING("holding"),
    PRODUCTION("production"),
    ORIGINAL_PROD("orig. prod."),
    BOOTLEG_PROD("bootleg prod."),
    REISSUE_PROD("reissue prod."),
    PUBLISHER("publisher"),;

    private String fieldname;

    LabelType(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFieldname() {
        return fieldname;
    }

}

