package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.soto on 1/12/2017.
 */

@ParseClassName("TimelinePostTest")
public class TimelinePost extends ParseObject implements Serializable {


    private static final long serialVersionUID = -4307875501665953468L;
    private List<TimelinePost> replies = new ArrayList<>();
    public List<TimelinePost> getReplies() {
        return replies;
    }

    public void setReplies(List<TimelinePost> replies) {
        this.replies = replies;
    }

    public TimelinePost getRepostFather() {
        return repostFather;
    }
    public void setRepostFather(TimelinePost repostFather) {
        this.repostFather = repostFather;
    }
    private TimelinePost repostFather;

    public static String NON_SPOILER_CONTENT = "nonSpoilerContent";
    public static String LIKED_BY = "likedBy";
    public static String REPLY_COUNT = "replyCount";
    public static String EDITED = "edited";
    public static String REPOSTED_BY = "repostedBy";
    public static String PARENT_POST = "parentPost";
    public static String REPORTED_BY = "reportedBy";
    public static String SUBSCRIBERS = "subscribers";
    public static String HAS_SPOILERS = "hasSpoilers";
    public static String USER_TIMELINE = "userTimeline";
    public static String REPOST_SOURCE = "repostSource";
    public static String VISIBILITY = "visibility";
    public static String LAST_REPLY = "lastReply";
    public static String POSTED_BY = "postedBy";
    public static String SPOILER_CONTENT = "spoilerContent";
    public static String IMAGES = "images";
    public static String LIKE_COUNT = "likeCount";
    public static String CONTENT = "content";
    public static String REPLY_LEVEL = "replyLevel";
    public static String TYPE = "type";
    public static String LINK = "link";
    public static String REPORT_COUNT = "reportCount";
    public static String IMAGE = "image";
    public static String EPISODE = "episode";
    public static String REPOST_COUNT = "repostCount";
    public static String YOUTUBE_ID = "youtubeID";
    public static String OBJECT_ID = "objectId";

    public static String CREATED_AT = "createdAt";

    public static String TABLE_NAME = "TimelinePost";
}
