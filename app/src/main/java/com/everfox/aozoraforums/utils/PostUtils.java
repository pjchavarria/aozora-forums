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
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.text.style.URLSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.controls.CustomTypefaceSpan;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.base.AbstractAnimatedDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedDrawable;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.parse.GetDataCallback;
import com.parse.ParseFile;

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

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class PostUtils {

    public static int NUMBER_OF_LOOPS = 1;
    public static double MAX_DIFFERENCE_WIDTH_HEIGHT = 1.2;
    public static String URL_YOUTUBE_THUMBNAILS ="https://i.ytimg.com/vi/YOUTUBE_ID/hqdefault.jpg";



    public static void loadYoutubeImageIntoImageView(Context context, TimelinePost post, ImageView imageView, ImageView ivPlayVideo) {
        String youtubeID = post.getString(TimelinePost.YOUTUBE_ID);
        String urlImage = URL_YOUTUBE_THUMBNAILS.replace("YOUTUBE_ID",youtubeID);
        Glide.with(context).load(urlImage).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
        imageView.requestLayout();
        ivPlayVideo.setVisibility(View.VISIBLE);
    }

    public static void loadTimelinePostImageURLToImageView(final Context context, TimelinePost post, final SimpleDraweeView simpleDraweeView, final ImageView imageView,final ImageView ivPlayGif) {
        try {

            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            final String urlImage = jsonImageInfo.getString("url");
            if(urlImage.toUpperCase().endsWith("GIF")) {
                isGif = true;
            }

            if(!isGif) {
                prepareImageView(jsonImageInfo,imageView,null );
                Glide.with(context).load(urlImage).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
            }
            else {

                if(urlImage.contains("https")) urlImage.replace("https","http");
                prepareImageView(jsonImageInfo,simpleDraweeView,simpleDraweeView);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(urlImage)
                        .setAutoPlayAnimations(false)
                        .setControllerListener(new BaseControllerListener<ImageInfo>(){
                            @Override
                            public void onFinalImageSet(String id, ImageInfo imageInfo, final Animatable animatable) {
                                super.onFinalImageSet(id, imageInfo, animatable);
                                ivPlayGif.setVisibility(View.VISIBLE);

                                final Handler handler = new Handler();
                                final Runnable runnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        animatable.stop();
                                        ivPlayGif.setVisibility(View.VISIBLE);
                                    }
                                } ;

                                simpleDraweeView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if(animatable.isRunning()) {
                                            animatable.stop();
                                            handler.removeCallbacks(runnable);
                                            ivPlayGif.setVisibility(View.VISIBLE);
                                        }else {
                                            try {
                                                Field field = AbstractAnimatedDrawable.class.getDeclaredField("mDurationMs");
                                                field.setAccessible(true);
                                                int duration = field.getInt(animatable);
                                                handler.postDelayed(runnable,duration*NUMBER_OF_LOOPS);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            animatable.start();
                                            ivPlayGif.setVisibility(View.GONE);

                                        }
                                    }
                                });
                            }

                        })
                        .build();

                simpleDraweeView.setController(controller);
                imageView.setVisibility(View.GONE);
                simpleDraweeView.setVisibility(View.VISIBLE);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void loadTimelinePostImageFileToImageView(final Context context, TimelinePost post, final ImageView imageView) {
        try {
            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            ParseFile imageFile = post.getParseFile(TimelinePost.IMAGE);
            if(imageFile.getName().toUpperCase().endsWith("GIF")) {
                isGif = true;
            }
            final Boolean finalIsGif = isGif;
            prepareImageView(jsonImageInfo,imageView,null);
            imageFile.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        if(!finalIsGif)
                            Glide.with(context).load(data).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
                        else
                            Glide.with(context).load(data).asGif().crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static void loadLinkIntoLinkLayout(Context context, TimelinePost post, LinearLayout llLinkLayout) {
        try {
            JSONObject jsonLink =  post.getJSONObject(TimelinePost.LINK);
            ((TextView) llLinkLayout.findViewById(R.id.tvLinkTitle)).setText(jsonLink.getString("title"));
            ((TextView) llLinkLayout.findViewById(R.id.tvLinkDesc)).setText(jsonLink.getString("description"));
            URL url = new URL(jsonLink.getString("url"));
            ((TextView) llLinkLayout.findViewById(R.id.tvLinkURL)).setText( url.getHost());
            Glide.with(context).load( jsonLink.getJSONArray("images").get(0))
                    .crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(((ImageView) llLinkLayout.findViewById(R.id.ivLinkImage)));

            llLinkLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void prepareImageView(JSONObject jsonImageInfo,ImageView iv, SimpleDraweeView simpleDraweeView) throws JSONException {

        final int jsonHeight = jsonImageInfo.getInt("height");
        final int jsonWidth = jsonImageInfo.getInt("width");
        if ((double) jsonHeight / (double) jsonWidth > MAX_DIFFERENCE_WIDTH_HEIGHT) {
            int maxAllowedHeight = (int) (jsonWidth * MAX_DIFFERENCE_WIDTH_HEIGHT);
            ViewGroup.LayoutParams params = iv.getLayoutParams();
            params.height = maxAllowedHeight;
            iv.setLayoutParams(params);
        } else {
            if (simpleDraweeView != null) {
                simpleDraweeView.setAspectRatio((float) jsonWidth / (float) jsonHeight);
            }
        }
    }

    public static String getWhenWasPosted(TimelinePost timelinePost) {

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

    public static void setPostedByFromPost(Context context, TimelinePost post, TextView tvPostedBy) {

        Typeface awesomeTypeface = Typeface.createFromAsset(context.getAssets(),"fonts/FontAwesome.ttf");
        tvPostedBy.setText(post.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME) +" ");
        Spannable rightArrow = new SpannableString(context.getString(R.string.fa_right_arow));
        rightArrow.setSpan(new CustomTypefaceSpan("",awesomeTypeface),0,rightArrow.length(),0);
        tvPostedBy.append(rightArrow);
        tvPostedBy.append( " " + post.getParseObject(TimelinePost.USER_TIMELINE).getString(ParseUserColumns.AOZORA_USERNAME));

    }

}