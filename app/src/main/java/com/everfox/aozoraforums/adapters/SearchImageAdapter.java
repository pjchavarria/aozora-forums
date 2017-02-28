package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.controls.FrescoGifListener;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/28/2017.
 */

public class SearchImageAdapter extends RecyclerView.Adapter<SearchImageAdapter.SearchViewHolder> {

    Context context;
    private ArrayList<String> resultsURLs;
    private Boolean isImage;

    public SearchImageAdapter (Context context, ArrayList<String> resultsURLs, Boolean isImage) {
        this.context = context;
        this.resultsURLs = resultsURLs;
        this.isImage = isImage;
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.layout_searchimage_item,parent,false);
        SearchViewHolder searchViewHolder = new SearchViewHolder(v);
        return searchViewHolder;
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {

        holder.ivSearchImage.setImageDrawable(null);
        holder.ivSearchImage.setVisibility(View.GONE);
        holder.sdvSearchGif.setVisibility(View.GONE);
        holder.ivPlay.setVisibility(View.GONE);
        String urlImage = resultsURLs.get(position);
        if(isImage) {
            holder.ivSearchImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(urlImage).crossFade().fitCenter()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT).into(holder.ivSearchImage);
        } else {

            holder.ivSearchImage.setVisibility(View.VISIBLE);
            if(urlImage.contains("https")) urlImage.replace("https","http");
            FrescoGifListener frescoGifListener = new FrescoGifListener(holder.ivPlay, holder.sdvSearchGif);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(urlImage)
                    .setAutoPlayAnimations(false)
                    .setControllerListener(frescoGifListener)
                    .build();
            holder.sdvSearchGif.setController(controller);
        }
    }

    @Override
    public int getItemCount() {
        return resultsURLs.size();
    }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ivPlay)
        ImageView ivPlay;
        @BindView(R.id.ivSearchImage)
        ImageView ivSearchImage;
        @BindView(R.id.sdvSearchGif)
        SimpleDraweeView sdvSearchGif;

        public SearchViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
