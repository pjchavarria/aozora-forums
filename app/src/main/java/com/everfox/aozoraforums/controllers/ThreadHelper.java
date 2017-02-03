package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;

import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.TimelinePost;
import com.parse.ParseObject;

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

    public ThreadHelper(Context context, Activity activity) {
        this.context = context;
        this.mOnGetThreadCommentsCallback = (OnGetThreadCommentsListener) activity;
    }

    public void GetThreadComments(AoThread thread, int skip, int limit){

    }
}
