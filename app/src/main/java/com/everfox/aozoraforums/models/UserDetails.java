package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Daniel on 27/10/2015.
 */

@ParseClassName("UserDetails")
public class UserDetails extends ParseObject {

    public static String POSTS = "posts" ;
    public static String DETAILS_USER = "details";
    public static String FOLLOWERS = "followersCount";
    public static String FOLLOWING = "followingCount";
    public static String ABOUT = "about";

}
