package com.everfox.aozoraforums.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.FirstActivity;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.activities.SettingsActivity;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.models.UserDetails;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;

import okhttp3.OkHttpClient;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class AoUtils {

    public static boolean canAddSimpleLoadingDialog(DialogFragment simpleLoading, FragmentManager fragmentManager) {
        Dialog dialogFrg=simpleLoading.getDialog();
        if(dialogFrg!=null && dialogFrg.isShowing()) {
        } else {
            Fragment prev = fragmentManager.findFragmentByTag("dialog");
            if(prev == null)
                return true;
        }
        return false;
    }

    public static boolean isNetworkAvailable(Context mContext) {
        if(mContext != null) {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }


    public static boolean isActivityInvalid(final Activity context) {
        if (context == null || context.isFinishing() || context.isDestroyed()) {
            return true;
        }
        return false;
    }

    public static void createDetailsIfMissing() {

        final ParseUser user = ParseUser.getCurrentUser();
        if(user.getParseObject("details") == null) {
            ParseQuery<UserDetails> query = ParseQuery.getQuery(UserDetails.class);
            query.whereEqualTo("details",user);
            query.setLimit(1);
            query.getFirstInBackground(new GetCallback<UserDetails>() {
                @Override
                public void done(UserDetails object, ParseException e) {
                    user.put("details",object);
                    user.saveInBackground();
                }
            });
        }
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static int numberToIntOrZero (Number number) {
        if(number == null || number == (Number)0)
            return 0;
        else {
            return number.intValue();
        }
    }
    public static String numberToStringOrZero (Number number) {
        if(number == null || number == (Number)0)
            return "0";
        else {
            return String.valueOf(number.intValue());
        }
    }

    public static OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {

                        @Override
                        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {

                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws java.security.cert.CertificateException {

                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory,(X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {

                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });

            OkHttpClient okHttpClient = builder.build();
            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getOptionListFromID (Context context, Integer optionListID) {
        switch (optionListID){
            case AoConstants.EDITDELETE_POST_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.admin_post_options));
            case AoConstants.MY_PROFILE_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.my_profile_options));
            case AoConstants.USER_LIST_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.user_lists_options));
            case AoConstants.REPUTATION_RANKS_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.reputation_ranks));
            case AoConstants.SORT_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.sort_options));
            case AoConstants.MY_PROFILE_OTHER_USER_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.my_profile_other_user_options));
            case AoConstants.EDIT_POST_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.edit_post_options));
            case AoConstants.REPORT_POST_OPTIONS_DIALOG:
                return Arrays.asList(context.getResources().getStringArray(R.array.other_user_post_options));
        }
        return new ArrayList<String>();
    }

    public static ArrayList<TimelinePost> clearTimelinePostDuplicates(ArrayList<TimelinePost> timelinePosts) {
        ArrayList<TimelinePost> newPostList = new ArrayList<>(timelinePosts);
        ArrayList<Integer> lstIDS = new ArrayList<>();
        for(int i=0;i<newPostList.size();i++) {
            TimelinePost originalPost =(TimelinePost) newPostList.get(i).getParseObject(TimelinePost.REPOST_SOURCE);
            if(originalPost != null) {
                originalPost.setRepostFather(newPostList.get(i));
                newPostList.remove(i);
                newPostList.add(i,originalPost);
            }
        }
        newPostList = new ArrayList<>(new LinkedHashSet<>(newPostList));
        for(int i=0;i<newPostList.size();i++) {
            TimelinePost repostFather =newPostList.get(i).getRepostFather();
            if(repostFather != null) {
                newPostList.remove(i);
                newPostList.add(i,repostFather);
            }
        }
        return newPostList;
    }

    public static ArrayList<AoNotification> filterNotifications(ArrayList<AoNotification> notifications) {
        ArrayList<AoNotification> filteredNotifications = new ArrayList<>();
        for(int i=0;i<notifications.size();i++) {
            AoNotification notification = notifications.get(i);
            List<PUser> pUserList = notification.getList(AoNotification.TRIGGERED_BY);
            if(pUserList == null) {
                filteredNotifications.add(notification);
                break;
            }
            if(pUserList.size() > 1 ||
                    pUserList.size() == 1 && !pUserList.get(0).equals(ParseUser.getCurrentUser())) {
                filteredNotifications.add(notification);
            }
        }
        return filteredNotifications;
    }

    public static String reputationToString(Number number) {
        int reputationInt;
        if(number == null || number == (Number)0)
            reputationInt = 0;
        else {
            reputationInt = number.intValue();
        }
        String reputationString ="";
        if(reputationInt >= 1000000 ) {
            reputationString = String.format("%.1fM",(float) (reputationInt-49)/1000000.0 );
        } else if (reputationInt > 1000) {

            reputationString = String.format("%.1fk",(float) (reputationInt-49)/1000.0 );
        } else {
            reputationString = String.valueOf(reputationInt);
        }
        return reputationString;
    }

    public static OptionListDialogFragment getDialogFragmentMoreOptions(ParseUser userWhoPosted,  Context context, Fragment fragment, Activity activity) {

        int userType = AozoraForumsApp.getIsAdmin(ParseUser.getCurrentUser());
        if(userType == 0) {
            //NormalUser
            if(ParseUser.getCurrentUser().getObjectId().equals(userWhoPosted.getObjectId())) {
                //Himself
                return OptionListDialogFragment.newInstance(context, "Editing post", "Only edit posts if they are breaking guidelines",
                        null, fragment, AoConstants.EDITDELETE_POST_OPTIONS_DIALOG, activity);
            } else {
                //Other
                return  OptionListDialogFragment.newInstance(context, "Warning! Reporting post", "Only report posts if they are breaking guidelines",
                        null, fragment, AoConstants.REPORT_POST_OPTIONS_DIALOG, activity);
            }
        } else if (userType == 1) {

            if(ParseUser.getCurrentUser().getObjectId().equals(userWhoPosted.getObjectId())) {
                //Himself
                return OptionListDialogFragment.newInstance(context, "Editing post", "Only edit posts if they are breaking guidelines",
                        null, fragment, AoConstants.EDITDELETE_POST_OPTIONS_DIALOG, activity);
            } else {
                //Other
                if (AozoraForumsApp.getIsAdmin(userWhoPosted) > 0) {
                    //Admin - TopAdmin
                    return  OptionListDialogFragment.newInstance(context, "Warning! Reporting post", "Only report posts if they are breaking guidelines",
                            null, fragment, AoConstants.REPORT_POST_OPTIONS_DIALOG, activity);
                } else {
                    //Normal Other
                    return OptionListDialogFragment.newInstance(context, "Editing post", "Only edit posts if they are breaking guidelines",
                            null, fragment, AoConstants.EDITDELETE_POST_OPTIONS_DIALOG, activity);
                }
            }
        } else {

            return OptionListDialogFragment.newInstance(context, "Editing post", "Only edit posts if they are breaking guidelines",
                    null, fragment, AoConstants.EDITDELETE_POST_OPTIONS_DIALOG, activity);
        }

    }


    public static ParseUser GetOriginalPoster(TimelinePost parentPost) {

        if(parentPost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
            //OMG ES REPOST SOUND THE FKING ALARM
            TimelinePost repost = (TimelinePost)parentPost.getParseObject(TimelinePost.REPOST_SOURCE);
            return  repost.getParseUser(TimelinePost.POSTED_BY);
        } else {
            return parentPost.getParseUser(TimelinePost.POSTED_BY);
        }
    }


    public static List<String> getRedOptionsDialog(Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.red_options_dialog));
    }

    public static void logout(final Context context) {

        ParseUser user = ParseUser.getCurrentUser();
        if(user != null) {
            user.put(ParseUserColumns.ACTIVE, false);
            user.put(ParseUserColumns.ACTIVE_END, Calendar.getInstance().getTime());
            user.saveInBackground();
        }

        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e== null) {
                    try {
                        if(ParseUser.getCurrentUser() != null)
                            ParseUser.logOut();
                        ParseObject.unpinAll();
                    } catch (ParseException pEx) {
                    }

                    SharedPreferences sharedPreferences = context.getSharedPreferences("com.everfox.aozoraforums", Context.MODE_PRIVATE);
                    sharedPreferences.edit().remove("MAL_User").apply();
                    sharedPreferences.edit().remove("MAL_Password").apply();
                    AozoraForumsApp.cleanValues();
                    Intent intent = new Intent(context, FirstActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }



    public static byte[] getBytesFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    public static void reportObject(ParseObject objectToReport) {
        ParseUser userWhoReported = ParseUser.getCurrentUser();
        objectToReport.addUnique(TimelinePost.REPORTED_BY,userWhoReported);
        objectToReport.increment(TimelinePost.REPORT_COUNT);
        objectToReport.saveInBackground();
        userWhoReported.getParseObject(ParseUserColumns.DETAILS).addUnique(ParseUserColumns.REPORTEDITEMS,objectToReport);
        userWhoReported.getParseObject(ParseUserColumns.DETAILS).increment(ParseUserColumns.REPORT_COUNT);
        userWhoReported.saveInBackground();
    }


    public static void showAlertWithTitleAndText(Context context,String title, String text) {

        new AlertDialog.Builder(context)
                .setTitle("title")
                .setMessage(text)
                .create()
                .show();
    }

    public static TimelinePost GetOriginalPost(TimelinePost parentPost) {

        if(parentPost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
            //OMG ES REPOST SOUND THE FKING ALARM
            return (TimelinePost)parentPost.getParseObject(TimelinePost.REPOST_SOURCE);
        } else {
            return parentPost;
        }
    }



}
