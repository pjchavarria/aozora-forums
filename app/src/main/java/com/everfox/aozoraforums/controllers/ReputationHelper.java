package com.everfox.aozoraforums.controllers;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by daniel.soto on 2/9/2017.
 */

public class ReputationHelper {

    private OnGetReputationListener mOnGetReputationCallback;
    public interface OnGetReputationListener {
        public void onGetReputation(List<ParseObject> users);
    }
    private Context context;

    public ReputationHelper(Context context, Fragment followersFragment) {
        this.context = context;
        this.mOnGetReputationCallback = (OnGetReputationListener) followersFragment;
    }

    public void GetTopReputationRank(Boolean active) {

        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.orderByDescending(ParseUserColumns.REPUTATION);
        if(active) {
            Calendar c = Calendar.getInstance();
            query.whereGreaterThan(ParseUserColumns.ACTIVE_START,c.getTime());
        }
        query.setLimit(500);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e==null) {
                    mOnGetReputationCallback.onGetReputation(new ArrayList<ParseObject>(objects));
                }
            }
        });

    }

    public void GetFriendsReputationRank() {

        ArrayList<ParseObject> following = new ArrayList<ParseObject>();
        following.addAll(FriendsController.following);
        following.add(ParseUser.getCurrentUser());
        Collections.sort(following,ReputationComparator);
        mOnGetReputationCallback.onGetReputation(following);

    }

    public static Comparator<ParseObject> ReputationComparator
            = new Comparator<ParseObject>() {

        public int compare(ParseObject user1, ParseObject user2) {
            try {
                Integer reputation;
                if(user1.containsKey(ParseUserColumns.REPUTATION))
                    reputation =  AoUtils.numberToIntOrZero(user1.getNumber(ParseUserColumns.REPUTATION));
                else
                    reputation = 0;
                Integer reputation2;
                if(user2.containsKey(ParseUserColumns.REPUTATION))
                    reputation2 = AoUtils.numberToIntOrZero(user2.getNumber(ParseUserColumns.REPUTATION));
                else
                    reputation2 = 0;
                return reputation2.compareTo(reputation);
            } catch(Exception ex) {
                ex.printStackTrace();
                return 0;
            }
        }

    };
}
