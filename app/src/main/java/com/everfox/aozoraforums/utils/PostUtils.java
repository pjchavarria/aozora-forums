package com.everfox.aozoraforums.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.AoThreadAdapter;
import com.everfox.aozoraforums.adapters.ProfileTimelineAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controls.CustomTypefaceSpan;
import com.everfox.aozoraforums.controls.FrescoGifListener;
import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.models.TimelinePost;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.base.AbstractAnimatedDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class PostUtils {

    public static double MAX_DIFFERENCE_WIDTH_HEIGHT = 1.2;
    public static double MAX_DIFFERENCE_WIDTH_HEIGHT_SIZE = 1.3;
    public static String URL_YOUTUBE_THUMBNAILS ="https://i.ytimg.com/vi/YOUTUBE_ID/hqdefault.jpg";



    public static void loadYoutubeImageIntoImageView(Context context, ParseObject post, ImageView imageView, ImageView ivPlayVideo) {
        String youtubeID = post.getString(TimelinePost.YOUTUBE_ID);
        String urlImage = URL_YOUTUBE_THUMBNAILS.replace("YOUTUBE_ID",youtubeID);
        Glide.with(context).load(urlImage).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
        imageView.requestLayout();
        ivPlayVideo.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
    }

    public static void loadTimelinePostImageURLToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView, final ImageView imageView,final ImageView ivPlayGif, Boolean fullscreen) {
        try {

            final Boolean isComment = (post instanceof TimelinePost
                    && post.getParseObject(TimelinePost.PARENT_POST) != null);
            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            final String urlImage = jsonImageInfo.getString("url");
            if(urlImage.toUpperCase().endsWith("GIF")) {
                isGif = true;
            }
            int jsonHeight = 0;
            int jsonWidth = 0;
            try {
                jsonHeight = jsonImageInfo.getInt("height");
                jsonWidth = jsonImageInfo.getInt("width");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            if(!isGif) {

                if(!isComment && !fullscreen)
                    prepareImageView(jsonImageInfo,imageView,null );

                if( (double) jsonHeight / (double) jsonWidth > MAX_DIFFERENCE_WIDTH_HEIGHT && !isComment && !fullscreen)
                    Glide.with(context).load(urlImage).crossFade().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
                else
                    Glide.with(context).load(urlImage).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);

                imageView.setVisibility(View.VISIBLE);
                imageView.requestLayout();
            }
            else {

                if(urlImage.contains("https")) urlImage.replace("https","http");

                if(!fullscreen)
                    prepareImageView(jsonImageInfo,simpleDraweeView,simpleDraweeView);
                else
                    prepareSimpleDraweeViewFullScreen(jsonImageInfo,simpleDraweeView);
                FrescoGifListener frescoGifListener = new FrescoGifListener(ivPlayGif, simpleDraweeView);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(urlImage)
                        .setAutoPlayAnimations(false)
                        .setControllerListener(frescoGifListener)
                        .build();

                simpleDraweeView.setController(controller);
                imageView.setVisibility(View.GONE);
                simpleDraweeView.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void loadTimelinePostImageFileToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView, final ImageView imageView, final ImageView ivPlayGif, final Boolean fullscreen) {
        try {

            final Boolean isComment = (post instanceof TimelinePost
                    && post.getParseObject(TimelinePost.PARENT_POST) != null);

            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            ParseFile imageFile = post.getParseFile(TimelinePost.IMAGE);
            if(imageFile.getName().toUpperCase().endsWith("GIF")) {
                isGif = true;
            }
            final Boolean finalIsGif = isGif;

            if(!isComment && !fullscreen) {
                if (!isGif)
                    prepareImageView(jsonImageInfo, imageView, null);
                else
                    prepareImageView(jsonImageInfo, imageView, simpleDraweeView);
            }

            imageFile.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {

                        int jsonHeight = 0;
                        int jsonWidth = 0;
                        try {
                            jsonHeight = jsonImageInfo.getInt("height");
                            jsonWidth = jsonImageInfo.getInt("width");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        if(!finalIsGif) {

                            if( (double) jsonHeight / (double) jsonWidth > MAX_DIFFERENCE_WIDTH_HEIGHT && !fullscreen && !isComment)
                                Glide.with(context).load(data).crossFade().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
                            else
                                Glide.with(context).load(data).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);

                            imageView.setVisibility(View.VISIBLE);
                            imageView.requestLayout();
                        }
                        else {
                            FrescoGifListener frescoGifListener = new FrescoGifListener(ivPlayGif, simpleDraweeView);
                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                    .setUri("data:mime/type;base64," + Base64.encodeToString(data,0))
                                    .setAutoPlayAnimations(false)
                                    .setControllerListener(frescoGifListener)
                                    .build();
                            try {
                                if (!fullscreen)
                                    prepareImageView(jsonImageInfo, simpleDraweeView, simpleDraweeView);
                                else
                                    prepareSimpleDraweeViewFullScreen(jsonImageInfo, simpleDraweeView);
                            } catch (JSONException jex) {

                            }
                            simpleDraweeView.setController(controller);
                            imageView.setVisibility(View.GONE);
                            simpleDraweeView.setVisibility(View.VISIBLE);

                        }
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void prepareSimpleDraweeViewFullScreen(JSONObject jsonImageInfo, SimpleDraweeView simpleDraweeView) throws JSONException {

        final int jsonHeight = jsonImageInfo.getInt("height");
        final int jsonWidth = jsonImageInfo.getInt("width");
        if (jsonWidth > AozoraForumsApp.getScreenWidth()) {
            simpleDraweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER);
        } else {
            simpleDraweeView.getHierarchy().setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
        }
        simpleDraweeView.setAspectRatio((float) jsonWidth / (float) jsonHeight);
    }

    private static void prepareImageView(JSONObject jsonImageInfo,ImageView iv, SimpleDraweeView simpleDraweeView) throws JSONException {

        final int jsonHeight = jsonImageInfo.getInt("height");
        final int jsonWidth = jsonImageInfo.getInt("width");
        if ((double) jsonHeight / (double) jsonWidth > MAX_DIFFERENCE_WIDTH_HEIGHT) {
            int maxAllowedHeight = (int) (jsonWidth * MAX_DIFFERENCE_WIDTH_HEIGHT_SIZE);
            if(maxAllowedHeight > 1500)
                maxAllowedHeight = maxAllowedHeight/2;
            ViewGroup.LayoutParams params = iv.getLayoutParams();
            params.height = maxAllowedHeight;
            iv.setLayoutParams(params);
            iv.requestLayout();

        } else {
            if (simpleDraweeView != null) {
                simpleDraweeView.setAspectRatio((float) jsonWidth / (float) jsonHeight);
            }
        }
    }

    public static void loadLinkIntoLinkLayout(Context context, ParseObject post, LinearLayout llLinkLayout) {
        try {
            JSONObject jsonLink =  post.getJSONObject(TimelinePost.LINK);
            ((TextView) llLinkLayout.findViewById(R.id.tvLinkTitle)).setText(jsonLink.getString("title"));
            ((TextView) llLinkLayout.findViewById(R.id.tvLinkDesc)).setText(jsonLink.getString("description"));
            String urlString = jsonLink.getString("url");
            if(!urlString.equals("")) {
                URL url = new URL(urlString);
                ((TextView) llLinkLayout.findViewById(R.id.tvLinkURL)).setText(url.getHost());
            } else {
                ((TextView) llLinkLayout.findViewById(R.id.tvLinkURL)).setText("");
            }

            Glide.with(context).load( jsonLink.getJSONArray("images").get(0))
                    .crossFade().fitCenter()
                        .into(((ImageView) llLinkLayout.findViewById(R.id.ivLinkImage)));

            llLinkLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getWhenNotificationWasUpdate(AoNotification notification) {

        Date lastUpdated = notification.getDate(AoNotification.LAST_UPDATED_AT);
        if(lastUpdated == null) {
            lastUpdated = notification.getUpdatedAt();
        }
        Date currentDate = Calendar.getInstance().getTime();
        int secondsDiff = (int) DateUtils.getSecondsDiff(lastUpdated,currentDate);
        int weeksAgo =  (secondsDiff/(7*24*60*60));
        if ( weeksAgo > 0) {
            return String.valueOf(weeksAgo) + " " + (weeksAgo == 1? "week" : "weeks");
        }
        int daysAgo =  (secondsDiff/(24*60*60));
        if ( daysAgo > 0) {
            return String.valueOf(daysAgo) + " " + (daysAgo == 1? "day" : "days");
        }
        int hoursAgo =  (secondsDiff/(60*60));
        if ( hoursAgo > 0) {
            return String.valueOf(hoursAgo) + " " + (hoursAgo == 1? "hr" : "hrs");
        }
        int minutesAgo =  (secondsDiff/(60));
        if ( minutesAgo > 0) {
            return String.valueOf(minutesAgo) + " " + (minutesAgo == 1? "min" : "mins");
        }

        return "Just now";
    }

    public static String getWhenWasPosted(ParseObject timelinePost) {

        Date dateCreated = timelinePost.getCreatedAt();
        Date currentDate = Calendar.getInstance().getTime();
        int secondsDiff = (int) DateUtils.getSecondsDiff(dateCreated,currentDate);
        int weeksAgo =  (secondsDiff/(7*24*60*60));
        if ( weeksAgo > 0) {
            return String.valueOf(weeksAgo) + " " + (weeksAgo == 1? "week" : "weeks");
        }
        int daysAgo =  (secondsDiff/(24*60*60));
        if ( daysAgo > 0) {
            return String.valueOf(daysAgo) + " " + (daysAgo == 1? "day" : "days");
        }
        int hoursAgo =  (secondsDiff/(60*60));
        if ( hoursAgo > 0) {
            return String.valueOf(hoursAgo) + " " + (hoursAgo == 1? "hr" : "hrs");
        }
        int minutesAgo =  (secondsDiff/(60));
        if ( minutesAgo > 0) {
            return String.valueOf(minutesAgo) + " " + (minutesAgo == 1? "min" : "mins");
        }

        return "Just now";
    }

    public static void setCommentUsernameAndText(Context context, final ParseObject comment, TextView tvComment,
                                                 final TimelinePostsAdapter.OnUsernameTappedListener mPostCallback
                                                 ) {

        final ParseUser userComment = (ParseUser)comment.getParseObject(TimelinePost.POSTED_BY);
        Spannable username = new SpannableString(userComment.getString(ParseUserColumns.AOZORA_USERNAME));
        username.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                mPostCallback.onUsernameTapped(userComment);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);

            }
        },0,username.length(),0);
        tvComment.setText(username);
        Spannable content = new SpannableString(" " + comment.getString(TimelinePost.CONTENT));
        content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.gray3C)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvComment.append(content);
        tvComment.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public static void setPostedByFromPost(Context context, final TimelinePost post, TextView tvPostedBy,
                                           final ProfileTimelineAdapter.OnUsernameTappedListener mProfileCallback,
                                           final TimelinePostsAdapter.OnUsernameTappedListener mPostCallback) {

        Typeface awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        Spannable postedBy = new SpannableString(post.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME) +" ");
        Spannable rightArrow = new SpannableString(context.getString(R.string.fa_right_arow) + " ");
        rightArrow.setSpan(new CustomTypefaceSpan("",awesomeTypeface),0,rightArrow.length(),0);
        Spannable userTimeline = new SpannableString(post.getParseObject(TimelinePost.USER_TIMELINE).getString(ParseUserColumns.AOZORA_USERNAME));
        if(mProfileCallback != null) {
            setProfileCallbackToSpan(postedBy,post.getParseUser(TimelinePost.POSTED_BY),mProfileCallback);
            setProfileCallbackToSpan(userTimeline,post.getParseUser(TimelinePost.USER_TIMELINE),mProfileCallback);
        } else {
            setPostCallbackToSpan(postedBy,post.getParseUser(TimelinePost.POSTED_BY),mPostCallback);
            setPostCallbackToSpan(userTimeline,post.getParseUser(TimelinePost.USER_TIMELINE),mPostCallback);
        }
        tvPostedBy.setText(postedBy);
        tvPostedBy.append(rightArrow);
        tvPostedBy.append(userTimeline);
        tvPostedBy.setMovementMethod(LinkMovementMethod.getInstance());

    }

    private static void setProfileCallbackToSpan(Spannable text, final ParseUser user, final ProfileTimelineAdapter.OnUsernameTappedListener mCallback ) {
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                mCallback.onUsernameTapped(user);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        },0,text.length(),0);
    }

    private static void setPostCallbackToSpan(Spannable text, final ParseUser user, final TimelinePostsAdapter.OnUsernameTappedListener mCallback ) {
        text.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                mCallback.onUsernameTapped(user);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        },0,text.length(),0);
    }

    public static void loadAvatarPic(ParseFile profilePic, final ImageView ivAvatar) {
        if(profilePic != null) {
            profilePic.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(data, 0, data.length);
                        ivAvatar.setImageBitmap(bmp);
                    }
                }
            });
        } else {
            ivAvatar.setImageResource(R.drawable.default_avatar);
        }
    }

    public static void loadBannerPic(ParseFile profilePic, final ImageView ivAvatar) {
        if(profilePic != null) {
            profilePic.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(data, 0, data.length);
                        ivAvatar.setImageBitmap(bmp);
                    }
                }
            });
        } else {
            ivAvatar.setImageResource(R.drawable.placeholder_banner);
        }
    }

    public interface OnDeletePostCallback {
        public void onDeletePost();
    }

    public interface OnLikePostCallback {
        public void onLikePost();
    }

    public static void deletePost(final ParseObject post, ParseObject parentPost, final OnDeletePostCallback onDeletePostCallback) {

        if(parentPost == null) {
            //Delete comments
            ParseQuery<ParseObject> queryPosts;
            if(post instanceof TimelinePost)
                queryPosts = new ParseQuery<>("TimelinePost");
            else
                queryPosts = new ParseQuery<>("Post");
            queryPosts.whereEqualTo(TimelinePost.PARENT_POST,post);
            queryPosts.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e==null) {
                        deletePosts(objects, post, true, onDeletePostCallback);

                    }
                }
            });

        } else {
            //Just delete Child Post
            ArrayList<ParseObject> lst = new ArrayList<>();
            lst.add(post);
            deletePosts(lst,parentPost,false,onDeletePostCallback);
        }
    }

    private static void deletePosts(final List<ParseObject> posts, final ParseObject parentPost, final Boolean removeParent, final OnDeletePostCallback onDeletePostCallback) {

        if(removeParent)
            posts.add(parentPost);

        ParseObject.deleteAllInBackground(posts, new DeleteCallback() {
            @Override
            public void done(ParseException e) {

                for(int i=0;i<posts.size();i++) {
                    posts.get(i).getParseObject(TimelinePost.POSTED_BY).increment(ParseUserColumns.POST_COUNT,-1);
                }

                if(parentPost instanceof Post) {
                    parentPost.getParseObject(Post.THREAD).increment(TimelinePost.REPLY_COUNT,-posts.size());
                    parentPost.getParseObject(Post.THREAD).saveInBackground();
                }

                if(removeParent) {
                    onDeletePostCallback.onDeletePost();
                } else {
                    ParseObject lastReply = posts.get(posts.size()-1);
                    if(lastReply instanceof TimelinePost) {
                        List<TimelinePost> replies = ((TimelinePost)parentPost).getReplies();
                        int index = replies.indexOf(lastReply);
                        if(index != -1) {
                            replies.remove(index);
                            ((TimelinePost) parentPost).setReplies(replies);
                        }
                        parentPost.increment(TimelinePost.REPLY_COUNT,-posts.size());

                        if(replies.size() > 0) {
                            parentPost.put(TimelinePost.LAST_REPLY,replies.get(replies.size()-1));
                        } else {
                            parentPost.remove(TimelinePost.LAST_REPLY);
                        }

                    } else {
                        List<Post> replies = ((Post)parentPost).getReplies();
                        int index = replies.indexOf(lastReply);
                        if(index != -1) {
                            replies.remove(index);
                            ((Post) parentPost).setReplies(replies);
                        }
                        parentPost.increment(TimelinePost.REPLY_COUNT,-posts.size());

                        if(replies.size() > 0) {
                            parentPost.put(TimelinePost.LAST_REPLY,replies.get(replies.size()-1));
                        } else {
                            parentPost.remove(TimelinePost.LAST_REPLY);
                        }

                    }

                    parentPost.saveInBackground();
                    onDeletePostCallback.onDeletePost();
                }

            }
        });
    }

    static Boolean savingLike = false;
    public static ParseObject likePost(ParseObject post) {
        if(savingLike)
            return null;
        List<ParseUser> lst = post.getList(TimelinePost.LIKED_BY);
        if(lst == null) lst = new ArrayList<>();
        ParseUser user = ParseUser.getCurrentUser();
        ArrayList<ParseUser> userArrayList = new ArrayList<>();
        if(lst.contains(user)){
            userArrayList.add(user);
            post.removeAll(TimelinePost.LIKED_BY,userArrayList);
            post.increment(TimelinePost.LIKE_COUNT,-1);
        } else {
            post.addUnique(TimelinePost.LIKED_BY,user);
            post.increment(TimelinePost.LIKE_COUNT,1);
            //Quitar de unlike
            if(post.has(AoThread.UNLIKED_BY)) {
                List<ParseUser> lstUnlike = post.getList(AoThread.UNLIKED_BY);
                if(lstUnlike == null) lstUnlike = new ArrayList<>();
                userArrayList = new ArrayList<>();
                userArrayList.add(user);
                if(lstUnlike.contains(user)) {
                    post.removeAll(AoThread.UNLIKED_BY,userArrayList);
                    post.increment(AoThread.UNLIKE_COUNT,-1);
                }
            }
        }
        savingLike = true;
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                savingLike = false;
            }
        });
        return post;
    }

    public static ParseObject unlikeThread(ParseObject thread) {

        if(savingLike)
            return null;

        List<ParseUser> lstUnlike = thread.getList(AoThread.UNLIKED_BY);
        if(lstUnlike == null) lstUnlike = new ArrayList<>();
        ArrayList<ParseUser> userArrayList = new ArrayList<>();
        ParseUser user = ParseUser.getCurrentUser();
        userArrayList.add(user);
        if(lstUnlike.contains(user)) {
            userArrayList.add(user);
            thread.removeAll(AoThread.UNLIKED_BY,userArrayList);
            thread.increment(AoThread.UNLIKE_COUNT,-1);
        } else {
            thread.addUnique(AoThread.UNLIKED_BY,user);
            thread.increment(AoThread.UNLIKE_COUNT,1);
            //Quitar de like
            if(thread.has(AoThread.LIKED_BY)) {
                List<ParseUser> lstLike = thread.getList(AoThread.LIKED_BY);
                if(lstLike == null) lstLike = new ArrayList<>();
                userArrayList = new ArrayList<>();
                userArrayList.add(user);
                if(lstLike.contains(user)) {
                    thread.removeAll(AoThread.LIKED_BY,userArrayList);
                    thread.increment(AoThread.LIKE_COUNT,-1);
                }
            }
        }
        savingLike = true;
        thread.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                savingLike = false;
            }
        });
        return thread;
    }

    public static ArrayList<ParseObject> repostPost(ParseObject post) {
        ArrayList<ParseObject> resultRepost = new ArrayList<>();
        ParseObject sourceObject = post.getParseObject(TimelinePost.REPOST_SOURCE);
        if(sourceObject == null)
            sourceObject = post;
        List<ParseUser> lstRepostedBy  = sourceObject.getList(TimelinePost.REPOSTED_BY);
        if(lstRepostedBy == null) lstRepostedBy = new ArrayList<>();
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(lstRepostedBy.contains(currentUser)) {
            lstRepostedBy.add(currentUser);
            sourceObject.removeAll(TimelinePost.REPOSTED_BY,lstRepostedBy);
            sourceObject.increment(TimelinePost.REPOST_COUNT,-1);
            sourceObject.saveInBackground();
            ParseQuery<TimelinePost> query = ParseQuery.getQuery(TimelinePost.class);
            query.whereEqualTo(TimelinePost.REPOST_SOURCE,sourceObject);
            query.whereEqualTo(TimelinePost.USER_TIMELINE,currentUser);
            query.getFirstInBackground(new GetCallback<TimelinePost>() {
                @Override
                public void done(TimelinePost object, ParseException e) {
                    if(e==null && object!=null)
                        object.deleteInBackground();
                }
            });
            resultRepost.add(sourceObject);
            return resultRepost;
        } else {
            sourceObject.addUnique(TimelinePost.REPOSTED_BY,currentUser);
            sourceObject.increment(TimelinePost.REPOST_COUNT,1);
            sourceObject.saveInBackground();
            TimelinePost timelinePost = new TimelinePost();
            timelinePost.put(TimelinePost.REPOST_SOURCE,sourceObject);
            timelinePost.put(TimelinePost.REPLY_LEVEL,0);
            timelinePost.put(TimelinePost.POSTED_BY,currentUser);
            timelinePost.put(TimelinePost.EDITED,false);
            timelinePost.put(TimelinePost.USER_TIMELINE,currentUser);
            try {
                timelinePost.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            resultRepost.add(sourceObject);
            resultRepost.add(timelinePost);
            return resultRepost;
        }
    }
}
