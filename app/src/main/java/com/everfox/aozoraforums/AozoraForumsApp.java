package com.everfox.aozoraforums;

import android.app.Application;

import com.everfox.aozoraforums.models.UserDetails;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class AozoraForumsApp extends Application {

    Integer FacebookRequestCode = 334;

    @Override
    public void onCreate() {
        super.onCreate();
        //Registrar subclases
        ParseObject.registerSubclass(UserDetails.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("AneKeKPLygTGmVmqWsY6totXXTQfk8")
                .clientKey("vvsbzUBBgnPKCoYQlltREy5S0gSIgMfBp34aDrkc")
                .server("http://www.aozoraapp.com/parse/")
                .enableLocalDataStore()
                .build());

        ParseFacebookUtils.initialize(this,FacebookRequestCode);
        ParseInstallation.getCurrentInstallation().saveInBackground();
    }

    private static ParseUser parseFacebookNewUser;
    public static ParseUser getParseFacebookNewUser() {
        return parseFacebookNewUser;
    }

    public static void setParseFacebookNewUser(ParseUser parseFacebookNewUser) {
        AozoraForumsApp.parseFacebookNewUser = parseFacebookNewUser;
    }

    public static void cleanValues() {
        parseFacebookNewUser = null;
    }
}
