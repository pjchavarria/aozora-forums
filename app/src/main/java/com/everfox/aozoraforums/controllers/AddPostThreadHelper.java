package com.everfox.aozoraforums.controllers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.everfox.aozoraforums.activities.TimelinePostActivity;
import com.everfox.aozoraforums.activities.postthread.CreatePostActivity;
import com.everfox.aozoraforums.activities.postthread.SearchImageActivity;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.models.LinkData;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
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

import java.util.Arrays;
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
    ParseObject parentThread;
    public static final int CONTENTTYPE_LINK = 0;
    public static final int CONTENTTYPE_IMAGE = 1;
    public static final int CONTENTTYPE_VIDEO = 2;
    int postContentType;
    TimelinePost timelinePost = new TimelinePost();
    Post post = new Post();
    int type;
    public static final int NEW_TIMELINEPOST = 0;
    public static final int EDIT_TIMELINEPOST = 1;
    public static final int NEW_TIMELINEPOST_REPLY = 2;
    public static final int EDIT_TIMELINEPOST_REPLY = 3;
    public static final int NEW_AOTHREAD = 4;
    public static final int EDIT_AOTHREAD = 5;
    public static final int NEW_AOTHREAD_REPLY = 6;
    public static final int EDIT_AOTHREAD_REPLY = 7;

    private OnPerformNewThread mOnPerformNewThread;
    public interface OnPerformNewThread {
        public void onPerformNewThread(AoThread thread ,ParseException e);
    }

    private OnPerformPost mOnPerformPost;
    public interface OnPerformPost {
        public void onPerformPost(ParseObject post, ParseObject parentpost, ParseException e);
    }

    public AddPostThreadHelper(Activity activity,  int requestPickImage, ParseUser postedBy, ParseUser postedIn, ParseObject parentPost, ParseObject parentThread, int type) {
        this.activity = activity;
        this.requestPickImage = requestPickImage;
        this.postedBy = postedBy;
        this.postedIn = postedIn;
        this.parentPost = parentPost;
        this.parentThread = parentThread;
        this.type = type;
        mOnPerformPost = (OnPerformPost) activity;
        if (activity instanceof CreatePostActivity)
            mOnPerformNewThread = (OnPerformNewThread) activity;

    }

    public void setFragmentCallback (Fragment fragmentCallback) {
        mOnPerformPost = (OnPerformPost) fragmentCallback;
        mOnPerformNewThread = null;
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

    public void performTimelinePost(final String content, String spoilers, Boolean hasSpoilers, Boolean isEditing, ImageData imageGallery, ImageData imageDataWeb, String youtubeID, LinkData selectedLinkData) {

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
                    file.saveInBackground();
                    timelinePost.put(TimelinePost.IMAGE,file);
                }

                postContentType = CONTENTTYPE_IMAGE;
            } else {
                timelinePost.put(TimelinePost.IMAGES, new JSONArray());
            }
        } catch (JSONException jEx) {

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

    public void performNewThread(final String content, String title,  ImageData imageGallery,
                              ImageData imageDataWeb, String youtubeID, LinkData selectedLinkData, ParseObject tag) {

        final AoThread aoThread = AoThread.create(AoThread.class);
        aoThread.put(AoThread.EDITED,false);
        aoThread.put(AoThread.REPLIES_COUNT,0);
        aoThread.put(AoThread.LIKE_COUNT,1);
        aoThread.put(AoThread.LIKED_BY, Arrays.asList(postedBy));
        aoThread.put(AoThread.LASTPOSTEDBY,postedBy);
        aoThread.put(AoThread.POSTEDBY,postedBy);

        //updateThread
        aoThread.put(AoThread.TITLE,title);
        aoThread.put(AoThread.CONTENT,content);
        aoThread.put(AoThread.TAGS,Arrays.asList(tag));
        try {
            if (imageGallery != null || imageDataWeb != null) {
                if (imageDataWeb != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("width", imageDataWeb.getWidth());
                    jsonObject.put("height", imageDataWeb.getHeight());
                    jsonObject.put("url",imageDataWeb.getUrl());
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    aoThread.put(TimelinePost.IMAGES, jsonArray);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("property", "image");
                    jsonObject.put("width", imageGallery.getWidth());
                    jsonObject.put("height", imageGallery.getHeight());
                    jsonObject.put("url",imageGallery.getUrl());
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    aoThread.put(TimelinePost.IMAGES, jsonArray);
                    final ParseFile file = new ParseFile(imageGallery.getImageName(), imageGallery.getImageFile());
                    file.saveInBackground();
                    aoThread.put(TimelinePost.IMAGE,file);
                }

                postContentType = CONTENTTYPE_IMAGE;
            } else {
                aoThread.put(TimelinePost.IMAGES, new JSONArray());
            }
        } catch (JSONException jEx) {

        }

        if(youtubeID != null) {
            aoThread.put(TimelinePost.YOUTUBE_ID,youtubeID);
            postContentType = CONTENTTYPE_VIDEO;
        }

        if(selectedLinkData != null) {
            aoThread.put(TimelinePost.LINK,selectedLinkData.toJsonObject());
            postContentType = CONTENTTYPE_LINK;
        }
        aoThread.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    HashMap<String,String> parameters = new HashMap<String, String>();
                    parameters.put("threadId",aoThread.getObjectId());
                    ParseCloud.callFunctionInBackground("Thread.UpdateHotRanking",parameters);
                    postedBy.increment(ParseUserColumns.POST_COUNT,1);
                    postedBy.saveInBackground();
                }
                mOnPerformNewThread.onPerformNewThread(aoThread,e);
            }
        });
    }

    public void performPostPost(String content, Boolean isEditing, ImageData imageGallery, ImageData imageDataWeb, String youtubeID) {

        post = Post.create(Post.class);
        post.put(TimelinePost.CONTENT,content);
        if(!isEditing) {
            post.put(TimelinePost.POSTED_BY,postedBy);
            post.put(TimelinePost.EDITED,false);
        } else {
            post.put(TimelinePost.EDITED,true);
        }
        try {
            if (imageGallery != null || imageDataWeb != null) {
                if (imageDataWeb != null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("width", imageDataWeb.getWidth());
                    jsonObject.put("height", imageDataWeb.getHeight());
                    jsonObject.put("url",imageDataWeb.getUrl());
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    post.put(TimelinePost.IMAGES, jsonArray);
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("property", "image");
                    jsonObject.put("width", imageGallery.getWidth());
                    jsonObject.put("height", imageGallery.getHeight());
                    jsonObject.put("url",imageGallery.getUrl());
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.put(jsonObject);
                    post.put(TimelinePost.IMAGES, jsonArray);
                    final ParseFile file = new ParseFile(imageGallery.getImageName(), imageGallery.getImageFile());
                    file.saveInBackground();
                    post.put(TimelinePost.IMAGE,file);
                }

                postContentType = CONTENTTYPE_IMAGE;
            } else {
                post.put(TimelinePost.IMAGES, new JSONArray());
            }
        } catch (JSONException jEx) {

        }

        if(youtubeID != null) {
            post.put(TimelinePost.YOUTUBE_ID,youtubeID);
            postContentType = CONTENTTYPE_VIDEO;
        }

        //Hacerlo dsps d guardar post


        if(parentPost != null) {
            post.put(Post.REPLYLEVEL,1);
            post.put(Post.THREAD,parentPost.getParseObject(Post.THREAD));
            post.put(Post.PARENTPOST,parentPost);
        } else {
            post.put(Post.REPLYLEVEL,0);
            post.put(Post.THREAD,parentThread);
        }

        post.getParseObject(Post.THREAD).increment(Post.REPLYCOUNT,1);
        post.getParseObject(Post.THREAD).put(AoThread.LASTPOSTEDBY,postedBy);

        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                HashMap<String,String> parameters = new HashMap<String, String>();
                parameters.put("threadId",post.getParseObject(Post.THREAD).getObjectId());
                ParseCloud.callFunctionInBackground("Thread.UpdateHotRanking",parameters);

                if(parentPost != null && parentPost instanceof  Post) {
                    parentPost.increment(Post.REPLYCOUNT,1);
                    parentPost.put(Post.LASTREPLY,post);
                    parentPost.saveInBackground();
                    HashMap<String,String> parametersNoti = new HashMap<String, String>();
                    parameters.put("toUserId",parentPost.getParseUser(Post.POSTEDBY).getObjectId());
                    parameters.put("postId",parentPost.getObjectId());
                    parameters.put("threadName",post.getParseObject(Post.THREAD).getString("title"));
                    ParseCloud.callFunctionInBackground("sendNewPostReplyPushNotification",parametersNoti);
                } else {
                    HashMap<String,String> parametersNoti = new HashMap<String, String>();
                    parameters.put("postId",post.getObjectId());
                    parameters.put("threadName",post.getParseObject(Post.THREAD).getString("title"));
                    if(post.getParseObject(Post.THREAD).has(AoThread.STARTEDBY)) {
                        parameters.put("toUserId",post.getParseObject(Post.THREAD).getParseUser(AoThread.STARTEDBY).getObjectId());
                    }
                    ParseCloud.callFunctionInBackground("sendNewPostPushNotification",parametersNoti);
                }
                if(parentPost == null)
                    mOnPerformPost.onPerformPost(post,parentThread, e);
                else
                    mOnPerformPost.onPerformPost(post,parentPost, e);

            }
        });




    }

}
