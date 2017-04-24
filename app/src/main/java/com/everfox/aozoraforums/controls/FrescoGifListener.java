package com.everfox.aozoraforums.controls;

import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.base.AbstractAnimatedDrawable;
import com.facebook.imagepipeline.image.ImageInfo;

import java.lang.reflect.Field;

/**
 * Created by daniel.soto on 1/19/2017.
 */

public class FrescoGifListener extends BaseControllerListener {

    public static int NUMBER_OF_LOOPS = 1;
    private ImageView ivPlayGif;
    private SimpleDraweeView simpleDraweeView;

    public FrescoGifListener (ImageView ivPlayGif, SimpleDraweeView simpleDraweeView) {
        this.ivPlayGif = ivPlayGif;
        this.simpleDraweeView = simpleDraweeView;
    }

    @Override
    public void onIntermediateImageSet(String id, Object imageInfo) {
        super.onIntermediateImageSet(id, imageInfo);
    }

    @Override
    public void onFinalImageSet(String id, Object imageInfo, final Animatable animatable) {
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
                if (animatable == null) { return; }
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
}
