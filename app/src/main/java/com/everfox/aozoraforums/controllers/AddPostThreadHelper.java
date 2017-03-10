package com.everfox.aozoraforums.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.everfox.aozoraforums.activities.TimelinePostActivity;
import com.everfox.aozoraforums.activities.postthread.SearchImageActivity;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.models.LinkData;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by daniel.soto on 3/6/2017.
 */

public class AddPostThreadHelper {

    Activity activity;
    int requestPickImage;
    ParseUser postedBy;
    ParseUser postedIn;
    ParseObject parentPost;
    public static final int CONTENTTYPE_LINK = 0;
    public static final int CONTENTTYPE_IMAGE = 1;
    public static final int CONTENTTYPE_VIDEO = 2;
    int postContentType;
    TimelinePost timelinePost = new TimelinePost();
    int type;
    public static final int NEW_TIMELINEPOST = 0;
    public static final int EDIT_TIMELINEPOST = 1;
    public static final int NEW_TIMELINEPOST_REPLY = 2;
    public static final int EDIT_TIMELINEPOST_REPLY = 3;
    public static final int NEW_AOTHREAD = 4;
    public static final int EDIT_AOTHREAD = 5;
    public static final int NEW_AOTHREAD_REPLY = 6;
    public static final int EDIT_AOTHREAD_REPLY = 7;


    private OnPerformPost mOnPerformPost;
    public interface OnPerformPost {
        public void onPerformPost(ParseObject post, ParseObject parentpost, ParseException e);
    }

    public AddPostThreadHelper(Activity activity, int requestPickImage, ParseUser postedBy, ParseUser postedIn, ParseObject parentPost, int type) {
        this.activity = activity;
        this.requestPickImage = requestPickImage;
        this.postedBy = postedBy;
        this.postedIn = postedIn;
        this.parentPost = parentPost;
        this.type = type;
        mOnPerformPost = (OnPerformPost) activity;
    }

    public void addPhotoGalleryTapped() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasPermission = (ContextCompat.checkSelfPermission(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        requestPickImage);
            } else {
                openGalleryIntent();
            }
        } else
            openGalleryIntent();
    }


    public void openGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, ""), requestPickImage);
    }

    public void  performTimelinePost(final String content, String spoilers, Boolean hasSpoilers, Boolean isEditing, ImageData imageGallery, ImageData imageDataWeb, String youtubeID, LinkData selectedLinkData) {

        timelinePost = TimelinePost.create(TimelinePost.class);
        if(hasSpoilers) {
            timelinePost.put(TimelinePost.CONTENT,content);
            timelinePost.put(TimelinePost.SPOILER_CONTENT,spoilers);
        } else {
            timelinePost.put(TimelinePost.CONTENT,content);
        }

        if(!isEditing) {
            timelinePost.put(TimelinePost.POSTED_BY,postedBy);
            timelinePost.put(TimelinePost.EDITED,false);
        } else {
            timelinePost.put(TimelinePost.EDITED,true);
        }

        timelinePost.put(TimelinePost.HAS_SPOILERS,hasSpoilers);

        try {
            if (imageGallery != null || imageDataWeb != null) {
                if (imageDataWeb != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("width", imageDataWeb.getWidth());
                    jsonObject.put("height", imageDataWeb.getHeight());
                    jsonObject.put("url",imageDataWeb.getUrl());
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    timelinePost.put(TimelinePost.IMAGES, jsonArray);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("property", "image");
                    jsonObject.put("width", imageGallery.getWidth());
                    jsonObject.put("height", imageGallery.getHeight());
                    jsonObject.put("url",imageGallery.getUrl());
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    timelinePost.put(TimelinePost.IMAGES, jsonArray);
                    final ParseFile file = new ParseFile(imageGallery.getImageName(), imageGallery.getImageFile());
                    file.save();
                    timelinePost.put(TimelinePost.IMAGE,file);
                }

                postContentType = CONTENTTYPE_IMAGE;
            } else {
                timelinePost.put(TimelinePost.IMAGES, new JSONArray());
            }
        } catch (JSONException jEx) {

        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(youtubeID != null) {
            timelinePost.put(TimelinePost.YOUTUBE_ID,youtubeID);
            postContentType = CONTENTTYPE_VIDEO;
        }

        if(selectedLinkData != null) {
            timelinePost.put(TimelinePost.LINK,selectedLinkData.toJsonObject());
            postContentType = CONTENTTYPE_LINK;
        }

        if(type == EDIT_TIMELINEPOST_REPLY || type == NEW_TIMELINEPOST_REPLY){
            timelinePost.put(TimelinePost.REPLY_LEVEL,1);
            timelinePost.put(TimelinePost.USER_TIMELINE,postedIn);
            timelinePost.put(TimelinePost.PARENT_POST,parentPost);

        } else {
            timelinePost.put(TimelinePost.REPLY_LEVEL,0);
            timelinePost.put(TimelinePost.USER_TIMELINE,postedIn);
        }

        timelinePost.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e== null){
                    String message = null;
                    String username = postedBy.getString(ParseUserColumns.AOZORA_USERNAME);
                    if(content.length()>1) {
                        message = username + ": " + content;
                    }

                    if(parentPost != null) {
                        parentPost.put(TimelinePost.LAST_REPLY,timelinePost);
                        parentPost.increment(TimelinePost.REPLY_COUNT,1);
                        parentPost.saveInBackground();
                        String parentPostID = parentPost.getObjectId();
                        if (message == null) {
                            switch (postContentType) {
                                case CONTENTTYPE_LINK:
                                    message = username + " replied with a link";
                                    break;
                                case CONTENTTYPE_IMAGE:
                                    message = username + " replied with an image";
                                    break;
                                case CONTENTTYPE_VIDEO:
                                    message = username + " replied with a video";
                                    break;
                                default:
                                    message = username + " replied";
                            }
                        }

                        HashMap<String, String> parameters = new HashMap<String, String>();
                        parameters.put("toUserId", postedIn.getObjectId());
                        parameters.put("timelinePostId", parentPostID);
                        parameters.put("toUserUsername", postedIn.getString(ParseUserColumns.AOZORA_USERNAME));
                        parameters.put("message", message);
                        ParseCloud.callFunctionInBackground("sendNewTimelinePostReplyPushNotificationV2", parameters);
                        postedBy.increment(ParseUserColumns.POST_COUNT, 1);
                    } else {
                        if(message == null) {
                            switch (postContentType) {
                                case CONTENTTYPE_LINK:
                                    message = username + " posted a link ";
                                    break;
                                case CONTENTTYPE_IMAGE:
                                    message = username + " posted an image in your timeline";
                                    break;
                                case CONTENTTYPE_VIDEO:
                                    message = username + " posted a video in your timeline";
                                    break;
                                default:
                                    message = username + " posted in your timeline";
                            }
                        }
                        HashMap<String,String> parameters = new HashMap<String, String>();
                        parameters.put("toUserId",postedIn.getObjectId());
                        parameters.put("timelinePostId",timelinePost.getObjectId());
                        parameters.put("message",message);
                        ParseCloud.callFunctionInBackground("sendNewTimelinePostPushNotificationV2",parameters);
                        postedBy.increment(ParseUserColumns.POST_COUNT,1);
                    }
                }
                mOnPerformPost.onPerformPost(timelinePost, parentPost, e);
            }
        });
    }

}
