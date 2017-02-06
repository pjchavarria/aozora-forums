package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by daniel.soto on 2/6/2017.
 */

@ParseClassName("Post")
public class Post extends ParseObject {


    public static final String THREAD = "thread";
    public static final String PARENTPOST = "parentPost";
    public static final String CREATEDAT = "createdAt";
    public static final String UPDATEDAT = "updatedAt";
    public static final String REPLYLEVEL = "replyLevel";
    public static final String POSTEDBY = "postedBy";
    public static final String LASTREPLY = "lastReply";
    public static final String LASTREPLY_POSTEDBY = "lastReply.postedBy";

}
