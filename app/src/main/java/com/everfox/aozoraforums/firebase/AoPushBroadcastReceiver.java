package com.everfox.aozoraforums.firebase;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by daniel.soto on 2/23/2017.
 */

public class AoPushBroadcastReceiver extends ParsePushBroadcastReceiver {

    public static String lastNotificationMessage = "";

    @Override
    protected void onPushReceive(Context context, Intent intent) {

        JSONObject pushData = null;
        try {
            pushData = new JSONObject(intent.getStringExtra(KEY_PUSH_DATA));
        } catch (JSONException e) {

        }

        if (pushData == null || (!pushData.has("alert") && !pushData.has("title"))) {
           super.onPushReceive(context,intent);
        }
        String currentNotificationMessage = pushData.optString("alert", "Notification received.");
        if(lastNotificationMessage.equals(currentNotificationMessage))
            return;
        else {
            lastNotificationMessage = currentNotificationMessage;
            super.onPushReceive(context, intent);

        }

    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        super.onPushOpen(context, intent);
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        super.onPushDismiss(context, intent);
    }
}
