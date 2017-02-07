package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by daniel.soto on 2/7/2017.
 */

public class SearchHelper {

    private static int RESULT_MAX = 30;

    private OnGetSearchUsersListener mOnGetSearchUsersCallback;
    public interface OnGetSearchUsersListener {
        public void onGetSearchUsersListener(List<ParseUser> results);
    }

    private OnGetSearchThreadsListener mOnGetSearchThreadCallback;
    public interface OnGetSearchThreadsListener {
        public void onGetSearchThreadCallback(List<AoThread> results);
    }

    private Context context;
    public SearchHelper (Context context, Activity callback) {
        this.context = context;
        this.mOnGetSearchUsersCallback = (OnGetSearchUsersListener) callback;
        this.mOnGetSearchThreadCallback = (OnGetSearchThreadsListener) callback;
    }

    public void SearchUsers(String text) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.setLimit(RESULT_MAX);
        query.whereMatches(ParseUserColumns.AOZORA_USERNAME,"^"+text,"i");
        query.orderByAscending(ParseUserColumns.AOZORA_USERNAME);
        query.include(ParseUserColumns.DETAILS);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e==null)
                    mOnGetSearchUsersCallback.onGetSearchUsersListener(objects);
            }
        });
    }


    public void SearchThreads(String text) {
        ParseQuery<AoThread> query = ParseQuery.getQuery(AoThread.class);
        query.setLimit(RESULT_MAX);
        query.whereMatches(AoThread.TITLE,"^"+text,"i");
        query.whereEqualTo(AoThread.TYPE, AoConstants.USERTHREAD);
        query.orderByAscending(AoThread.UPDATEDAT);
        query.include(AoThread.TAGS);
        query.include(AoThread.STARTEDBY);
        query.include(AoThread.POSTEDBY);
        query.include(AoThread.LASTPOSTEDBY);
        query.findInBackground(new FindCallback<AoThread>() {
            @Override
            public void done(List<AoThread> objects, ParseException e) {
                if(e==null)
                    mOnGetSearchThreadCallback.onGetSearchThreadCallback(objects);
            }
        });
    }



}
