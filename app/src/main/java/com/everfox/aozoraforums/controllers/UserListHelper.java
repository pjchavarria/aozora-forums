package com.everfox.aozoraforums.controllers;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by daniel.soto on 2/10/2017.
 */

public class UserListHelper {


    private OnGetUsersListener mOnGetUsersListener;
    public interface OnGetUsersListener {
        public void onGetUsersListener(List<PUser> users);
    }
    private Context context;

    public UserListHelper (Context context, Fragment fragment) {
        this.context = context;
        this.mOnGetUsersListener = (OnGetUsersListener) fragment;
    }

    public void GetOnlineNow() {

        ParseQuery<PUser> query = ParseQuery.getQuery(PUser.class);
        query.whereExists(ParseUserColumns.AOZORA_USERNAME);
        query.whereEqualTo(ParseUserColumns.ACTIVE,true);
        query.setLimit(500);
        query.findInBackground(new FindCallback<PUser>() {
            @Override
            public void done(List<PUser> objects, ParseException e) {
                if(e==null)
                    mOnGetUsersListener.onGetUsersListener(objects);
            }
        });
    }

    public void GetNewUsers() {

        ParseQuery<PUser> query = ParseQuery.getQuery(PUser.class);
        query.whereExists(ParseUserColumns.AOZORA_USERNAME);
        query.orderByDescending(ParseUserColumns.JOIN_DATE);
        query.setLimit(500);
        query.findInBackground(new FindCallback<PUser>() {
            @Override
            public void done(List<PUser> objects, ParseException e) {
                if(e==null)
                    mOnGetUsersListener.onGetUsersListener(objects);
            }
        });
    }

    public void GetAozoraStaff() {

        ArrayList<String> badges = new ArrayList<>();
        badges.add("Admin");
        badges.add("Mod");
        ParseQuery<PUser> query = ParseQuery.getQuery(PUser.class);
        query.whereExists(ParseUserColumns.AOZORA_USERNAME);
        query.whereContainedIn(ParseUserColumns.BADGES,badges);
        query.setLimit(1000);
        query.findInBackground(new FindCallback<PUser>() {
            @Override
            public void done(List<PUser> objects, ParseException e) {
                if(e==null)
                    mOnGetUsersListener.onGetUsersListener(objects);
            }
        });
    }

    public void GetNewProMembers() {

        ArrayList<String> badges = new ArrayList<>();
        badges.add("PRO");
        badges.add("PROPlus");
        ParseQuery<PUser> query = ParseQuery.getQuery(PUser.class);
        query.whereExists(ParseUserColumns.AOZORA_USERNAME);
        query.whereContainedIn(ParseUserColumns.BADGES,badges);
        query.orderByDescending(ParseUserColumns.CREATED_AT);
        query.setLimit(200);
        executeQueryFindRelations(query);
    }

    public void GetOldestActiveUsers() {

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR,-2);

        ArrayList<String> badges = new ArrayList<>();
        badges.add("Admin");
        badges.add("Mod");
        ParseQuery<PUser> query = ParseQuery.getQuery(PUser.class);
        query.whereExists(ParseUserColumns.AOZORA_USERNAME);
        query.whereContainedIn(ParseUserColumns.BADGES,badges);
        query.whereGreaterThan(ParseUserColumns.ACTIVE_START,calendar.getTime());
        query.setLimit(1000);
        executeQueryFindRelations(query);
    }

    private void executeQueryFindRelations(ParseQuery<PUser> query) {

        query.findInBackground(new FindCallback<PUser>() {
            @Override
            public void done(final List<PUser> users, ParseException e) {

                if(e==null) {
                    ArrayList<String> userIDs = new ArrayList<String>();
                    for(int i=0;i<users.size();i++) {
                        users.get(i).setFollowingThisUser(false);
                        userIDs.add(users.get(i).getObjectId());
                    }

                    ParseQuery<ParseObject> relationQuery = ParseUser.getCurrentUser().getRelation(ParseUserColumns.FOLLOWING).getQuery();
                    relationQuery.whereContainedIn(ParseUserColumns.OBJECT_ID,userIDs);
                    relationQuery.setLimit(1000);
                    relationQuery.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> objects, ParseException e) {

                            for(int i=0;i<objects.size();i++) {
                                (users.get(users.indexOf(objects.get(i))) ).setFollowingThisUser(true);
                            }
                            mOnGetUsersListener.onGetUsersListener(users);
                        }
                    });
                }
            }
        });

    }


}
