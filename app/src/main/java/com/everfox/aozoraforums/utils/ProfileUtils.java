package com.everfox.aozoraforums.utils;

import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class ProfileUtils {

    static String PRO = "PRO";
    static String PRO_PLUS = "PRO+";


    public static String badgesArrayToPro(JSONArray jsonBadges) {

        try {
            String badge = "";
            if(jsonBadges == null) return badge;
            for (int i = 0; i < jsonBadges.length(); i++) {
                if (jsonBadges.get(i).equals(PRO_PLUS)) {
                    return PRO_PLUS;
                } else if(jsonBadges.get(i).equals(PRO)) {
                    badge = PRO;
                }
            }
            return badge;
        }
        catch (JSONException jEx) {
            return "";
        }
    }
    public static String badgesArrayToBadge(JSONArray jsonBadges) {

        try {
            String badge = "";
            if(jsonBadges == null) return badge;
            for (int i = 0; i < jsonBadges.length(); i++) {
                if (!jsonBadges.get(i).equals(PRO_PLUS) && !jsonBadges.get(i).equals(PRO)) {
                   return (String)jsonBadges.get(i);
                }
            }
            return badge;
        }
        catch (JSONException jEx) {
            return "";
        }
    }
    public static String lastActiveFromUser(ParseUser user){
        return "";
    }
}
