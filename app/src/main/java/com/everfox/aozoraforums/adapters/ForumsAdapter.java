package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.everfox.aozoraforums.utils.ThreadUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/1/2017.
 */

public class ForumsAdapter extends RecyclerView.Adapter {

    private final int VIEW_GLOBAL= -1;
    private final int VIEW_PROG = 0;
    private final int VIEW_AOART = 1;
    private final int VIEW_AONEWS = 2;
    private final int VIEW_AOTALK = 3;
    private final int VIEW_AOGUROFFICIAL = 4;

    public int viewType = 1;
    private List<AoThread> aoThreads;
    private Context context;
    Typeface awesomeTypeface;
    private ParseUser currentUser;

    public ForumsAdapter(Context context, ArrayList<AoThread> list) {
        this.context = context;
        this.aoThreads = list;
        awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        currentUser = ParseUser.getCurrentUser();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if(viewType == VIEW_AOART) {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_aoart_thread,parent,false);
            vh = new AoArtViewHolder(v);
        } else if (viewType == VIEW_GLOBAL)  {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_sticky_thread,parent,false);
            vh = new StickyViewHolder(v);
        } else {

            View v = LayoutInflater.from(context)
                    .inflate(R.layout.progress_item, parent, false);
            vh = new ProgressViewHolder(v);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final AoThread aoThread = aoThreads.get(position);
        if(holder instanceof AoArtViewHolder) {

            AoArtViewHolder viewHolder = (AoArtViewHolder)holder;
            bindAoArtThread(viewHolder,aoThread);
        } else if(holder instanceof StickyViewHolder) {

            StickyViewHolder viewHolder = (StickyViewHolder)holder;
            bindStickyTread(viewHolder,aoThread);
        }
    }

    private void bindAoArtThread(AoArtViewHolder viewHolder, AoThread aoThread) {
        //ThreadTag
        List<ParseObject> lst = aoThread.getList(AoThread.TAGS);
        String threadTag = "";
        for(int i=0;i<lst.size();i++) {
            if(lst.get(i) instanceof AoThreadTag) {
                threadTag = lst.get(i).getString(AoThreadTag.NAME);
                break;
            }
        }
        String whenWasPosted = PostUtils.getWhenWasPosted(aoThread);
        viewHolder.tvArtTagPostedWhen.setText("#"+threadTag + " - " + whenWasPosted);

        //LOAD VIDEO/LINK/IMAGE FOR THREAD
        if(aoThread.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            ThreadUtils.loadThreadImageFileToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false);
        } else if(aoThread.getJSONArray(TimelinePost.IMAGES) != null  && aoThread.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            ThreadUtils.loadThreadImageURLToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false);
        } else if(aoThread.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,aoThread,viewHolder.ivThreadImage, viewHolder.ivPlay);
        }

        //Load Upvote/Downvote/Comments
        List<ParseUser> listLiked = aoThread.getList(TimelinePost.LIKED_BY);
        if(listLiked != null && listLiked.contains(currentUser)) {
            viewHolder.ivUpvotes.setImageResource(R.drawable.icon_upvote_filled);
        }
        viewHolder.tvUpvotes.setText(AoUtils.numberToStringOrZero(aoThread.getNumber(TimelinePost.LIKE_COUNT)));
        List<ParseUser> listUnliked = aoThread.getList(AoThread.UNLIKED_BY);
        if(listUnliked != null && listUnliked.contains(currentUser)) {
            viewHolder.ivDownvotes.setImageResource(R.drawable.icon_downvote_filled);
        }
        viewHolder.tvDownvotes.setText(AoUtils.numberToStringOrZero(aoThread.getNumber(AoThread.UNLIKE_COUNT)));
        viewHolder.tvComments.setText(AoUtils.numberToStringOrZero(aoThread.getNumber(TimelinePost.REPLY_COUNT)));
    }

    private void bindStickyTread(StickyViewHolder viewHolder, AoThread aoThread) {
        viewHolder.tvTitleSticky.setText(aoThread.getString(AoThread.TITLE));
        if(aoThread.getHideDivider()){
            viewHolder.vDividerSticky.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return aoThreads.get(position).getObjectId() != null ?
                aoThreads.get(position).getShowAsPinned() ? VIEW_GLOBAL : viewType : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return aoThreads.size();
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.pbLoading)
        ProgressBar pbLoading;
        public ProgressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class StickyViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitleSticky)
        TextView tvTitleSticky;
        @BindView(R.id.tvHideSticky)
        TextView tvHideSticky;
        @BindView(R.id.vDividerSticky)
        View vDividerSticky;

        public StickyViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public static class AoArtViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvArtTagPostedWhen)
        TextView tvArtTagPostedWhen;
        @BindView(R.id.sdvThreadImageGif)
        SimpleDraweeView sdvThreadImageGif;
        @BindView(R.id.ivThreadImage)
        ImageView ivThreadImage;
        @BindView(R.id.ivPlay)
        ImageView ivPlay;
        @BindView(R.id.ivUpvotes)
        ImageView ivUpvotes;
        @BindView(R.id.tvUpvotes)
        TextView tvUpvotes;
        @BindView(R.id.ivComments)
        ImageView ivComments;
        @BindView(R.id.tvComments)
        TextView tvComments;
        @BindView(R.id.ivDownvotes)
        ImageView ivDownvotes;
        @BindView(R.id.tvDownvotes)
        TextView tvDownvotes;
        @BindView(R.id.ivAddReply)
        ImageView ivAddReply;


        public AoArtViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
