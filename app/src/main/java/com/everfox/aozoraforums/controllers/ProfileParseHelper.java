package com.everfox.aozoraforums.controllers;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.TimelinePost;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class ProfileParseHelper {


    public static final int PROFILE_SKIP_STEP = 15;
    public static final int PROFILE_FETCH_LIMIT = 15;

    public static final int FOLLOWING_LIST = 0;
    public static final int AOZORA_LIST = 1;
    public static final int PROFILE_LIST = 2;


    private OnGetProfilePostsListener mOnGetProfilePostsCallback;

    public interface OnGetProfilePostsListener {
        public void onGetProfilePosts(List<TimelinePost> timelinePosts);
    }
    private Context context;

    public ProfileParseHelper (Context context, Fragment profileFragment) {
        this.context = context;
        this.mOnGetProfilePostsCallback = (OnGetProfilePostsListener) profileFragment;
    }

    public void GetProfilePosts(ParseUser userProfile, int skip, int limit,int selectedList) {

        ParseQuery<TimelinePost> query = ParseQuery.getQuery(TimelinePost.class);
        query.setSkip(skip);
        query.setLimit(limit);
        query.orderByDescending(TimelinePost.CREATED_AT);
        switch (selectedList){
            case FOLLOWING_LIST:
                break;
            case AOZORA_LIST:
                break;
            case PROFILE_LIST:
                query.whereEqualTo(TimelinePost.USER_TIMELINE,userProfile);
                ArrayList<String> lstVisibility = new ArrayList<String>();
                lstVisibility.add("profile");
                lstVisibility.add("update");
                lstVisibility.add("popular");
                query.whereContainedIn(TimelinePost.VISIBILITY,lstVisibility);
                break;
        }
        query.include(TimelinePost.EPISODE);
        query.include(TimelinePost.POSTED_BY);
        query.include(TimelinePost.USER_TIMELINE);
        query.include(TimelinePost.LAST_REPLY);
        query.include(TimelinePost.LAST_REPLY+"."+ TimelinePost.POSTED_BY);
        query.include(TimelinePost.LAST_REPLY+"."+ TimelinePost.USER_TIMELINE);
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
                mOnGetProfilePostsCallback.onGetProfilePosts(objects);
            }
        });

    }


}
