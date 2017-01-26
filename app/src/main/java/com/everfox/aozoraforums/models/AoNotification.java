package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by daniel.soto on 1/25/2017.
 */

@ParseClassName("Notification")
public class AoNotification extends ParseObject {

    public static String LAST_TRIGGERED_BY = "lastTriggeredBy";
    public static String TRIGGERED_BY = "triggeredBy";
    public static String OWNER = "owner";
    public static String READ_BY = "readBy";
    public static String LAST_UPDATED_AT = "lastUpdatedAt";
    public static String SUBSCRIBERS = "subscribers";
    public static String MESSAGE_OWNER = "messageOwner";
    public static String PREVIOUS_MESSAGE = "previousMessage";
    public static String MESSAGE = "message";
    public static String TARGET_ID = "targetID";
    public static String TARGET_CLASS = "targetClass";
}
