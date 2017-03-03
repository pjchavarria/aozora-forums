package com.everfox.aozoraforums.models;

import com.everfox.aozoraforums.controllers.FollowersHelper;
import com.everfox.aozoraforums.controllers.FriendsController;
import com.parse.ParseClassName;
import com.parse.ParseCloud;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel.soto on 1/23/2017.
 */

@ParseClassName("_User")
public class PUser extends ParseUser implements Serializable {

    private static final long serialVersionUID = -3051425209789228271L;
    private Boolean isFollowingThisUser;

    public Boolean getFollowingThisUser() {
        if(isFollowingThisUser == null)
            return false;
        return isFollowingThisUser;
    }

    public void setFollowingThisUser(Boolean followingThisUser) {
        isFollowingThisUser = followingThisUser;
    }

    public static void followUser(ParseObject user, Boolean follow){

        int count = 0;
        ArrayList<ParseObject> following = FriendsController.following;
        ParseUser currentUser =  ParseUser.getCurrentUser();
        if(following == null) following = new ArrayList<>();
        if(follow) {
            following.add(user);
            ParseRelation<ParseObject> relation = currentUser.getRelation(ParseUserColumns.FOLLOWING);
            relation.add(user);
            count = 1;
            HashMap<String,String> parametersMap = new HashMap<>();
            parametersMap.put("toUser",user.getObjectId());
            ParseCloud.callFunctionInBackground("sendFollowingPushNotificationV2",parametersMap);
            FriendsController.following = following;
        } else {
            following.remove(user);
            count = -1;
            ParseRelation<ParseObject> relation = currentUser.getRelation(ParseUserColumns.FOLLOWING);
            relation.remove(user);
            FriendsController.following = following;
        }

        //setFollowingThisUser = follow
        if(user instanceof PUser)
            ((PUser)user).setFollowingThisUser(follow);
        user.getParseObject("details").increment(UserDetails.FOLLOWERS,count);
        user.saveInBackground();
        currentUser.getParseObject("details").increment(UserDetails.FOLLOWING,count);
        currentUser.saveInBackground();
    }

    public static Boolean isMuted(ParseUser parseUser){
        Date mutedUntil = parseUser.getParseObject("details").getDate("mutedUntil");
        Date calendar = Calendar.getInstance().getTime();

        if(mutedUntil != null && calendar.before(mutedUntil)){
            return true;
        }
        return false;

    }
}
