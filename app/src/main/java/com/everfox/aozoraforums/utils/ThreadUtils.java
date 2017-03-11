package com.everfox.aozoraforums.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.AoThreadAdapter;
import com.everfox.aozoraforums.adapters.CommentPostAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controls.FrescoGifListener;
import com.everfox.aozoraforums.models.Anime;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.models.TimelinePost;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by daniel.soto on 2/1/2017.
 */

public class ThreadUtils {

    public static double MAX_DIFFERENCE_WIDTH_HEIGHT = 1.2;
    public static double MAX_DIFFERENCE_WIDTH_HEIGHT_SIZE = 1.3;

    public static double MAX_DIFFERENCE_AOTALK_WIDTH_HEIGHT = 0.33;
    public static double MAX_DIFFERENCE_AOTALK_WIDTH_HEIGHT_SIZE = 0.33;


    public static void setThreadTagWhenPostedViewsBy(final AoThread aoThread, TextView textView,
                                                     final AoThreadAdapter.OnUsernameTappedListener mCallback) {
        List<ParseObject> lst = aoThread.getList(AoThread.TAGS);
        String threadTag = "";
        for(int i=0;i<lst.size();i++) {
            if(lst.get(i) instanceof AoThreadTag) {
                threadTag = lst.get(i).getString(AoThreadTag.NAME);
                break;
            }
            if(lst.get(i) instanceof Anime) {
                threadTag = lst.get(i).getString(Anime.TITLE);
                break;
            }
        }
        String whenWasPosted = PostUtils.getWhenWasPosted(aoThread);
        String views = " - " + AoUtils.numberToStringOrZero(aoThread.getNumber(AoThread.VIEWS)) + " views - ";
        String by = "by ";
        String usernamePosted = aoThread.getParseUser(AoThread.POSTEDBY).getString(ParseUserColumns.AOZORA_USERNAME);
        textView.setText("#" + threadTag + " - ");
        textView.append(whenWasPosted);
        textView.append(views);
        textView.append(by);
        Spannable username = new SpannableString(usernamePosted);
        username.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                mCallback.onUsernameTapped(aoThread.getParseUser(AoThread.POSTEDBY));
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        },0,username.length(),0);
        textView.append(username);
        textView.setMovementMethod(LinkMovementMethod.getInstance());

    }


    public static String getThreadTagWhenPosted(AoThread aoThread) {
        List<ParseObject> lst = aoThread.getList(AoThread.TAGS);
        String threadTag = "";
        for(int i=0;i<lst.size();i++) {
            if(lst.get(i) instanceof AoThreadTag) {
                threadTag = lst.get(i).getString(AoThreadTag.NAME);
                break;
            }
            if(lst.get(i) instanceof Anime) {
                threadTag = lst.get(i).getString(Anime.TITLE);
                break;
            }
        }
        String whenWasPosted = PostUtils.getWhenWasPosted(aoThread);
        return "#"+threadTag + " - " + whenWasPosted;
    }


    public static void loadNewsImageURLToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView, final ImageView imageView, final ImageView ivPlayGif) {
        try {

            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            final String urlImage = jsonImageInfo.getString("url");
            if(urlImage.toUpperCase().endsWith("GIF")) {
                isGif = true;
            }

            if(!isGif) {

                Glide.with(context).load(urlImage).crossFade().centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                imageView.requestLayout();

            }
            else {
                if(urlImage.contains("https")) urlImage.replace("https","http");
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


    public static void loadNewsImageFileToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView, final ImageView imageView, final ImageView ivPlayGif) {
        try {

            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            ParseFile imageFile = post.getParseFile(TimelinePost.IMAGE);
            if(imageFile.getName().toUpperCase().endsWith("GIF")) {
                isGif = true;
            }
            final Boolean finalIsGif = isGif;

            imageFile.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        if(!finalIsGif) {

                            Glide.with(context).load(data).crossFade().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
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


    public static void loadThreadImageURLToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView,
                                                     final ImageView imageView, final ImageView ivPlayGif, Boolean fullscreen, Boolean aoTalk) {
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

                if(!isComment && !fullscreen) {
                    if(aoTalk)
                        prepareImageViewAoTalk(jsonImageInfo, imageView, null);
                    else
                        prepareImageView(jsonImageInfo, imageView, null);
                }


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

    public static void loadThreadImageFileToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView,
                                                      final ImageView imageView, final ImageView ivPlayGif, final Boolean fullscreen, Boolean aoTalk) {
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
                if (!isGif) {
                    if(aoTalk)
                        prepareImageViewAoTalk(jsonImageInfo, imageView, null);
                    else
                        prepareImageView(jsonImageInfo, imageView, null);
                }
                else {
                    if(aoTalk)
                        prepareImageViewAoTalk(jsonImageInfo, imageView, simpleDraweeView);
                    else
                        prepareImageView(jsonImageInfo, imageView, simpleDraweeView);
                }
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

                            try {
                                if(!fullscreen)
                                    prepareImageView(jsonImageInfo,simpleDraweeView,simpleDraweeView);
                                else
                                    prepareSimpleDraweeViewFullScreen(jsonImageInfo,simpleDraweeView);
                            } catch (JSONException jex) {
                            }

                            FrescoGifListener frescoGifListener = new FrescoGifListener(ivPlayGif, simpleDraweeView);
                            DraweeController controller = Fresco.newDraweeControllerBuilder()
                                    .setUri("data:mime/type;base64," + Base64.encodeToString(data,0))
                                    .setAutoPlayAnimations(false)
                                    .setControllerListener(frescoGifListener)
                                    .build();

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
    private static void prepareImageViewAoTalk(JSONObject jsonImageInfo,ImageView iv, SimpleDraweeView simpleDraweeView) throws JSONException {

        final int jsonHeight = jsonImageInfo.getInt("height");
        final int jsonWidth = jsonImageInfo.getInt("width");
        if ((double) jsonHeight / (double) jsonWidth > MAX_DIFFERENCE_AOTALK_WIDTH_HEIGHT) {
            int maxAllowedHeight = (int) (jsonWidth * MAX_DIFFERENCE_AOTALK_WIDTH_HEIGHT_SIZE);
            ViewGroup.LayoutParams params = iv.getLayoutParams();
            if(maxAllowedHeight > 1500)
                maxAllowedHeight = maxAllowedHeight/2;
            params.height = maxAllowedHeight;
            iv.setLayoutParams(params);
            iv.requestLayout();
        } else {
            if (simpleDraweeView != null) {
                simpleDraweeView.setAspectRatio((float) jsonWidth / (float) jsonHeight);
            }
        }
    }

    private static void prepareImageView(JSONObject jsonImageInfo,ImageView iv, SimpleDraweeView simpleDraweeView) throws JSONException {

        final int jsonHeight = jsonImageInfo.getInt("height");
        final int jsonWidth = jsonImageInfo.getInt("width");
        if ((double) jsonHeight / (double) jsonWidth > MAX_DIFFERENCE_WIDTH_HEIGHT) {
            int maxAllowedHeight = (int) (jsonWidth * MAX_DIFFERENCE_WIDTH_HEIGHT_SIZE);
            ViewGroup.LayoutParams params = iv.getLayoutParams();
            if(maxAllowedHeight > 1500)
                maxAllowedHeight = maxAllowedHeight/2;
            params.height = maxAllowedHeight;
            iv.setLayoutParams(params);
            iv.requestLayout();
        } else {
            if (simpleDraweeView != null) {
                simpleDraweeView.setAspectRatio((float) jsonWidth / (float) jsonHeight);
            }
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
    public static void setCommentPostUsernameAndText(Context context, final Post comment, TextView tvComment,
                                                     final CommentPostAdapter.OnUsernameTappedListener mCommentCallback) {

        final ParseUser userComment = (ParseUser)comment.getParseObject(TimelinePost.POSTED_BY);
        Spannable username = new SpannableString(userComment.getString(ParseUserColumns.AOZORA_USERNAME));
        username.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                mCommentCallback.onUsernameTapped(userComment);
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

    public static void setCommentThreadUsernameAndText(final Context context, final Post comment, final Post parentComment, TextView tvComment,
                                                       final AoThreadAdapter.OnUsernameTappedListener mPostCallback,
                                                       final AoThreadAdapter.OnCommentTappedListener mThreadCallback
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
        content.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                mThreadCallback.onCommentTapped(parentComment);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setColor(ContextCompat.getColor(context,R.color.gray3C));

            }
        },0,content.length(),0);
        tvComment.append(content);
        tvComment.setMovementMethod(LinkMovementMethod.getInstance());
    }



}
