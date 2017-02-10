package com.everfox.aozoraforums.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.text.Spanned;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.models.UserDetails;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static OptionListDialogFragment getDialogFragmentMoreOptions(ParseUser user, Context context, Fragment fragment, Activity activity) {
        if(AozoraForumsApp.getIsAdmin()) {
            if (user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                return OptionListDialogFragment.newInstance(context, "Editing post", "Only edit posts if they are breaking guidelines",
                        null, fragment, AoConstants.EDITDELETE_POST_OPTIONS_DIALOG, activity);
            else
                return  OptionListDialogFragment.newInstance(context, "Warning! Editing post", "Only edit posts if they are breaking guidelines",
                        null, fragment, AoConstants.EDITDELETE_POST_OPTIONS_DIALOG, activity);
        } else {

            if (user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId()))
                return  OptionListDialogFragment.newInstance(context, "Editing post", "Only edit posts if they are breaking guidelines",
                        null, fragment, AoConstants.EDITDELETE_POST_OPTIONS_DIALOG, activity);
            else
                return  OptionListDialogFragment.newInstance(context, "Warning! Reporting post", "Only report posts if they are breaking guidelines",
                        null, fragment, AoConstants.REPORT_POST_OPTIONS_DIALOG, activity);
        }
    }

    public static List<String> getRedOptionsDialog(Context context) {
        return Arrays.asList(context.getResources().getStringArray(R.array.red_options_dialog));
    }
}
