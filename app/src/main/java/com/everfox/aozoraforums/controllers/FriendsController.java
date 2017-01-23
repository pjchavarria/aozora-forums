package com.everfox.aozoraforums.controllers;

import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by daniel.soto on 1/23/2017.
 */

public class FriendsController {


    static ArrayList<ParseObject> following;

    private static FriendsController friendsController = new FriendsController();

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private FriendsController() { }

    /* Static 'instance' method */
    public static FriendsController getInstance( ) {
        return friendsController;
    }

    public static void fetchFollowing() {

        following = new ArrayList<>();
        ParseUser currentUser = ParseUser.getCurrentUser();
        ParseQuery<ParseObject> query =  currentUser.getRelation(ParseUserColumns.FOLLOWING) .getQuery();
        query.whereEqualTo(ParseUserColumns.FOLLOWING, currentUser);
        query.selectKeys(Arrays.asList(new String[]{ParseUserColumns.AOZORA_USERNAME,ParseUserColumns.REPUTATION,ParseUserColumns.AVATAR_THUMB}));
        query.setLimit(1000);
        query.orderByDescending(ParseUserColumns.ACTIVE_START);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null && objects != null && objects.size() > 0) {
                    following = (ArrayList<ParseObject>) objects;
                } else {
                    following = null;
                }
            }
        });

    }



}
