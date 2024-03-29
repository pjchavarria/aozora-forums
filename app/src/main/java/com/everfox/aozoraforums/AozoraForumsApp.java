package com.everfox.aozoraforums;

import android.app.Application;
import android.graphics.Typeface;

import com.crashlytics.android.Crashlytics;
import com.everfox.aozoraforums.models.Anime;
import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PurchaseUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import io.fabric.sdk.android.Fabric;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class AozoraForumsApp extends Application {

    Integer FacebookRequestCode = 334;

    private static float density;

    public static float getDensity() {
        return density;
    }

    public static void setDensity(float density) {
        AozoraForumsApp.density = density;
    }

    private static int screenWidth;
    public static int getScreenWidth() {
        return screenWidth;
    }
    public static void setScreenWidth(int screenWidth) {
        AozoraForumsApp.screenWidth = screenWidth;
    }

    private static ParseObject updatedPost;

    public static ParseObject getUpdatedPost() {
        return updatedPost;
    }

    public static void setUpdatedPost(ParseObject updatedPost) {
        AozoraForumsApp.updatedPost = updatedPost;
    }


    public static ParseObject getUpdatedParentThread() {
        return updatedParentThread;
    }

    public static void setUpdatedParentThread(ParseObject updatedParentThread) {
        AozoraForumsApp.updatedParentThread = updatedParentThread;
    }

    private static ParseObject updatedParentThread;

    private static ParseObject updatedParentPost;
    public static ParseObject getUpdatedParentPost() {
        return updatedParentPost;
    }
    public static void setUpdatedParentPost(ParseObject updatedParentPost) {
        AozoraForumsApp.updatedParentPost = updatedParentPost;
    }

    private static ParseUser postedBy;
    private static ParseUser postedIn;
    public static ParseUser getPostedBy() {
        return postedBy;
    }
    public static void setPostedBy(ParseUser postedBy) {
        AozoraForumsApp.postedBy = postedBy;
    }
    public static ParseUser getPostedIn() {
        return postedIn;
    }
    public static void setPostedIn(ParseUser postedIn) {
        AozoraForumsApp.postedIn = postedIn;
    }
    private static ParseObject tagToPass;
    public static ParseObject getTagToPass() {
        return tagToPass;
    }

    public static void setTagToPass(ParseObject tagToPass) {
        AozoraForumsApp.tagToPass = tagToPass;
    }

    private static ParseObject postToUpdate;

    public static ParseObject getPostToUpdate() {
        return postToUpdate;
    }

    public static void setPostToUpdate(ParseObject postToUpdate) {
        AozoraForumsApp.postToUpdate = postToUpdate;
    }

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

    private static List<AoThread> hiddenGlobalThreads;
    public static List<AoThread> getHiddenGlobalThreads() {
        if(hiddenGlobalThreads == null)
            hiddenGlobalThreads = new ArrayList<>();
        return hiddenGlobalThreads;
    }
    public static void setHiddenGlobalThreads(List<AoThread> hiddenGlobalThreads) {
        AozoraForumsApp.hiddenGlobalThreads = hiddenGlobalThreads;
    }


    private static List<AoThread> globalThreads;
    public static List<AoThread> getGlobalThreads() {
        if(globalThreads == null)
            globalThreads = new ArrayList<>();
        else {
            List<AoThread> hiddenGlobal = getHiddenGlobalThreads();
            for(int i=0;i<hiddenGlobal.size();i++) {
                if(globalThreads.contains(hiddenGlobal.get(i))) {
                    globalThreads.remove(hiddenGlobal.get(i));
                }
            }
        }
        return globalThreads;
    }
    public static void setGlobalThreads(List<AoThread> globalThreads) {
        AozoraForumsApp.globalThreads = globalThreads;
    }

    private static int userType = -1;

    public static int getIsAdmin(ParseUser user) {
        if(userType == -1) {
            JSONArray jsonArray = user.getJSONArray(ParseUserColumns.BADGES);
            if(jsonArray == null) {
                userType = 0;
                return userType;
            }
            if (jsonArray.toString().contains("Admin")) {
                userType = 2;
                return userType;
            }
            if (jsonArray.toString().contains("Mod"))
                userType = 1;
            else
                userType = 0;
        }
        return userType;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
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
        threadToPass = null;
        profileToPass = null;
        hiddenGlobalThreads = null;
        globalThreads = null;
        postedBy = null;
        postedIn = null;
        tagToPass = null;
        updatedParentPost = null;
        updatedParentThread = null;
        postToUpdate = null;
    }
}
