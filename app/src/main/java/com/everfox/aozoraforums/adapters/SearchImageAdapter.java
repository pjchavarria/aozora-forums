package com.everfox.aozoraforums.adapters;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.controls.FrescoGifListener;
import com.everfox.aozoraforums.controls.FrescoPreviewGifListener;
import com.everfox.aozoraforums.models.ImageData;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactoryImpl;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder;
import com.facebook.imagepipeline.image.CloseableAnimatedImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/28/2017.
 */

public class SearchImageAdapter extends RecyclerView.Adapter<SearchImageAdapter.SearchViewHolder> {

    Context context;
    private ArrayList<ImageData> resultsURLs;
    private Boolean isImage;

    public OnItemClickListener mOnItemClicked;
    public interface OnItemClickListener {
        public void onItemClicked(ImageData image);
    }

    public SearchImageAdapter (Context context, ArrayList<ImageData> resultsURLs, Boolean isImage, Activity callback) {
        this.context = context;
        this.resultsURLs = resultsURLs;
        this.isImage = isImage;
        mOnItemClicked = (OnItemClickListener) callback;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_searchimage_item,parent,false);
        SearchViewHolder searchViewHolder = new SearchViewHolder(v);
        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, final int position) {

        holder.pbLoading.setVisibility(View.GONE);
        holder.ivSearchImageGifPreview.setImageDrawable(null);
        holder.ivSearchImage.setImageDrawable(null);
        holder.ivSearchImageGifPreview.setVisibility(View.GONE);
        holder.ivSearchImage.setVisibility(View.GONE);
        holder.sdvSearchGif.setVisibility(View.GONE);
        holder.ivPlay.setVisibility(View.GONE);
        String urlImage = resultsURLs.get(position).getImageURL();
        if(isImage) {
            holder.ivSearchImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(urlImage).crossFade().centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.ivSearchImage);
            holder.ivSearchImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClicked.onItemClicked(resultsURLs.get(position));
                }
            });
        } else {
            holder.pbLoading.setVisibility(View.VISIBLE);
            holder.ivSearchImageGifPreview.setVisibility(View.VISIBLE);
            holder.sdvSearchGif.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(urlImage)
                    .asBitmap()
                    .priority(Priority.IMMEDIATE)
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .centerCrop().into(holder.ivSearchImageGifPreview);

            if(urlImage.contains("https")) urlImage.replace("https","http");
            FrescoPreviewGifListener frescoPreviewGifListener = new FrescoPreviewGifListener(holder.ivPlay, holder.sdvSearchGif,holder.ivSearchImageGifPreview);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(urlImage)
                    .setAutoPlayAnimations(false)
                    .setControllerListener(frescoPreviewGifListener)
                    .build();
            holder.sdvSearchGif.setController(controller);
        }

        holder.sdvSearchGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnItemClicked.onItemClicked(resultsURLs.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return resultsURLs.size();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivSearchImageGifPreview)
        ImageView ivSearchImageGifPreview;
        @BindView(R.id.ivPlay)
        ImageView ivPlay;
        @BindView(R.id.ivSearchImage)
        ImageView ivSearchImage;
        @BindView(R.id.sdvSearchGif)
        SimpleDraweeView sdvSearchGif;
        @BindView(R.id.pbLoading)
        ProgressBar pbLoading;

        public SearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
