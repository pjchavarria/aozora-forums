package com.everfox.aozoraforums.controllers;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class ProfileParseHelper {


    public static final int PROFILE_SKIP_STEP = 5;
    public static final int PROFILE_FETCH_LIMIT = 5;

    public static final int FOLLOWING_LIST = 0;
    public static final int AOZORA_LIST = 1;
    public static final int PROFILE_LIST = 2;

    public static final String DARKCIRIUS_ACCOUNT = "Bt5dy11isC";
    public static final String AOZORA_ACCOUNT = "bR0T6mStO";

    PostUtils.OnDeletePostCallback mOnDeletePost;

    private OnGetProfilePostsListener mOnGetProfilePostsCallback;
    public interface OnGetProfilePostsListener {
        public void onGetProfilePosts(List<TimelinePost> timelinePosts);
    }
    private Context context;

    public ProfileParseHelper (Context context, Fragment profileFragment) {
        this.context = context;
        this.mOnGetProfilePostsCallback = (OnGetProfilePostsListener) profileFragment;
        mOnDeletePost = (PostUtils.OnDeletePostCallback) profileFragment;
    }

    public void GetProfilePosts(ParseUser userProfile, int skip, int limit,int selectedList, Date createdDate) {

        ParseQuery<TimelinePost> query = ParseQuery.getQuery(TimelinePost.class);
        query.setSkip(skip);
        query.setLimit(limit);
        query.orderByDescending(TimelinePost.CREATED_AT);
        ArrayList<String> lstVisibility = new ArrayList<String>();
        ArrayList<String> lstPostType = new ArrayList<String>();
        switch (selectedList){
            case FOLLOWING_LIST:

                lstPostType.add("statusUpdate");
                query.whereContainedIn(TimelinePost.TYPE,lstPostType);

                if(FriendsController.following != null) {
                    ArrayList<ParseObject> allUsers = new ArrayList<>();
                    ParseUser darkciriusAccount = ParseUser.createWithoutData(ParseUser.class,DARKCIRIUS_ACCOUNT);
                    ParseUser aozoraAccount = ParseUser.createWithoutData(ParseUser.class,AOZORA_ACCOUNT);
                    allUsers.add(darkciriusAccount);
                    allUsers.add(aozoraAccount);
                    allUsers.addAll( FriendsController.following);
                    query.whereContainedIn(TimelinePost.USER_TIMELINE,allUsers);

                } else {
                    ParseQuery<ParseObject> followingQuery = userProfile.getRelation(ParseUserColumns.FOLLOWING).getQuery();
                    followingQuery.orderByDescending(ParseUserColumns.ACTIVE_START);
                    followingQuery.selectKeys(Arrays.asList(new String[]{ParseUserColumns.OBJECT_ID}));
                    followingQuery.setLimit(1000);
                    query.whereMatchesKeyInQuery(TimelinePost.USER_TIMELINE,TimelinePost.USER_TIMELINE,followingQuery);
                }

                break;
            case AOZORA_LIST:
                lstVisibility.add("sponsored");
                lstVisibility.add("popular");
                query.whereContainedIn(TimelinePost.VISIBILITY,lstVisibility);
                break;
            case PROFILE_LIST:
                query.whereEqualTo(TimelinePost.USER_TIMELINE,userProfile);
                lstVisibility.add("profile");
                lstVisibility.add("update");
                lstVisibility.add("popular");
                query.whereContainedIn(TimelinePost.VISIBILITY,lstVisibility);
                break;
        }

        if(createdDate != null) {
            query.whereLessThan(TimelinePost.CREATED_AT,createdDate);
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
                if(e== null) {
                    for(int i=0;i<objects.size();i++) {
                        objects.get(i).setReplies(null);
                        objects.get(i).setRepostFather(null);
                    }
                    mOnGetProfilePostsCallback.onGetProfilePosts(objects);
                }
            }
        });

    }

    public void deletePost(ParseObject post, ParseObject parseObject) {
        PostUtils.deletePost(post,parseObject,mOnDeletePost);
    }


}
