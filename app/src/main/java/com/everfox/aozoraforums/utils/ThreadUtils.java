package com.everfox.aozoraforums.utils;

import android.content.Context;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.everfox.aozoraforums.controls.FrescoGifListener;
import com.everfox.aozoraforums.models.TimelinePost;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by daniel.soto on 2/1/2017.
 */

public class ThreadUtils {

    public static double MAX_DIFFERENCE_WIDTH_HEIGHT = 1.2;
    public static double MAX_DIFFERENCE_WIDTH_HEIGHT_SIZE = 1.3;

    public static void loadThreadImageURLToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView, final ImageView imageView, final ImageView ivPlayGif, Boolean fullscreen) {
        try {

            final Boolean isComment = (post instanceof TimelinePost
                    && post.getParseObject(TimelinePost.PARENT_POST) != null);
            Boolean isGif = false;
            final JSONObject jsonImageInfo = post.getJSONArray(TimelinePost.IMAGES).getJSONObject(0);
            final String urlImage = jsonImageInfo.getString("url");
            if(urlImage.toUpperCase().endsWith("GIF")) {
                isGif = true;
            }

            if(!isGif) {

                if(!isComment && !fullscreen)
                    prepareImageView(jsonImageInfo,imageView,null );

                int jsonHeight = 0;
                int jsonWidth = 0;
                try {
                    jsonHeight = jsonImageInfo.getInt("height");
                    jsonWidth = jsonImageInfo.getInt("width");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                if( (double) jsonHeight / (double) jsonWidth > MAX_DIFFERENCE_WIDTH_HEIGHT && !isComment && !fullscreen)
                    Glide.with(context).load(urlImage).crossFade().centerCrop().diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
                else
                    Glide.with(context).load(urlImage).crossFade().fitCenter()
                            .diskCacheStrategy(DiskCacheStrategy.RESULT).into(imageView);
                imageView.setVisibility(View.VISIBLE);
                imageView.requestLayout();
            }
            else {

                if(urlImage.contains("https")) urlImage.replace("https","http");

                if(!fullscreen)
                    prepareImageView(jsonImageInfo,simpleDraweeView,simpleDraweeView);
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

    public static void loadThreadImageFileToImageView(final Context context, ParseObject post, final SimpleDraweeView simpleDraweeView, final ImageView imageView, final ImageView ivPlayGif, final Boolean fullscreen) {
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
                        if(!finalIsGif) {

                            int jsonHeight = 0;
                            int jsonWidth = 0;
                            try {
                                jsonHeight = jsonImageInfo.getInt("height");
                                jsonWidth = jsonImageInfo.getInt("width");
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
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

                            simpleDraweeView.setController(controller);
                            imageView.setVisibility(View.GONE);
                            simpleDraweeView.setVisibility(View.VISIBLE);

                            //Glide.with(context).load(data).asGif().crossFade().fitCenter().diskCacheStrategy(DiskCacheStrategy.NONE).into(imageView);
                        }
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
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

}
