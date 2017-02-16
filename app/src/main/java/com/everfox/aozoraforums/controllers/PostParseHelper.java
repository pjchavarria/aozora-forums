package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.List;

/**
 * Created by daniel.soto on 1/19/2017.
 */

public class PostParseHelper {


    private PostUtils.OnDeletePostCallback mOnDeletePost;

    private OnGetTimelinePostCommentsListener mOnGetTimelinePostCommentsCallback;
    public interface OnGetTimelinePostCommentsListener {
        public void onTimelinePostComments(List<TimelinePost> timelinePosts);
    }

    private Context context;

    public PostParseHelper(Context context, Activity timelinePostAct) {
        this.context = context;
        this.mOnGetTimelinePostCommentsCallback = (OnGetTimelinePostCommentsListener) timelinePostAct;
        this.mOnDeletePost = (PostUtils.OnDeletePostCallback) timelinePostAct;
    }

    public void GetTimelinePostComments(TimelinePost timelinePost, int skip, int limit) {

        ParseQuery<TimelinePost> query = ParseQuery.getQuery(TimelinePost.class);
        query.setSkip(skip);
        query.setLimit(limit);
        query.addAscendingOrder(TimelinePost.CREATED_AT);

        if(timelinePost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
            query.whereEqualTo(TimelinePost.PARENT_POST,timelinePost.getParseObject(TimelinePost.REPOST_SOURCE));
        } else {
            query.whereEqualTo(TimelinePost.PARENT_POST, timelinePost);

        }
        query.include(TimelinePost.EPISODE);
        query.include(TimelinePost.POSTED_BY);
        query.include(TimelinePost.USER_TIMELINE);
        query.include(TimelinePost.REPOSTED_BY);
        query.include(TimelinePost.REPOST_SOURCE+"."+TimelinePost.EPISODE);
        query.include(TimelinePost.REPOST_SOURCE+"."+TimelinePost.POSTED_BY);
        query.include(TimelinePost.REPOST_SOURCE + "." + TimelinePost.USER_TIMELINE);
        query.include(TimelinePost.REPOST_SOURCE + "." + TimelinePost.LAST_REPLY);
        query.include(TimelinePost.REPOST_SOURCE + "." + TimelinePost.LAST_REPLY+ "." + TimelinePost.POSTED_BY);
        query.include(TimelinePost.REPOST_SOURCE + "." + TimelinePost.LAST_REPLY+ "." + TimelinePost.USER_TIMELINE);
        query.include(TimelinePost.REPOST_SOURCE + "." + TimelinePost.REPOSTED_BY);

        query.findInBackground(new FindCallback<TimelinePost>() {
            @Override
            public void done(List<TimelinePost> objects, ParseException e) {
                if(e== null)
                    mOnGetTimelinePostCommentsCallback.onTimelinePostComments(objects);
            }
        });
    }

    public void deletePost(ParseObject post, ParseObject parseObject) {
        PostUtils.deletePost(post,parseObject,mOnDeletePost);
    }



}
