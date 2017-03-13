package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;

import com.everfox.aozoraforums.models.Anime;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.List;
import java.util.Random;

/**
 * Created by daniel.soto on 3/13/2017.
 */

public class ThreadTagHelper {



    Context context;
    private OnGetSearchAnimeListener mOnGetSearchAnimeCallback;
    public interface OnGetSearchAnimeListener {
        public void onGetSearchAnimeCallback(List<Anime> results);
    }

    private OnGetSearchTagsListener mOnGetSearchTagsCallback;
    public interface OnGetSearchTagsListener {
        public void onGetSearchTagsListener(List<AoThreadTag> results);
    }

    public ThreadTagHelper (Context context, Activity callback) {
        this.context = context;
        this.mOnGetSearchAnimeCallback = (OnGetSearchAnimeListener) callback;
        this.mOnGetSearchTagsCallback = (OnGetSearchTagsListener) callback;
    }

    public void SearchTags ( Boolean isAdmin) {
        ParseQuery<AoThreadTag> query = ParseQuery.getQuery(AoThreadTag.class);
        query.orderByAscending("order");
        query.whereEqualTo("visible",true);
        if(isAdmin)
            query.whereEqualTo("adminVisible",true);
        else
            query.whereEqualTo("privateTag",false);

        query.findInBackground(new FindCallback<AoThreadTag>() {
            @Override
            public void done(List<AoThreadTag> objects, ParseException e) {
                mOnGetSearchTagsCallback.onGetSearchTagsListener(objects);
            }
        });
    }

    public void SearchAnimes(String text) {
        ParseQuery<Anime> query = ParseQuery.getQuery(Anime.class);
        query.setLimit(10);
        //DELETE
        Random r = new Random();
        query.include("details");
        query.whereMatches("title","^"+text,"i");
        query.setLimit(30);
        query.findInBackground(new FindCallback<Anime>() {
            @Override
            public void done(List<Anime> objects, ParseException e) {
                if(e== null)
                    mOnGetSearchAnimeCallback.onGetSearchAnimeCallback(objects);
            }
        });
    }
}
