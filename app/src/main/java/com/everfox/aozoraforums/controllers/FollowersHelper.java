package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daniel.soto on 1/27/2017.
 */

public class FollowersHelper {

    private OnGetFollowersListener mOnGetFollowersCallback;
    public interface OnGetFollowersListener {
        public void onGetFollowers(List<PUser> users);
    }
    private Context context;

    public FollowersHelper(Context context, Fragment followersFragment) {
        this.context = context;
        this.mOnGetFollowersCallback = (OnGetFollowersListener) followersFragment;
    }

    public void GetFollowers(ParseUser user) {
        ParseQuery<PUser> userParseQuery = ParseQuery.getQuery(PUser.class);
        userParseQuery.whereEqualTo(ParseUserColumns.FOLLOWING,user);
        userParseQuery.orderByAscending(ParseUserColumns.AOZORA_USERNAME);
        userParseQuery.setLimit(1000);
        userParseQuery.findInBackground(new FindCallback<PUser>() {
            @Override
            public void done(List<PUser> objects, ParseException e) {
                GetRelations(objects);
            }
        });
    }

    private void GetRelations(final List<PUser> users) {

        if(users != null) {
            ArrayList<String> userIDs = new ArrayList<String>();
            for (int i = 0; i < users.size(); i++) {
                users.get(i).setFollowingThisUser(false);
                userIDs.add(users.get(i).getObjectId());
            }

            ParseQuery<ParseObject> relationQuery = ParseUser.getCurrentUser().getRelation(ParseUserColumns.FOLLOWING).getQuery();
            relationQuery.whereContainedIn(ParseUserColumns.OBJECT_ID, userIDs);
            relationQuery.setLimit(1000);
            relationQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {

                    for (int i = 0; i < objects.size(); i++) {
                        (users.get(users.indexOf(objects.get(i)))).setFollowingThisUser(true);
                    }
                    mOnGetFollowersCallback.onGetFollowers(users);
                }
            });
        }
    }


    public void GetFollowing(ParseUser user) {
        ParseQuery<ParseObject> userParseQuery = (ParseQuery<ParseObject>) user.getRelation(ParseUserColumns.FOLLOWING).getQuery();
        userParseQuery.orderByAscending(ParseUserColumns.AOZORA_USERNAME);
        userParseQuery.setLimit(1000);
        userParseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {

                ArrayList<PUser> pUsers = new ArrayList<PUser>();
                for(int i=0;i<objects.size();i++)
                    pUsers.add( (PUser)objects.get(i));
                GetRelations(pUsers);
            }
        });
    }
}
