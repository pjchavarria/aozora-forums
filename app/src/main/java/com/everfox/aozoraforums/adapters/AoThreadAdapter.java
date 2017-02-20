package com.everfox.aozoraforums.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AoTubeActivity;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
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

    private View.OnClickListener upvoteClickListener;
    private View.OnClickListener downvoteClickListener;
    private View.OnClickListener likecommentClickListener;



    public OnUpDownVoteListener mOnUpDownVote;
    public interface OnUpDownVoteListener {
        public void onUpDownVote(Boolean upvote, ParseObject object, int position);
    }

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
        mOnCommentTappedCallback = (OnCommentTappedListener) callback;
        mOnUpDownVote = (OnUpDownVoteListener) callback;
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ParseObject parseObject = threadCommentsList.get(position);

        upvoteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnUpDownVote.onUpDownVote(true,parseObject,position);
            }
        };
        downvoteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnUpDownVote.onUpDownVote(false,parseObject,position);
            }
        };
        likecommentClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnUpDownVote.onUpDownVote(true,parseObject,position);
            }
        };

        switch (holder.getItemViewType()) {
            case ITEM_FIRST_POST:
                ViewHolderThread vhFP = (ViewHolderThread) holder;
                configureViewHolderThread(vhFP, (AoThread)parseObject);
                break;
            case ITEM_COMMENT:
                ViewHolderComment vhComment = (ViewHolderComment) holder;
                configureViewHolderComment(vhComment,(Post)parseObject);
                break;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if(!payloads.isEmpty()) {
            if (payloads.get(0) instanceof AoThread) {
                AoThread aoThread = (AoThread)payloads.get(0);
                ViewHolderThread vhFP = (ViewHolderThread) holder;
                updateUpvoteDownvote(vhFP.ivUpvotes,vhFP.tvUpvotes,vhFP.ivDownvotes,vhFP.tvDownvotes,aoThread);
            } else if (payloads.get(0) instanceof Post) {
                Post comment = (Post)payloads.get(0);
                ViewHolderComment vhComment = (ViewHolderComment) holder;
                updateLike(vhComment.ivCommentLikes,vhComment.tvCommentNumberLikes,comment);
            }
        } else {
            super.onBindViewHolder(holder,position, payloads);
        }
    }

    private void updateUpvoteDownvote(ImageView ivUp, TextView tvUp, ImageView ivDown, TextView tvDown, AoThread aoThread) {
        List<ParseUser> listLiked = aoThread.getList(TimelinePost.LIKED_BY);
        if(listLiked != null && listLiked.contains(currentUser)) {
            ivUp.setImageResource(R.drawable.icon_upvote_filled);
        } else {
            ivUp.setImageResource(R.drawable.icon_upvote);
        }
        tvUp.setText(AoUtils.numberToStringOrZero(aoThread.getNumber(TimelinePost.LIKE_COUNT)));
        List<ParseUser> listUnliked = aoThread.getList(AoThread.UNLIKED_BY);
        if(listUnliked != null && listUnliked.contains(currentUser)) {
            ivDown.setImageResource(R.drawable.icon_downvote_filled);
        }else {
            ivDown.setImageResource(R.drawable.icon_downvote);
        }
        tvDown.setText(AoUtils.numberToStringOrZero(aoThread.getNumber(AoThread.UNLIKE_COUNT)));
    }

    private void updateLike(ImageView ivCommentLikes, TextView tvCommentNumberLikes,  Post post) {
        List<ParseUser> listLastCommentLiked = post.getList(TimelinePost.LIKED_BY);
        if(listLastCommentLiked != null && listLastCommentLiked.contains(currentUser)) {
            ivCommentLikes.setImageResource(R.drawable.icon_like_filled_small);
        } else {
            ivCommentLikes.setImageResource(R.drawable.icon_like_small);
        }
        tvCommentNumberLikes.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.LIKE_COUNT)));
    }



    private void configureViewHolderThread(ViewHolderThread viewHolder, final AoThread aoThread) {

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
            viewHolder.ivThreadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, AoTubeActivity.class);
                    i.putExtra(AoTubeActivity.YOUTUBEID_PARAM, aoThread.getString(TimelinePost.YOUTUBE_ID));
                    context.startActivity(i);
                }
            });
        }else if (aoThread.getJSONObject(TimelinePost.LINK) != null) {
            viewHolder.rlThreadContent.setVisibility(View.INVISIBLE);
            PostUtils.loadLinkIntoLinkLayout(context,aoThread,viewHolder.llLinkLayout);
        }

        //Load Upvote/Downvote/Comments
        updateUpvoteDownvote(viewHolder.ivUpvotes, viewHolder.tvUpvotes, viewHolder.ivDownvotes, viewHolder.tvDownvotes,aoThread);

        viewHolder.ivUpvotes.setOnClickListener(upvoteClickListener);
        viewHolder.tvUpvotes.setOnClickListener(upvoteClickListener);
        viewHolder.ivDownvotes.setOnClickListener(downvoteClickListener);
        viewHolder.tvDownvotes.setOnClickListener(downvoteClickListener);
        viewHolder.tvComments.setText(AoUtils.numberToStringOrZero(aoThread.getNumber(TimelinePost.REPLY_COUNT)));

    }


    private void configureViewHolderComment(ViewHolderComment vhComment, final Post post) {



        vhComment.ivCommentImage.setImageDrawable(null);
        vhComment.ivCommentImage.setVisibility(View.GONE);
        vhComment.sdvCommentImageGif.setVisibility(View.GONE);
        vhComment.ivCommentPlay.setVisibility(View.GONE);
        vhComment.tvViewPreviousComments.setVisibility(View.GONE);

        ParseUser userComment = (ParseUser)post.getParseObject(TimelinePost.POSTED_BY);
        PostUtils.loadAvatarPic(userComment.getParseFile(ParseUserColumns.AVATAR_THUMB), vhComment.ivCommentAvatar);
        if (userComment.getBoolean(ParseUserColumns.ACTIVE)) {
            vhComment.tvCommentUserActive.setVisibility(View.VISIBLE);
        }

        ThreadUtils.setCommentThreadUsernameAndText(context, post,post,vhComment.tvCommentText,mOnUsernameTappedCallback,mOnCommentTappedCallback);
        if(post.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            PostUtils.loadTimelinePostImageFileToImageView(context,post,vhComment.sdvCommentImageGif,vhComment.ivCommentImage, vhComment.ivCommentPlay,false);
        } else if(post.getJSONArray(TimelinePost.IMAGES) != null  && post.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            PostUtils.loadTimelinePostImageURLToImageView(context,post,vhComment.sdvCommentImageGif,vhComment.ivCommentImage, vhComment.ivCommentPlay,false);
        } else if(post.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,post,vhComment.ivCommentImage,vhComment.ivCommentPlay);
            vhComment.ivCommentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, AoTubeActivity.class);
                    i.putExtra(AoTubeActivity.YOUTUBEID_PARAM, post.getString(TimelinePost.YOUTUBE_ID));
                    context.startActivity(i);
                }
            });
        }

        vhComment.tvCommentWhen.setText(PostUtils.getWhenWasPosted(post));
        updateLike(vhComment.ivCommentLikes,vhComment.tvCommentNumberLikes,post);
        vhComment.ivCommentLikes.setOnClickListener(likecommentClickListener);
        vhComment.tvCommentNumberLikes.setOnClickListener(likecommentClickListener);

        //LastReply
        if(post.getParseObject(TimelinePost.LAST_REPLY) != null) {

            vhComment.llLastComment.setVisibility(View.VISIBLE);
            if(post.getInt(TimelinePost.REPLY_COUNT)>1) {
                vhComment.tvViewPreviousComments.setVisibility(View.VISIBLE);
            }
            //Load info last comment
            LoadInfoLastReply(vhComment,(Post) post.getParseObject(TimelinePost.LAST_REPLY),post);


        } else {
            vhComment.llLastComment.setVisibility(View.GONE);
        }

        vhComment.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnCommentTappedCallback.onCommentTapped(post);
            }
        });

    }

    private void LoadInfoLastReply(ViewHolderComment vhComment, Post lastComment, final Post originalPost) {

        vhComment.ivLastCommentImage.setImageDrawable(null);
        vhComment.ivLastCommentImage.setVisibility(View.GONE);
        vhComment.sdvLastCommentImageGif.setVisibility(View.GONE);
        vhComment.ivLastCommentPlay.setVisibility(View.GONE);

        ParseUser userComment = (ParseUser)lastComment.getParseObject(TimelinePost.POSTED_BY);
        PostUtils.loadAvatarPic(userComment.getParseFile(ParseUserColumns.AVATAR_THUMB), vhComment.ivLastCommentAvatar);
        if (userComment.getBoolean(ParseUserColumns.ACTIVE)) {
            vhComment.tvLastCommentUserActive.setVisibility(View.VISIBLE);
        }

        ThreadUtils.setCommentThreadUsernameAndText(context,lastComment,originalPost,vhComment.tvLastCommentText,mOnUsernameTappedCallback,mOnCommentTappedCallback);
        if(lastComment.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            PostUtils.loadTimelinePostImageFileToImageView(context,lastComment,vhComment.sdvLastCommentImageGif,vhComment.ivLastCommentImage, vhComment.ivLastCommentPlay,false);
        } else if(lastComment.getJSONArray(TimelinePost.IMAGES) != null  && lastComment.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            PostUtils.loadTimelinePostImageURLToImageView(context,lastComment,vhComment.sdvLastCommentImageGif,vhComment.ivLastCommentImage, vhComment.ivLastCommentPlay,false);
        } else if(lastComment.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,lastComment,vhComment.ivLastCommentImage,vhComment.ivLastCommentPlay);
        }

        vhComment.tvLastCommentWhen.setText(PostUtils.getWhenWasPosted(lastComment));
        vhComment.tvLastCommentNumberLikes.setText(AoUtils.numberToStringOrZero(lastComment.getNumber(TimelinePost.LIKE_COUNT)));
        List<ParseUser> listLastCommentLiked = lastComment.getList(TimelinePost.LIKED_BY);
        if(listLastCommentLiked != null && listLastCommentLiked.contains(currentUser)) {
            vhComment.ivLastCommentLikes.setImageResource(R.drawable.icon_like_filled_small);
        } else {
            vhComment.ivLastCommentLikes.setImageResource(R.drawable.icon_like_small);
        }

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
