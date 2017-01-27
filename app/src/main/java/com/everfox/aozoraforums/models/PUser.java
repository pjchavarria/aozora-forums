package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by daniel.soto on 1/23/2017.
 */

@ParseClassName("_User")
public class PUser extends ParseUser {

    private Boolean isFollowingThisUser;

    public Boolean getFollowingThisUser() {
        if(isFollowingThisUser == null)
            return false;
        return isFollowingThisUser;
    }

    public void setFollowingThisUser(Boolean followingThisUser) {
        isFollowingThisUser = followingThisUser;
    }
}
