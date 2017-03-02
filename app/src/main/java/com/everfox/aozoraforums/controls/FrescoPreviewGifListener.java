package com.everfox.aozoraforums.controls;

import android.graphics.drawable.Animatable;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.base.AbstractAnimatedDrawable;

import java.lang.reflect.Field;

/**
 * Created by Daniel on 02/03/2017.
 */

public class FrescoPreviewGifListener extends BaseControllerListener {

    public static int NUMBER_OF_LOOPS = 1;
    private ImageView ivPlayGif, ivPreview;
    private SimpleDraweeView simpleDraweeView;

    public FrescoPreviewGifListener ( ImageView ivPlayGif, SimpleDraweeView simpleDraweeView, ImageView ivPreview) {
        this.ivPlayGif = ivPlayGif;
        this.ivPreview = ivPreview;
        this.simpleDraweeView = simpleDraweeView;
    }

    @Override
    public void onIntermediateImageSet(String id, Object imageInfo) {
        super.onIntermediateImageSet(id, imageInfo);
    }

    @Override
    public void onFinalImageSet(String id, Object imageInfo, final Animatable animatable) {
        super.onFinalImageSet(id, imageInfo, animatable);
        ivPreview.setVisibility(View.GONE);
        ivPlayGif.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                animatable.stop();
                ivPlayGif.setVisibility(View.VISIBLE);
            }
        } ;

        ivPlayGif.setOnClickListener(new View.OnClickListener() {
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
}
