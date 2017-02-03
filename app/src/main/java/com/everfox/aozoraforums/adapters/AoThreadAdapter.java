package com.everfox.aozoraforums.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.everfox.aozoraforums.utils.ThreadUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/3/2017.
 */

public class AoThreadAdapter extends RecyclerView.Adapter {

    public static final int ITEM_FIRST_POST = 0;
    public static final int ITEM_COMMENT = 1;
    private List<ParseObject> threadCommentsList;
    private Context context;
    Typeface awesomeTypeface;
    ParseUser currentUser;

    public OnUsernameTappedListener mOnUsernameTappedCallback;
    public interface OnUsernameTappedListener {
        public void onUsernameTapped(ParseUser userTapped);
    }
    public OnCommentTappedListener mOnCommentTappedCallback;
    public interface OnCommentTappedListener {
        public void onCommentTapped(ParseObject commentTapped);
    }

    public AoThreadAdapter (Context context, List<ParseObject> tclist, Activity callback) {
        this.context = context;
        this.threadCommentsList = tclist;
        this.currentUser = ParseUser.getCurrentUser();
        awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        mOnUsernameTappedCallback = (OnUsernameTappedListener) callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM_FIRST_POST:
                View v1 = inflater.inflate(R.layout.layout_first_post_thread, parent, false);
                viewHolder = new ViewHolderThread(v1);
                break;
            case ITEM_COMMENT:
                View v2 = inflater.inflate(R.layout.layout_comment_thread, parent, false);
                viewHolder = new ViewHolderComment(v2);
                break;
        }
        return viewHolder;
        
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ParseObject parseObject = threadCommentsList.get(position);
        switch (holder.getItemViewType()) {
            case ITEM_FIRST_POST:
                ViewHolderThread vhFP = (ViewHolderThread) holder;
                configureViewHolderThread(vhFP, (AoThread)parseObject);
                break;
            case ITEM_COMMENT:
                ViewHolderComment vhComment = (ViewHolderComment) holder;
                configureViewHolderComment(vhComment,position);
                break;
        }
    }


    private void configureViewHolderThread(ViewHolderThread viewHolder, AoThread aoThread) {

        //INITIALIZE
        viewHolder.ivThreadImage.setImageDrawable(null);
        viewHolder.ivThreadImage.setVisibility(View.GONE);
        viewHolder.sdvThreadImageGif.setVisibility(View.GONE);
        viewHolder.ivPlay.setVisibility(View.GONE);
        viewHolder.llLinkLayout.setVisibility(View.GONE);

        //ThreadTag
        ThreadUtils.setThreadTagWhenPostedViewsBy(aoThread,viewHolder.tvThreadTagPostedWhen, mOnUsernameTappedCallback);
        viewHolder.tvThreadTitle.setText(aoThread.getString(AoThread.TITLE));
        String content =  aoThread.getString(AoThread.CONTENT);
        if(content == null || content.length() == 0) {
            viewHolder.tvThreadText.setVisibility(View.GONE);
        } else {
            viewHolder.tvThreadText.setVisibility(View.VISIBLE);
            viewHolder.tvThreadText.setText(content);
        }

        if(aoThread.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            ThreadUtils.loadThreadImageFileToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,true,false);
        } else if(aoThread.getJSONArray(TimelinePost.IMAGES) != null  && aoThread.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            ThreadUtils.loadThreadImageURLToImageView(context,aoThread,viewHolder.sdvThreadImageGif,viewHolder.ivThreadImage, viewHolder.ivPlay,true,false);
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


    private void configureViewHolderComment(ViewHolderComment vhComment, int position) {
    }


    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return ITEM_FIRST_POST;
        } else if(position > 0) {
            return ITEM_COMMENT;
        }
        return  -1;
    }


    @Override
    public int getItemCount() {
        return threadCommentsList.size();
    }


    public static class ViewHolderComment extends RecyclerView.ViewHolder {

        @BindView(R.id.ivCommentAvatar) ImageView ivCommentAvatar;
        @BindView(R.id.tvCommentUserActive) View tvCommentUserActive;
        @BindView(R.id.tvCommentText) TextView tvCommentText;
        @BindView(R.id.tvCommentSpoilerOpen) TextView tvCommentSpoilerOpen;
        @BindView(R.id.tvCommentSpoilerText) TextView tvCommentSpoilerText;
        @BindView(R.id.rlCommentContent) RelativeLayout rlCommentContent;
        @BindView(R.id.ivCommentImage) ImageView ivCommentImage;
        @BindView(R.id.tvCommentWhen) TextView tvCommentWhen;
        @BindView(R.id.ivCommentLikes) ImageView ivCommentLikes;
        @BindView(R.id.tvCommentNumberLikes) TextView tvCommentNumberLikes;
        @BindView(R.id.sdvCommentImageGif)
        SimpleDraweeView sdvCommentImageGif;
        @BindView(R.id.ivCommentPlay)
        ImageView ivCommentPlay;
        @BindView(R.id.llLastComment)
        LinearLayout llLastComment;
        @BindView(R.id.tvViewPreviousComments)
        TextView tvViewPreviousComments;

        @BindView(R.id.ivLastCommentAvatar) ImageView ivLastCommentAvatar;
        @BindView(R.id.tvLastCommentUserActive) View tvLastCommentUserActive;
        @BindView(R.id.tvLastCommentText) TextView tvLastCommentText;
        @BindView(R.id.tvLastCommentSpoilerOpen) TextView tvLastCommentSpoilerOpen;
        @BindView(R.id.tvLastCommentSpoilerText) TextView tvLastCommentSpoilerText;
        @BindView(R.id.ivLastCommentImage) ImageView ivLastCommentImage;
        @BindView(R.id.tvLastCommentWhen) TextView tvLastCommentWhen;
        @BindView(R.id.ivLastCommentLikes) ImageView ivLastCommentLikes;
        @BindView(R.id.tvLastCommentNumberLikes) TextView tvLastCommentNumberLikes;
        @BindView(R.id.rlLastCommentContent)
        RelativeLayout rlLastCommentContent;
        @BindView(R.id.sdvLastCommentImageGif)
        SimpleDraweeView sdvLastCommentImageGif;
        @BindView(R.id.ivLastCommentPlay)
        ImageView ivLastCommentPlay;

        public ViewHolderComment(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class ViewHolderThread extends RecyclerView.ViewHolder {

        @BindView(R.id.llFirstPost)
        LinearLayout llFirstPost;
        @BindView(R.id.tvThreadTagPostedWhen)
        TextView tvThreadTagPostedWhen;
        @BindView(R.id.tvThreadTitle)
        TextView tvThreadTitle;
        @BindView(R.id.tvThreadText)
        TextView tvThreadText;
        @BindView(R.id.rlThreadContent)
        RelativeLayout rlThreadContent;
        @BindView(R.id.ivPlay)
        ImageView ivPlay;
        @BindView(R.id.ivThreadImage)
        ImageView ivThreadImage;
        @BindView(R.id.sdvThreadImageGif)
        SimpleDraweeView sdvThreadImageGif;
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

        public ViewHolderThread(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }
}
