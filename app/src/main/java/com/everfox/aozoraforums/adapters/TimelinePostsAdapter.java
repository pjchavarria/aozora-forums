package com.everfox.aozoraforums.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.app.Fragment;
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
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

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
    ParseUser currentUser;

    private OnUsernameTappedListener mOnUsernameTappedCallback;
    public interface OnUsernameTappedListener {
        public void onUsernameTapped(ParseUser userTapped);
    }

    private OnMoreOptionsTappedListener mOnMoreOptionsTappedCallback;
    public interface OnMoreOptionsTappedListener {
        public void onMoreOptionsTappedCallback(TimelinePost post);
    }

    private OnRepostTappedListener mOnRepostTappedListener;
    public interface OnRepostTappedListener {
        public void onRepostTappedListener(TimelinePost post, int position);
    }

    private OnLikeTappedListener mOnLikeTappedListener;
    public interface OnLikeTappedListener {
        public void onLikeTappedListener(TimelinePost post, int position);
    }

    public TimelinePostsAdapter (Context context, List<TimelinePost> tlps, Activity callback, ParseUser currentUser) {
        this.context = context;
        this.timelinePosts = tlps;
        this.currentUser = currentUser;
        awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        mOnUsernameTappedCallback = (OnUsernameTappedListener) callback;
        mOnMoreOptionsTappedCallback = (OnMoreOptionsTappedListener) callback;
        mOnLikeTappedListener = (OnLikeTappedListener) callback;
        mOnRepostTappedListener = (OnRepostTappedListener) callback;
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

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {

        if (!payloads.isEmpty()) {
            if (payloads.get(0) instanceof TimelinePost) {
                TimelinePost timelinePost = (TimelinePost)payloads.get(0);
                if(position == 0) {
                    ViewHolderFirstPost vhFP = (ViewHolderFirstPost) holder;
                    updateLikeRepost(vhFP.ivLikes,vhFP.tvLikes,vhFP.ivRepost,vhFP.tvRepost,timelinePost);
                } else {
                    ViewHolderComment vhComment = (ViewHolderComment) holder;
                    updateLike(vhComment.ivCommentLikes,vhComment.tvCommentNumberLikes,timelinePost);
                }
            }
        }else {
            super.onBindViewHolder(holder,position, payloads);
        }
    }

    private void updateLikeRepost(ImageView ivLikes, TextView tvLikes, ImageView ivRepost, TextView tvRepost, TimelinePost post) {

        List<ParseUser> listLiked = post.getList(TimelinePost.LIKED_BY);
        if(listLiked != null && listLiked.contains(currentUser)) {
            ivLikes.setImageResource(R.drawable.icon_like_filled_small);
        } else {
            ivLikes.setImageResource(R.drawable.icon_like_small);
        }
        tvLikes.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.LIKE_COUNT)));
        List<ParseUser> listRepostedBy = post.getList(TimelinePost.REPOSTED_BY);
        if(listRepostedBy != null && listRepostedBy.contains(currentUser)) {
            ivRepost.setImageResource(R.drawable.icon_repost_filled);
        }else {
            ivRepost.setImageResource(R.drawable.icon_repost);
        }
        tvRepost.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.REPOST_COUNT)));
    }

    private void updateLike(ImageView ivCommentLikes, TextView tvCommentNumberLikes, TimelinePost comment) {
        tvCommentNumberLikes.setText(AoUtils.numberToStringOrZero(comment.getNumber(TimelinePost.LIKE_COUNT)));
        List<ParseUser> listLastCommentLiked = comment.getList(TimelinePost.LIKED_BY);
        if(listLastCommentLiked != null && listLastCommentLiked.contains(currentUser)) {
            ivCommentLikes.setImageResource(R.drawable.icon_like_filled_small);
        } else {
            ivCommentLikes.setImageResource(R.drawable.icon_like_small);
        }
    }

    private void configureViewHolderFirstPost(ViewHolderFirstPost holder, final int position) {

        final TimelinePost timelinePost = timelinePosts.get(position);

        //inicializamos imagenes

        if(timelinePost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
            //OMG ES REPOST SOUND THE FKING ALARM
            TimelinePost repost = (TimelinePost)timelinePost.getParseObject(TimelinePost.REPOST_SOURCE);
            ParseUser userWhoPosted = repost.getParseUser(TimelinePost.POSTED_BY);
            loadPicOriginalPoster(holder,userWhoPosted);
            holder.tvRepostedBy.setText(context.getString(R.string.fa_icon_reposted_by)+ " " + timelinePost.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME) + " Reposted");
            holder.tvRepostedBy.setTypeface(awesomeTypeface);
            loadFirstPostInfo(repost,holder,position);
        } else {
            ParseUser userWhoPosted = timelinePost.getParseUser(TimelinePost.POSTED_BY);
            loadPicOriginalPoster(holder,userWhoPosted);
            loadFirstPostInfo(timelinePost,holder,position);
        }
        View.OnClickListener repostListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnRepostTappedListener.onRepostTappedListener(timelinePost,position);
            }
        };
        holder.ivRepost.setOnClickListener(repostListener);
        holder.tvRepost.setOnClickListener(repostListener);

    }

    private void loadPicOriginalPoster(ViewHolderFirstPost holder, ParseUser user) {
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        PostUtils.loadAvatarPic(profilePic, holder.ivAvatar);
        if (user.getBoolean(ParseUserColumns.ACTIVE)) {
            holder.tvUserActive.setVisibility(View.VISIBLE);
        }
    }


    private void loadFirstPostInfo(final TimelinePost post, final ViewHolderFirstPost holder, final int position) {
        if(!post.getParseObject(TimelinePost.USER_TIMELINE).equals(post.getParseObject(TimelinePost.POSTED_BY))) {
            // Es un post en el muro de otra persona
            PostUtils.setPostedByFromPost(context,post,holder.tvPostedBy,null,mOnUsernameTappedCallback);
        } else {
            //Post propio
            holder.tvPostedBy.setText(post.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME));
            holder.tvPostedBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnUsernameTappedCallback.onUsernameTapped(post.getParseUser(TimelinePost.POSTED_BY));
                }
            });
        }
        String edited = "";
        if (post.getBoolean(TimelinePost.EDITED))
            edited = " - Edited";
        holder.tvPostedWhen.setText(PostUtils.getWhenWasPosted(post) + edited);
        holder.tvPostText.setText(post.getString(TimelinePost.CONTENT));
        //SPOILERS
        if(post.getBoolean(TimelinePost.HAS_SPOILERS)) {
            holder.rlPostContent.setVisibility(View.GONE);
            holder.tvSpoilerText.setText(post.getString(TimelinePost.SPOILER_CONTENT));
            holder.tvSpoilerOpen.setVisibility(View.VISIBLE);
            holder.tvSpoilerOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.tvSpoilerOpen.setVisibility(View.GONE);
                    holder.tvSpoilerText.setVisibility(View.VISIBLE);
                    holder.rlPostContent.setVisibility(View.VISIBLE);
                }
            });
        }
        //LOAD VIDEO/LINK/IMAGE FOR POST
        if(post.getParseFile(TimelinePost.IMAGE) != null ) {
            //File
                PostUtils.loadTimelinePostImageFileToImageView(context,post,holder.sdvPostImageGif,holder.ivPostImage, holder.ivPlay,true);
        } else if(post.getJSONArray(TimelinePost.IMAGES) != null  && post.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
                PostUtils.loadTimelinePostImageURLToImageView(context,post,holder.sdvPostImageGif,holder.ivPostImage, holder.ivPlay,true);
        } else if(post.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,post,holder.ivPostImage, holder.ivPlay);
            //PLAY YOUTUBE ANOTHER SCREEN
            holder.ivPostImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, AoTubeActivity.class);
                    i.putExtra(AoTubeActivity.YOUTUBEID_PARAM,post.getString(TimelinePost.YOUTUBE_ID));
                    context.startActivity(i);
                }
            });

        } else if (post.getJSONObject(TimelinePost.LINK) != null) {
            PostUtils.loadLinkIntoLinkLayout(context,post,holder.llLinkLayout);
            //ABRIR LINK
            try {
                if (!post.getJSONObject(TimelinePost.LINK).getString("url").equals("")) {
                    final String link = post.getJSONObject(TimelinePost.LINK).getString("url");
                    holder.llLinkLayout.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                            context.startActivity(browserIntent);
                        }
                    });
                }
            }
            catch (JSONException jex) {
                jex.printStackTrace();
            }
        }

        //Like/Share/Comment
        updateLikeRepost(holder.ivLikes,holder.tvLikes,holder.ivRepost, holder.tvRepost,post);
        View.OnClickListener likeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnLikeTappedListener.onLikeTappedListener(post,position);
            }
        };
        holder.ivLikes.setOnClickListener(likeListener);
        holder.tvLikes.setOnClickListener(likeListener);
        holder.tvComments.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.REPLY_COUNT)));
        holder.itemView.setTag("imageLoaded");


        holder.ivMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnMoreOptionsTappedCallback.onMoreOptionsTappedCallback(post);

            }
        });
    }


    private void configureViewHolderComment(final ViewHolderComment holder, final int position) {

        //inicializamos imagenes
        holder.ivCommentImage.setImageDrawable(null);
        holder.ivCommentImage.setVisibility(View.GONE);
        holder.sdvCommentImageGif.setVisibility(View.GONE);
        holder.ivCommentPlay.setVisibility(View.GONE);
        holder.tvCommentSpoilerOpen.setVisibility(View.GONE);
        holder.tvCommentSpoilerText.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        holder.ivCommentImage.setLayoutParams(lp);

        final TimelinePost comment = timelinePosts.get(position);

        ParseUser userLastComment = (ParseUser)comment.getParseObject(TimelinePost.POSTED_BY);
        PostUtils.loadAvatarPic(userLastComment.getParseFile(ParseUserColumns.AVATAR_THUMB), holder.ivCommentAvatar);
        if (userLastComment.getBoolean(ParseUserColumns.ACTIVE)) {
            holder.tvCommentUserActive.setVisibility(View.VISIBLE);
        }
        PostUtils.setCommentUsernameAndText(context,comment,holder.tvCommentText,mOnUsernameTappedCallback);

        //SPOILERS ABREN EN COMMENTS
        if(comment.getBoolean(TimelinePost.HAS_SPOILERS)) {
            holder.rlCommentContent.setVisibility(View.GONE);
            holder.tvCommentSpoilerText.setText(comment.getString(TimelinePost.SPOILER_CONTENT));
            holder.tvCommentSpoilerOpen.setVisibility(View.VISIBLE);
            holder.tvCommentSpoilerOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.tvCommentSpoilerOpen.setVisibility(View.GONE);
                    holder.tvCommentSpoilerText.setVisibility(View.VISIBLE);
                    holder.rlCommentContent.setVisibility(View.VISIBLE);
                }
            });
        }

        //LOAD VIDEO/IMAGE / LINK???? FOR COMMENT
        if(comment.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            PostUtils.loadTimelinePostImageFileToImageView(context,comment,holder.sdvCommentImageGif,holder.ivCommentImage, holder.ivCommentPlay,false);
        } else if(comment.getJSONArray(TimelinePost.IMAGES) != null  && comment.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            PostUtils.loadTimelinePostImageURLToImageView(context,comment,holder.sdvCommentImageGif,holder.ivCommentImage, holder.ivCommentPlay,false);
        } else if(comment.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,comment,holder.ivCommentImage,holder.ivCommentPlay);
            //PLAY YOUTUBE ANOTHER SCREEN
            holder.ivCommentImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, AoTubeActivity.class);
                    i.putExtra(AoTubeActivity.YOUTUBEID_PARAM,comment.getString(TimelinePost.YOUTUBE_ID));
                    context.startActivity(i);
                }
            });
        }

        //LIKES
        String editedComment = "";
        if (comment.getBoolean(TimelinePost.EDITED))
            editedComment = " - Edited";
        holder.tvCommentWhen.setText(PostUtils.getWhenWasPosted(comment) + editedComment);
        updateLike(holder.ivCommentLikes,holder.tvCommentNumberLikes,comment);

        View.OnClickListener likeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnLikeTappedListener.onLikeTappedListener(comment,position);
            }
        };
        holder.tvCommentNumberLikes.setOnClickListener(likeListener);
        holder.ivCommentLikes.setOnClickListener(likeListener);

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
        @BindView(R.id.tvRepost) TextView tvRepost;
        @BindView(R.id.tvSpoilerOpen) TextView tvSpoilerOpen;
        @BindView(R.id.tvSpoilerText) TextView tvSpoilerText;

        public ViewHolderFirstPost(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class ViewHolderComment extends RecyclerView.ViewHolder {

        @BindView(R.id.ivCommentAvatar) ImageView ivCommentAvatar;
        @BindView(R.id.tvCommentUserActive) View tvCommentUserActive;
        @BindView(R.id.tvCommentText) TextView tvCommentText;
        @BindView(R.id.ivCommentImage) ImageView ivCommentImage;
        @BindView(R.id.tvCommentWhen) TextView tvCommentWhen;
        @BindView(R.id.ivCommentLikes) ImageView ivCommentLikes;
        @BindView(R.id.tvCommentNumberLikes) TextView tvCommentNumberLikes;
        @BindView(R.id.tvCommentSpoilerOpen) TextView tvCommentSpoilerOpen;
        @BindView(R.id.tvCommentSpoilerText) TextView tvCommentSpoilerText;
        @BindView(R.id.rlCommentContent)
        RelativeLayout rlCommentContent;
        @BindView(R.id.sdvCommentImageGif)
        SimpleDraweeView sdvCommentImageGif;
        @BindView(R.id.ivCommentPlay)
        ImageView ivCommentPlay;

        public ViewHolderComment(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
