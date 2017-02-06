package com.everfox.aozoraforums;

import android.app.Application;
import android.graphics.Typeface;

import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.models.Anime;
import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.Post;
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

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class AozoraForumsApp extends Application {

    Integer FacebookRequestCode = 334;

    private static AoThread threadToPass;

    public static AoThread getThreadToPass() {
        return threadToPass;
    }

    public static void setThreadToPass(AoThread threadToPass) {
        AozoraForumsApp.threadToPass = threadToPass;
    }

    private static TimelinePost timelinePostToPass;
    public static TimelinePost getTimelinePostToPass() {
        return timelinePostToPass;
    }
    public static void setTimelinePostToPass(TimelinePost _timelinePostToPass) {
        timelinePostToPass = _timelinePostToPass;
    }

    private static ParseUser profileToPass;
    public static ParseUser getProfileToPass() {
        return profileToPass;
    }
    public static void setProfileToPass(ParseUser _profileToPass) {
        profileToPass = _profileToPass;
    }

    static Typeface awesomeTypeface;

    public static Typeface getAwesomeTypeface() {
        return awesomeTypeface;
    }

    public static void setAwesomeTypeface(Typeface _awesomeTypeface) {
        awesomeTypeface = _awesomeTypeface;
    }

    private static List<String> hiddenGlobalThreads;

    public static List<String> getHiddenGlobalThreads() {
        if(hiddenGlobalThreads == null)
            hiddenGlobalThreads = new ArrayList<>();
        return hiddenGlobalThreads;
    }
    public static void setHiddenGlobalThreads(List<String> hiddenGlobalThreads) {
        AozoraForumsApp.hiddenGlobalThreads = hiddenGlobalThreads;
    }

    private static List<AoThread> globalThreads;

    public static List<AoThread> getGlobalThreads() {
        if(globalThreads == null)
            globalThreads = new ArrayList<>();
        return globalThreads;
    }
    public static void setGlobalThreads(List<AoThread> globalThreads) {
        AozoraForumsApp.globalThreads = globalThreads;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Registrar subclases
        ParseObject.registerSubclass(UserDetails.class);
        ParseObject.registerSubclass(TimelinePost.class);
        ParseObject.registerSubclass(PUser.class);
        ParseObject.registerSubclass(AoNotification.class);
        ParseObject.registerSubclass(AoThread.class);
        ParseObject.registerSubclass(AoThreadTag.class);
        ParseObject.registerSubclass(Anime.class);
        ParseObject.registerSubclass(Post.class);

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
        AozoraForumsApp.setAwesomeTypeface(Typeface.createFromAsset(getApplicationContext().getAssets(),"fonts/FontAwesome.ttf"));
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
        timelinePostToPass = null;
        profileToPass = null;
    }
}
