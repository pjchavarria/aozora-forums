package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.ThreadActivity;
import com.everfox.aozoraforums.activities.TimelinePostActivity;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.everfox.aozoraforums.utils.ThreadUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ConsoleHandler;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/1/2017.
 */

public class ForumsAdapter extends RecyclerView.Adapter {

    public static final int VIEW_GLOBAL= -1;
    public static final int VIEW_PROG = 0;
    public static final int VIEW_AOART = 1;
    public static final int VIEW_AONEWS = 2;
    public static final int VIEW_AOTALK = 3;
    public static final int VIEW_AOGUR = 4;
    public static final int VIEW_AOOFFICIAL = 5;

    public int viewType = 1;
    private List<AoThread> aoThreads;
    private Context context;
    Typeface awesomeTypeface;
    private ParseUser currentUser;


    public ForumsAdapter(Context context, ArrayList<AoThread> list,int viewType) {
        this.context = context;
        this.aoThreads = list;
        awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        currentUser = ParseUser.getCurrentUser();
        this.viewType = viewType;
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
        } else if (viewType == VIEW_AONEWS)  {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_aonews_thread,parent,false);
            vh = new AoNewsViewHolder(v);
        } else if (viewType == VIEW_AOGUR || viewType == VIEW_AOOFFICIAL)  {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_aogurofficial_thread,parent,false);
            vh = new AoGurOfficialViewHolder(v);
        }  else if (viewType == VIEW_AOTALK)  {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_aogurofficial_thread,parent,false);
            vh = new AoTalkViewHolder(v);
        }
        else {

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
        } else if(holder instanceof AoNewsViewHolder) {

            AoNewsViewHolder viewHolder = (AoNewsViewHolder)holder;
            bindNewsThread(viewHolder,aoThread);
        }  else if(holder instanceof StickyViewHolder) {

            StickyViewHolder viewHolder = (StickyViewHolder)holder;
            bindStickyTread(viewHolder,aoThread);
        }  else if(holder instanceof AoGurOfficialViewHolder) {

            AoGurOfficialViewHolder viewHolder = (AoGurOfficialViewHolder)holder;
            bindAoGurOfficialTread(viewHolder,aoThread);
        } else if(holder instanceof AoTalkViewHolder) {

            AoTalkViewHolder viewHolder = (AoTalkViewHolder)holder;
            bindAoTalkThread(viewHolder,aoThread);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AozoraForumsApp.setThreadToPass(aoThread);
                Intent i = new Intent(context, ThreadActivity.class);
                context.startActivity(i);
            }
        });
    }


    private void bindAoTalkThread(AoTalkViewHolder viewHolder, AoThread aoThread) {

        //INITIALIZE
        viewHolder.ivThreadImage.setImageDrawable(null);
        viewHolder.ivThreadImage.setVisibility(View.GONE);
        viewHolder.sdvThreadImageGif.setVisibility(View.GONE);
        viewHolder.ivPlay.setVisibility(View.GONE);
        viewHolder.llLinkLayout.setVisibility(View.GONE);

        //ThreadTag
        viewHolder.tvThreadTagPostedWhen.setText(ThreadUtils.getThreadTagWhenPosted(aoThread));
        viewHolder.tvThreadTitle.setText(aoThread.getString(AoThread.TITLE));
        viewHolder.tvThreadText.setText(aoThread.getString(AoThread.CONTENT));

        viewHolder.rlThreadContent.setVisibility(View.VISIBLE);
        //Load Video/Link/Image
        if(aoThread.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            ThreadUtils.loadThreadImageFileToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false,true);
        } else if(aoThread.getJSONArray(TimelinePost.IMAGES) != null  && aoThread.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            ThreadUtils.loadThreadImageURLToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false,true);
        } else if(aoThread.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,aoThread,viewHolder.ivThreadImage, viewHolder.ivPlay);
        }else if (aoThread.getJSONObject(TimelinePost.LINK) != null) {
            viewHolder.rlThreadContent.setVisibility(View.INVISIBLE);
            PostUtils.loadLinkIntoLinkLayout(context,aoThread,viewHolder.llLinkLayout);
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


    private void bindAoGurOfficialTread(AoGurOfficialViewHolder viewHolder, final AoThread aoThread) {

        //INITIALIZE
        viewHolder.ivThreadImage.setImageDrawable(null);
        viewHolder.ivThreadImage.setVisibility(View.GONE);
        viewHolder.sdvThreadImageGif.setVisibility(View.GONE);
        viewHolder.ivPlay.setVisibility(View.GONE);
        viewHolder.llLinkLayout.setVisibility(View.GONE);

        //ThreadTag
        viewHolder.tvThreadTagPostedWhen.setText(ThreadUtils.getThreadTagWhenPosted(aoThread));
        if(viewType == VIEW_AOGUR) {
            viewHolder.tvThreadTitle.setText(aoThread.getString(AoThread.TITLE));
            viewHolder.tvThreadText.setVisibility(View.GONE);
        }
        else {
            viewHolder.tvThreadTitle.setText(aoThread.getString(AoThread.TITLE));
            viewHolder.tvThreadText.setText(aoThread.getString(AoThread.CONTENT));
        }
        viewHolder.rlThreadContent.setVisibility(View.VISIBLE);
        //Load Video/Link/Image
        if(aoThread.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            ThreadUtils.loadThreadImageFileToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false,false);
        } else if(aoThread.getJSONArray(TimelinePost.IMAGES) != null  && aoThread.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            ThreadUtils.loadThreadImageURLToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false,false);
        } else if(aoThread.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,aoThread,viewHolder.ivThreadImage, viewHolder.ivPlay);
        }else if (aoThread.getJSONObject(TimelinePost.LINK) != null) {
            viewHolder.rlThreadContent.setVisibility(View.INVISIBLE);
            PostUtils.loadLinkIntoLinkLayout(context,aoThread,viewHolder.llLinkLayout);
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

    private void bindNewsThread(AoNewsViewHolder viewHolder, AoThread aoThread) {

        //INITIALIZE
        viewHolder.ivThreadImage.setImageDrawable(null);
        viewHolder.ivThreadImage.setVisibility(View.GONE);
        viewHolder.sdvThreadImageGif.setVisibility(View.GONE);
        viewHolder.ivPlay.setVisibility(View.GONE);
        viewHolder.llLinkLayout.setVisibility(View.GONE);

        //ThreadTag
        viewHolder.tvNewsTagPostedWhen.setText(ThreadUtils.getThreadTagWhenPosted(aoThread));
        viewHolder.tvNewsTitle.setText(aoThread.getString(AoThread.TITLE));
        viewHolder.rlNewsImage.setVisibility(View.VISIBLE);
        //Load Video/Link/Image
        if(aoThread.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            ThreadUtils.loadNewsImageFileToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay);
        } else if(aoThread.getJSONArray(TimelinePost.IMAGES) != null  && aoThread.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            ThreadUtils.loadNewsImageURLToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay);
        } else if(aoThread.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,aoThread,viewHolder.ivThreadImage, viewHolder.ivPlay);
        }else if (aoThread.getJSONObject(TimelinePost.LINK) != null) {
            viewHolder.rlNewsImage.setVisibility(View.INVISIBLE);
            PostUtils.loadLinkIntoLinkLayout(context,aoThread,viewHolder.llLinkLayout);
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

    private void bindAoArtThread(AoArtViewHolder viewHolder, AoThread aoThread) {

        //Initialize
        viewHolder.ivThreadImage.setImageDrawable(null);
        viewHolder.ivThreadImage.setVisibility(View.GONE);
        viewHolder.sdvThreadImageGif.setVisibility(View.GONE);
        viewHolder.ivPlay.setVisibility(View.GONE);

        //ThreadTag
        viewHolder.tvArtTagPostedWhen.setText(ThreadUtils.getThreadTagWhenPosted(aoThread));

        //LOAD VIDEO/LINK/IMAGE FOR THREAD
        if(aoThread.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            ThreadUtils.loadThreadImageFileToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false,false);
        } else if(aoThread.getJSONArray(TimelinePost.IMAGES) != null  && aoThread.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            ThreadUtils.loadThreadImageURLToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,false,false);
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

        if(aoThreads.get(position).getObjectId() == null)
            return VIEW_PROG;
        else {
            if(aoThreads.get(position).getString(AoThread.PIN_TYPE) == null) {
                if (aoThreads.get(position).getString(AoThread.SUBTYPE).equals(AoConstants.AOART))
                    return VIEW_AOART;
                if (aoThreads.get(position).getString(AoThread.SUBTYPE).equals(AoConstants.AOGUR))
                    return VIEW_AOGUR;
                if (aoThreads.get(position).getString(AoThread.SUBTYPE).equals(AoConstants.AONEWS))
                    return VIEW_AONEWS;
                if (aoThreads.get(position).getString(AoThread.SUBTYPE).equals(AoConstants.AOTALK))
                    return VIEW_AOTALK;
                if (aoThreads.get(position).getString(AoThread.SUBTYPE).equals(AoConstants.OFFICIAL))
                    return VIEW_AOOFFICIAL;
            } else {
                if(aoThreads.get(position).getString(AoThread.PIN_TYPE).equals(AoConstants.PINTYPE_GLOBAL))
                    if(viewType != VIEW_AOOFFICIAL)
                        return VIEW_GLOBAL;
                    else
                        return VIEW_AOOFFICIAL;
            }
        }

        return VIEW_AOGUR;
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

    public static class AoNewsViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvNewsTagPostedWhen)
        TextView tvNewsTagPostedWhen;

        @BindView(R.id.tvNewsTitle)
        TextView tvNewsTitle;

        @BindView(R.id.sdvThreadImageGif)
        SimpleDraweeView sdvThreadImageGif;
        @BindView(R.id.ivThreadImage)
        ImageView ivThreadImage;
        @BindView(R.id.ivPlay)
        ImageView ivPlay;

        @BindView(R.id.rlNewsImage)
        RelativeLayout rlNewsImage;

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

        @BindView(R.id.llLinkLayout)
        LinearLayout llLinkLayout;
        @BindView(R.id.ivLinkImage)
        ImageView ivLinkImage;
        @BindView(R.id.tvLinkTitle)
        TextView tvLinkTitle;
        @BindView(R.id.tvLinkDesc)
        TextView tvLinkDesc;
        @BindView(R.id.tvLinkURL)
        TextView tvLinkURL;

        @BindView(R.id.ivGoToLink)
        ImageView ivGoToLink;

        public AoNewsViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class AoGurOfficialViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvThreadTagPostedWhen)
        TextView tvThreadTagPostedWhen;

        @BindView(R.id.tvThreadTitle)
        TextView tvThreadTitle;
        @BindView(R.id.tvThreadText)
        TextView tvThreadText;

        @BindView(R.id.sdvThreadImageGif)
        SimpleDraweeView sdvThreadImageGif;
        @BindView(R.id.ivThreadImage)
        ImageView ivThreadImage;
        @BindView(R.id.ivPlay)
        ImageView ivPlay;

        @BindView(R.id.rlThreadContent)
        RelativeLayout rlThreadContent;

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

        @BindView(R.id.llLinkLayout)
        LinearLayout llLinkLayout;
        @BindView(R.id.ivLinkImage)
        ImageView ivLinkImage;
        @BindView(R.id.tvLinkTitle)
        TextView tvLinkTitle;
        @BindView(R.id.tvLinkDesc)
        TextView tvLinkDesc;
        @BindView(R.id.tvLinkURL)
        TextView tvLinkURL;

        @BindView(R.id.ivGoToLink)
        ImageView ivGoToLink;

        public AoGurOfficialViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class AoTalkViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvThreadTagPostedWhen)
        TextView tvThreadTagPostedWhen;

        @BindView(R.id.tvThreadTitle)
        TextView tvThreadTitle;
        @BindView(R.id.tvThreadText)
        TextView tvThreadText;

        @BindView(R.id.sdvThreadImageGif)
        SimpleDraweeView sdvThreadImageGif;
        @BindView(R.id.ivThreadImage)
        ImageView ivThreadImage;
        @BindView(R.id.ivPlay)
        ImageView ivPlay;

        @BindView(R.id.rlThreadContent)
        RelativeLayout rlThreadContent;

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

        @BindView(R.id.llLinkLayout)
        LinearLayout llLinkLayout;
        @BindView(R.id.ivLinkImage)
        ImageView ivLinkImage;
        @BindView(R.id.tvLinkTitle)
        TextView tvLinkTitle;
        @BindView(R.id.tvLinkDesc)
        TextView tvLinkDesc;
        @BindView(R.id.tvLinkURL)
        TextView tvLinkURL;

        @BindView(R.id.ivGoToLink)
        ImageView ivGoToLink;

        public AoTalkViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}