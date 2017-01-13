package com.everfox.aozoraforums.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.everfox.aozoraforums.models.TimelinePost;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.parse.GetDataCallback;
import com.parse.ParseFile;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class PostUtils {

    public static double MAX_DIFFERENCE_WIDTH_HEIGHT = 1.2;
    public static String URL_YOUTUBE_THUMBNAILS ="https://i.ytimg.com/vi/YOUTUBE_ID/hqdefault.jpg";



    public static void loadYoutubeImageIntoImageView(Context context, TimelinePost post, ImageView imageView, ImageView ivPlayYoutube) {
        String youtubeID = post.getString(TimelinePost.YOUTUBE_ID);
        String urlImage = URL_YOUTUBE_THUMBNAILS.replace("YOUTUBE_ID",youtubeID);
        Glide.with(context).load(urlImage).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
        imageView.requestLayout();
        ivPlayYoutube.setVisibility(View.VISIBLE);
    }

    public static void loadTimelinePostImageURLToImageView(Context context, TimelinePost post, com.facebook.drawee.view.SimpleDraweeView simpleDraweeView, ImageView imageView) {
        try {

            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            String urlImage = jsonImageInfo.getString("url");
            if(urlImage.toUpperCase().endsWith("GIF")) {
                isGif = true;
            }
            if(!isGif) {
                prepareImageView(jsonImageInfo,imageView);
                Glide.with(context).load(urlImage).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
            }
            else {

                prepareImageView(jsonImageInfo,simpleDraweeView);
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(urlImage)
                        .setAutoPlayAnimations(true)
                        .build();
                simpleDraweeView.setController(controller);
                imageView.setVisibility(View.GONE);
                simpleDraweeView.setVisibility(View.VISIBLE);
            }

/*
            if(!isGif)
                Glide.with(context).load(urlImage).crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
            else
                Glide.with(context).load(urlImage).asGif().crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
*/

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
            prepareImageView(jsonImageInfo,imageView);
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

    private static void prepareImageView(JSONObject jsonImageInfo,ImageView iv) throws JSONException {

        final int jsonHeight =  jsonImageInfo.getInt("height");
        final int jsonWidth =  jsonImageInfo.getInt("width");
        if( (double)jsonHeight /  (double)jsonWidth > MAX_DIFFERENCE_WIDTH_HEIGHT) {
            int maxAllowedHeight = (int) (jsonWidth * MAX_DIFFERENCE_WIDTH_HEIGHT);
            ViewGroup.LayoutParams params = iv.getLayoutParams();
            params.height = maxAllowedHeight;
            iv.setLayoutParams(params);
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
}
