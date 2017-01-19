package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.TimelinePost;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.List;

/**
 * Created by daniel.soto on 1/19/2017.
 */

public class PostParseHelper {


    private OnGetTimelinePostCommentsListener mOnGetTimelinePostCommentsCallback;

    public interface OnGetTimelinePostCommentsListener {
        public void onTimelinePostComments(List<TimelinePost> timelinePosts);
    }
    private Context context;

    public PostParseHelper(Context context, Activity timelinePostAct) {
        this.context = context;
        this.mOnGetTimelinePostCommentsCallback = (OnGetTimelinePostCommentsListener) timelinePostAct;
    }

    public void GetTimelinePostComments(TimelinePost timelinePost, int skip, int limit) {

        ParseQuery<TimelinePost> query = ParseQuery.getQuery(TimelinePost.class);
        query.setSkip(skip);
        query.setLimit(limit);
        query.addAscendingOrder(TimelinePost.CREATED_AT);
        query.whereEqualTo(TimelinePost.PARENT_POST,timelinePost);
        query.findInBackground(new FindCallback<TimelinePost>() {
            @Override
            public void done(List<TimelinePost> objects, ParseException e) {
                if(e== null)
                    mOnGetTimelinePostCommentsCallback.onTimelinePostComments(objects);
            }
        });



    }
}
