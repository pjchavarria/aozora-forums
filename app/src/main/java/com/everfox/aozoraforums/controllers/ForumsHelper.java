package com.everfox.aozoraforums.controllers;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

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

    private OnGetGlobalThreadsListener mGetGlobalThreadsCallback;
    public interface OnGetGlobalThreadsListener {
        public void onGetGlobalThreads();
    }

    private OnGetThreadsListener mGetThreadsCallback;
    public interface OnGetThreadsListener {
        public void onGetThreads(List<AoThread> threads);
    }

    public ForumsHelper(Context context, Fragment fragment) {
        this.context = context;
        mGetThreadsCallback = (OnGetThreadsListener) fragment;
        mGetGlobalThreadsCallback = (OnGetGlobalThreadsListener) fragment;
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
                    List<String> hiddenThreads = AozoraForumsApp.getHiddenGlobalThreads();
                    ArrayList<AoThread> globalThreads = new ArrayList<AoThread>();

                    for(int i=0;i<objects.size();i++) {
                        objects.get(i).setShowAsPinned(true);
                        if(!hiddenThreads.contains(objects.get(i).getObjectId()))
                            globalThreads.add(objects.get(i));
                    }

                    globalThreads.get(globalThreads.size()-1).setHideDivider(true);
                    AozoraForumsApp.setGlobalThreads(globalThreads);
                    mGetGlobalThreadsCallback.onGetGlobalThreads();
                }
            }
        });
    }

    public void GetThreads(String selectedList, String selectedSort,int skip, int limit) {

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
                if(e== null)
                    mGetThreadsCallback.onGetThreads(objects);
            }
        });


    }

}
