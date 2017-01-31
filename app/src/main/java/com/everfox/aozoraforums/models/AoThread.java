package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by daniel.soto on 1/31/2017.
 */

@ParseClassName("Thread")
public class AoThread extends ParseObject {

    public Boolean getShowAsPinned() {
        return showAsPinned;
    }

    public void setShowAsPinned(Boolean showAsPinned) {
        this.showAsPinned = showAsPinned;
    }

    private Boolean showAsPinned;

    public static String PIN_TYPE = "pinType";
    public static final String HOTRANKING = "hotRanking";
    public static final String TAGS = "tags";
    public static final String STARTEDBY = "startedBy";
    public static final String POSTEDBY = "postedBy";
    public static final String LASTPOSTEDBY = "lastPostedBy";
    public static final String TYPE = "type";
    public static final String SUBTYPE = "subtype";
    public static final String VISIBILITY = "visible";
    public static final String LIKED_BY = "likedBy";
    public static final String CREATED_AT = "createdAt";
}
