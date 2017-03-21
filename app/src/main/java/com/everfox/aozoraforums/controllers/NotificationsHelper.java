package com.everfox.aozoraforums.controllers;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.TimelinePost;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by daniel.soto on 1/25/2017.
 */

public class NotificationsHelper {

    public static final int NOTIFICATION_FETCH_LIMIT = 25;

    private OnGetNotificationListener mOnGetNotificationCallback;

    public interface OnGetNotificationListener {
        public void onGetNotificationListener(List<AoNotification> notifications);
    }
    private Context context;

    public NotificationsHelper (Context context, Fragment notificationFragment) {
        this.context = context;
        this.mOnGetNotificationCallback = (OnGetNotificationListener) notificationFragment;
    }

    public void GetNotifications(ParseUser currentUser, int skip, int limit) {

        ParseQuery<AoNotification> query = ParseQuery.getQuery(AoNotification.class);
        query.setSkip(skip);
        query.setLimit(limit);
        query.orderByDescending(AoNotification.LAST_UPDATED_AT);
        query.include(AoNotification.LAST_TRIGGERED_BY);
        query.include(AoNotification.TRIGGERED_BY);
        query.include(AoNotification.OWNER);
        query.include(AoNotification.READ_BY);
        query.whereEqualTo(AoNotification.SUBSCRIBERS,currentUser);

        ParseQuery<AoNotification> secondQuery = query;

        query.findInBackground(new FindCallback<AoNotification>() {
            @Override
            public void done(List<AoNotification> objects, ParseException e) {
                if(e== null)
                    mOnGetNotificationCallback.onGetNotificationListener(objects);
            }

        });
    }
}
