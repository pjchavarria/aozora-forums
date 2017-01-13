package com.everfox.aozoraforums.utils;

import com.everfox.aozoraforums.models.ParseUserColumns;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Calendar;
import java.util.Date;

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
        if(user.getBoolean(ParseUserColumns.ACTIVE))
            return "ACTIVE NOW";
        else {
            Date dateCreated = user.getDate(ParseUserColumns.ACTIVE_END);
            Date currentDate = Calendar.getInstance().getTime();


            int secondsDiff = (int) DateUtils.getSecondsDiff(dateCreated,currentDate);
            int weeksAgo =  (secondsDiff/(7*24*60*60));
            if ( weeksAgo > 0) {
                return String.valueOf(weeksAgo) + " " + (weeksAgo == 1? "WEEK AGO" : "WEEKS AGO");
            }
            int daysAgo =  (secondsDiff/(24*60*60));
            if ( daysAgo > 0) {
                return String.valueOf(daysAgo) + " " + (daysAgo == 1? "DAY AGO" : "DAYS AGO");
            }
            int hoursAgo =  (secondsDiff/(60*60));
            if ( hoursAgo > 0) {
                return String.valueOf(hoursAgo) + " " + (hoursAgo == 1? "HR AGO" : "HRS AGO");
            }
            int minutesAgo =  (secondsDiff/(60));
            if ( minutesAgo > 0) {
                return String.valueOf(minutesAgo) + " " + (minutesAgo == 1? "MIN AGO" : "MINS AGO");
            }
            return "ACTIVE NOW";
        }
    }
}
