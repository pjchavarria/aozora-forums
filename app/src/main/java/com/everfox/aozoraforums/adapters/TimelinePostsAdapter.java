package com.everfox.aozoraforums.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.TimelinePost;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/19/2017.
 */

public class TimelinePostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int ITEM_FIRST_POST = 0;
    public static final int ITEM_COMMENT = 1;

    private List<TimelinePost> timelinePosts;
    private Context context;
    Typeface awesomeTypeface;

    public TimelinePostsAdapter (Context context, List<TimelinePost> tlps, Activity callback) {
        this.context = context;
        this.timelinePosts = tlps;
        awesomeTypeface = Typeface.createFromAsset(context.getAssets(),"fonts/FontAwesome.ttf");
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM_FIRST_POST:
                View v1 = inflater.inflate(R.layout.layout_first_post, parent, false);
                viewHolder = new ViewHolderFirstPost(v1);
                break;
            case ITEM_COMMENT:
                View v2 = inflater.inflate(R.layout.layout_comment, parent, false);
                viewHolder = new ViewHolderComment(v2);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case ITEM_FIRST_POST:
                ViewHolderFirstPost vhFP = (ViewHolderFirstPost) holder;
                configureViewHolderFirstPost(vhFP,position);
                break;
            case ITEM_COMMENT:
                ViewHolderComment vhComment = (ViewHolderComment) holder;
                configureViewHolderComment(vhComment,position);
                break;
        }
    }

    private void configureViewHolderFirstPost(ViewHolderFirstPost vhFP, int position) {

    }

    private void configureViewHolderComment(ViewHolderComment vhComment, int position) {

    }

    @Override
    public int getItemCount() {
        return timelinePosts.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_FIRST_POST;
        } else if (position > 0) {
            return ITEM_COMMENT;
        }
        return -1;
    }

    public static class ViewHolderFirstPost extends RecyclerView.ViewHolder {

        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;
        @BindView(R.id.tvUserActive) View tvUserActive;
        @BindView(R.id.tvRepostedBy)
        TextView tvRepostedBy;
        @BindView(R.id.tvPostedBy) TextView tvPostedBy;
        @BindView(R.id.tvPostedWhen) TextView tvPostedWhen;
        @BindView(R.id.ivMoreOptions) ImageView ivMoreOptions;
        @BindView(R.id.tvPostText) TextView tvPostText;
        @BindView(R.id.sdvPostImageGif) com.facebook.drawee.view.SimpleDraweeView sdvPostImageGif;
        @BindView(R.id.ivPostImage) ImageView ivPostImage;
        @BindView(R.id.ivLikes) ImageView ivLikes;
        @BindView(R.id.tvLikes) TextView tvLikes;
        @BindView(R.id.ivComments) ImageView ivComments;
        @BindView(R.id.tvComments) TextView tvComments;
        @BindView(R.id.ivRepost) ImageView ivRepost;
        @BindView(R.id.ivAddReply) ImageView ivAddReply;
        @BindView(R.id.llLinkLayout)
        LinearLayout llLinkLayout;
        @BindView(R.id.ivPlay)
        ImageView ivPlay;
        @BindView(R.id.rlPostContent)
        RelativeLayout rlPostContent;

        public ViewHolderFirstPost(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class ViewHolderComment extends RecyclerView.ViewHolder {

        @BindView(R.id.llLastComment)
        LinearLayout llLastComment;
        @BindView(R.id.ivCommentAvatar) ImageView ivCommentAvatar;
        @BindView(R.id.tvCommentUserActive) View tvCommentUserActive;
        @BindView(R.id.tvCommentText) TextView tvCommentText;
        @BindView(R.id.ivCommentImage) ImageView ivCommentImage;
        @BindView(R.id.tvCommentWhen) TextView tvCommentWhen;
        @BindView(R.id.ivCommentLikes) ImageView ivCommentLikes;
        @BindView(R.id.tvCommentNumberLikes) TextView tvCommentNumberLikes;
        @BindView(R.id.tvRepost) TextView tvRepost;
        @BindView(R.id.tvSpoilerOpen) TextView tvSpoilerOpen;
        @BindView(R.id.tvSpoilerText) TextView tvSpoilerText;
        @BindView(R.id.tvCommentSpoilerOpen) TextView tvCommentSpoilerOpen;
        @BindView(R.id.tvCommentSpoilerText) TextView tvCommentSpoilerText;
        @BindView(R.id.rlCommentContent)
        RelativeLayout rlCommentContent;
        @BindView(R.id.sdvCommentImageGif)
        SimpleDraweeView sdvCommentImageGif;
        @BindView(R.id.ivCommentPlay)
        ImageView ivCommentPlay;
        @BindView(R.id.llLinkLayout)
        LinearLayout llLinkLayout;

        public ViewHolderComment(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
