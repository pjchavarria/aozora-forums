package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.models.TimelinePost;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by daniel.soto on 2/3/2017.
 */

public class ThreadHelper {

    private Context context;

    private OnGetThreadCommentsListener mOnGetThreadCommentsCallback;
    public interface OnGetThreadCommentsListener {
        public void onGetThreadComments(List<ParseObject> comments);
    }
    private OnGetPostCommentsListener mOnGetPostCommentsCallback;
    public interface OnGetPostCommentsListener {
        public void onGetPostComments(List<Post> comments);
    }

    public ThreadHelper(Context context, Activity activity, Fragment fragment) {
        this.context = context;
        this.mOnGetThreadCommentsCallback = (OnGetThreadCommentsListener) activity;
        this.mOnGetPostCommentsCallback = (OnGetPostCommentsListener)fragment;
    }

    public void GetThreadComments(final AoThread thread, int skip, int limit){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        query.setSkip(skip);
        query.setLimit(limit);
        query.whereEqualTo(Post.THREAD,thread);
        query.orderByDescending(Post.UPDATEDAT);
        query.whereEqualTo(Post.REPLYLEVEL,0);
        query.include(Post.POSTEDBY);
        query.include(Post.LASTREPLY);
        query.include(Post.LASTREPLY_POSTEDBY);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e==null)

                    mOnGetThreadCommentsCallback.onGetThreadComments(objects);
            }
        });
    }

    public void GetPostComments(Post post, int skip, int limit){

        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.setSkip(skip);
        query.setLimit(limit);
        query.whereEqualTo(Post.PARENTPOST,post);
        query.orderByAscending(Post.CREATEDAT);
        query.include(Post.POSTEDBY);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> objects, ParseException e) {
                if(e==null)
                    mOnGetPostCommentsCallback.onGetPostComments(objects);
            }
        });
    }
}
