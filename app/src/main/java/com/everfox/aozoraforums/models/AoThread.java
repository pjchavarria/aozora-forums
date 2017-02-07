package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by daniel.soto on 1/31/2017.
 */

@ParseClassName("Thread")
public class AoThread extends ParseObject {

    public Boolean getHasMenu() {
        if(hasMenu == null)
            return true;
        return hasMenu;
    }

    public void setHasMenu(Boolean hasMenu) {
        this.hasMenu = hasMenu;
    }

    private Boolean hasMenu;

    public Boolean getHideDivider() {
        if(hideDivider == null)
            return false;
        return hideDivider;
    }
    public void setHideDivider(Boolean hideDivider) {
        this.hideDivider = hideDivider;
    }

    private Boolean hideDivider;

    public Boolean getShowAsPinned() {
        if(showAsPinned == null)
            return false;
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
    public static final String SUBTYPE = "subType";
    public static final String VISIBILITY = "visibility";
    public static final String LIKED_BY = "likedBy";
    public static final String CREATED_AT = "createdAt";
    public static final String LIKE_COUNT = "likeCount";
    public static final String UNLIKE_COUNT = "unlikeCount";
    public static final String UNLIKED_BY = "unlikedBy";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String VIEWS = "views";
    public static final String UPDATEDAT = "updatedAt";
}
