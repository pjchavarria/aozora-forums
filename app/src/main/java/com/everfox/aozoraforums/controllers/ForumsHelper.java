package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.provider.SyncStateContract;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.fragments.ThreadByUserFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by daniel.soto on 1/31/2017.
 */

public class ForumsHelper {

    public static int THREADS_FETCH_LIMIT = 15;
    private static int THREADRISINGMAXUPVOTES = 26;

    private Context context;

    OnBanDeletePostCallback mOnBanDelete;
    public interface OnBanDeletePostCallback {
        public void onDeleteOrBan();
    }

    private OnGetGlobalThreadsListener mGetGlobalThreadsCallback;
    public interface OnGetGlobalThreadsListener {
        public void onGetGlobalThreads();
    }

    private OnGetThreadsListener mGetThreadsCallback;
    public interface OnGetThreadsListener {
        public void onGetThreads(List<AoThread> threads);
    }

    private OnGetUserThreadsListener mGetUserThreadsCallback;
    public interface OnGetUserThreadsListener {
        public void onGetUserThreads(List<AoThread> threads);
    }

    public ForumsHelper(Context context, Fragment fragment, Activity activity) {
        this.context = context;
        if(activity != null)
            mOnBanDelete = (OnBanDeletePostCallback) activity;
        else {
            mOnBanDelete = (OnBanDeletePostCallback) fragment;
            if (fragment instanceof ThreadByUserFragment)
                mGetUserThreadsCallback = (OnGetUserThreadsListener) fragment;
            else {
                mGetThreadsCallback = (OnGetThreadsListener) fragment;
                mGetGlobalThreadsCallback = (OnGetGlobalThreadsListener) fragment;
            }
        }
    }

    public void GetGlobalThreads(){
        ParseQuery<AoThread> query = ParseQuery.getQuery(AoThread.class);
        query.whereEqualTo(AoThread.PIN_TYPE, AoConstants.PINTYPE_GLOBAL);
        query.orderByDescending(AoThread.HOTRANKING);
        query.include(AoThread.TAGS);
        query.include(AoThread.STARTEDBY);
        query.include(AoThread.POSTEDBY);
        query.include(AoThread.LASTPOSTEDBY);
        query.findInBackground(new FindCallback<AoThread>() {
            @Override
            public void done(List<AoThread> objects, ParseException e) {
                if(e==null) {
                    ArrayList<AoThread> globalThreads = new ArrayList<AoThread>();

                    for(int i=0;i<objects.size();i++) {
                        objects.get(i).setShowAsPinned(true);
                        globalThreads.add(objects.get(i));
                    }

                    if(globalThreads.size()>1)
                        globalThreads.get(globalThreads.size()-1).setHideDivider(true);
                    AozoraForumsApp.setGlobalThreads(globalThreads);
                    mGetGlobalThreadsCallback.onGetGlobalThreads();
                }
            }
        });
    }

    public void GetThreads(final String selectedList, String selectedSort, int skip, int limit) {

        ParseQuery<AoThread> query = ParseQuery.getQuery(AoThread.class);
        query.whereEqualTo(AoThread.TYPE,AoConstants.USERTHREAD);
        query.whereEqualTo(AoThread.SUBTYPE, selectedList);
        query.whereEqualTo(AoThread.VISIBILITY, AoConstants.VISIBLE);
        query.setSkip(skip);
        query.setLimit(limit);
        // FILTERING FAVORITES
        query.include(AoThread.TAGS);
        query.include(AoThread.STARTEDBY);
        query.include(AoThread.POSTEDBY);
        query.include(AoThread.LASTPOSTEDBY);

        switch (selectedSort) {
            case AoConstants.POPULAR:
                query.orderByDescending(AoThread.HOTRANKING);
                break;
            case AoConstants.RISING:
                query.orderByDescending(AoThread.HOTRANKING);
                query.whereLessThan(AoThread.LIKE_COUNT,THREADRISINGMAXUPVOTES);
                break;
            case AoConstants.NEW:
                query.orderByDescending(AoThread.CREATED_AT);
                break;
        }

        query.findInBackground(new FindCallback<AoThread>() {
            @Override
            public void done(List<AoThread> objects, ParseException e) {
                if(e== null) {
                    mGetThreadsCallback.onGetThreads(objects);
                }
            }
        });

    }

    public void GetUserThreads(ParseUser user, int skip, int limit) {

        ParseQuery<AoThread> query = ParseQuery.getQuery(AoThread.class);
        query.whereEqualTo(AoThread.POSTEDBY,user);
        query.setSkip(skip);
        query.setLimit(limit);
        query.orderByDescending(AoThread.CREATED_AT);
        // FILTERING FAVORITES
        query.include(AoThread.TAGS);
        query.include(AoThread.STARTEDBY);
        query.include(AoThread.POSTEDBY);
        query.include(AoThread.LASTPOSTEDBY);

        query.findInBackground(new FindCallback<AoThread>() {
            @Override
            public void done(List<AoThread> objects, ParseException e) {
                if(e== null) {
                    mGetUserThreadsCallback.onGetUserThreads(objects);
                }
            }
        });

    }

    public void deleteThread (final AoThread thread) {
        ParseQuery<Post> postParseQuery = ParseQuery.getQuery(Post.class);
        postParseQuery.whereEqualTo(Post.THREAD,thread);
        postParseQuery.include(Post.POSTEDBY);
        postParseQuery.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(final List<Post> objects, ParseException e) {

                if(e== null) {

                    ParseObject.deleteAllInBackground(objects, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e==null) {
                                thread.getParseUser(AoThread.POSTEDBY).increment(ParseUserColumns.POST_COUNT, -1);
                                for (int i = 0; i < objects.size(); i++) {
                                    objects.get(i).getParseUser(AoThread.POSTEDBY).increment(ParseUserColumns.POST_COUNT, -1);
                                    objects.get(i).getParseUser(AoThread.POSTEDBY).saveInBackground();
                                }
                                thread.getParseUser(AoThread.POSTEDBY).saveInBackground();
                                thread.deleteInBackground(new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {

                                        mOnBanDelete.onDeleteOrBan();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

    }

    public void banThread (AoThread thread) {
        thread.put(AoThread.VISIBILITY,AoConstants.HIDDEN);
        thread.saveInBackground();
        mOnBanDelete.onDeleteOrBan();
    }
}
