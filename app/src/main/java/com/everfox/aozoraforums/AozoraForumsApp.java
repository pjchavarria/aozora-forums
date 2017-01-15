package com.everfox.aozoraforums;

import android.app.Application;

import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AoUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import okhttp3.OkHttpClient;

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
        ParseObject.registerSubclass(TimelinePost.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("AneKeKPLygTGmVmqWsY6totXXTQfk8")
                .clientKey("vvsbzUBBgnPKCoYQlltREy5S0gSIgMfBp34aDrkc")
                .server("http://www.aozoraapp.com/parse/")
                .enableLocalDataStore()
                .build());

        ParseFacebookUtils.initialize(this,FacebookRequestCode);
        ParseInstallation.getCurrentInstallation().saveInBackground();
        OkHttpClient okHttpClient = AoUtils.getUnsafeOkHttpClient();

        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(getApplicationContext(), okHttpClient)
        .build();
        Fresco.initialize(getApplicationContext(), config);
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
