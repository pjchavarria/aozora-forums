package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
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
import com.everfox.aozoraforums.activities.TimelinePostActivity;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.facebook.drawee.drawable.RoundedCornersDrawable;
import com.facebook.drawee.view.SimpleDraweeView;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class ProfileTimelineAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;

    private OnItemTappedListener mOnItemTappedListener;
    public interface OnItemTappedListener {
        public void onItemTappedListener(TimelinePost post, int position);
    }

    private OnMoreOptionsTappedListener mOnMoreOptionsTappedCallback;
    public interface OnMoreOptionsTappedListener {
        public void onMoreOptionsTappedCallback(TimelinePost post, int position);
    }

    private OnUsernameTappedListener mOnUsernameTappedCallback;
    public interface OnUsernameTappedListener {
        public void onUsernameTapped(ParseUser userTapped);
    }

    private OnLikeTappedListener mOnLikeTappedListener;
    public interface OnLikeTappedListener {
        public void onLikeTappedListener(TimelinePost post, int position);
    }

    private OnRepostTappedListener mOnRepostTappedListener;
    public interface OnRepostTappedListener {
        public void onRepostTappedListener(TimelinePost post, int position);
    }


    private List<TimelinePost> timelinePosts;
    private Context context;
    Typeface awesomeTypeface;
    private ParseUser currentUser;


    public ProfileTimelineAdapter (Context context, List<TimelinePost> tlps, Fragment callback, ParseUser parseUser) {
        this.context = context;
        this.timelinePosts = tlps;
        this.currentUser = parseUser;
        awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        mOnUsernameTappedCallback = (OnUsernameTappedListener) callback;
        mOnMoreOptionsTappedCallback = (OnMoreOptionsTappedListener) callback;
        mOnItemTappedListener = (OnItemTappedListener) callback;
        mOnLikeTappedListener = (OnLikeTappedListener) callback;
        mOnRepostTappedListener = (OnRepostTappedListener) callback;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.timeline_post_item, parent, false);

            vh = new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.progress_item, parent, false);

            vh = new ProgressViewHolder(v);
        }

        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if(holder instanceof ProgressViewHolder) {


        } else {
            ViewHolder itemViewHolder = (ViewHolder) holder;
            //si tiene repostsource sacar info de ahi
            final TimelinePost timelinePost = timelinePosts.get(position);
            if (timelinePost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
                //OMG ES REPOST SOUND THE FKING ALARM
                TimelinePost repost = (TimelinePost) timelinePost.getParseObject(TimelinePost.REPOST_SOURCE);
                ParseUser userWhoPosted = repost.getParseUser(TimelinePost.POSTED_BY);
                ParseUser userWhoPostedRepost = timelinePost.getParseUser(TimelinePost.POSTED_BY);
                loadPicOriginalPoster(itemViewHolder, userWhoPosted);
                itemViewHolder.tvRepostedBy.setText(context.getString(R.string.fa_icon_reposted_by) + " " + userWhoPostedRepost.getString(ParseUserColumns.AOZORA_USERNAME) + " Reposted");
                itemViewHolder.tvRepostedBy.setTypeface(awesomeTypeface);
                loadTimelinePostInfo(repost, itemViewHolder,position);

            } else {

                ParseUser userWhoPosted = timelinePost.getParseUser(TimelinePost.POSTED_BY);
                loadPicOriginalPoster(itemViewHolder, userWhoPosted);
                loadTimelinePostInfo(timelinePost, itemViewHolder, position);
            }
            View.OnClickListener repostListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnRepostTappedListener.onRepostTappedListener(timelinePost,position);
                }
            };
            ((ViewHolder) holder).ivRepost.setOnClickListener(repostListener);
            ((ViewHolder) holder).tvRepost.setOnClickListener(repostListener);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemTappedListener.onItemTappedListener(timelinePost,position);
                }
            });



        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if(!payloads.isEmpty()) {
            if (payloads.get(0) instanceof TimelinePost) {
                TimelinePost timelinePost = (TimelinePost)payloads.get(0);
                ViewHolder viewHolder = (ViewHolder)holder;
                updateLikeRepost(viewHolder.ivLikes,viewHolder.tvLikes,viewHolder.ivRepost,viewHolder.tvRepost,timelinePost);
            }
        }else {
            super.onBindViewHolder(holder,position, payloads);
        }
    }

    private void updateLikeRepost(ImageView ivLikes, TextView tvLikes, ImageView ivRepost, TextView tvRepost, TimelinePost post) {
        //Like/Share/Comment
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
        } else {
            ivRepost.setImageResource(R.drawable.icon_repost);
        }
        tvRepost.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.REPOST_COUNT)));
    }

    private void loadPicOriginalPoster(ViewHolder holder, ParseUser user) {
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        PostUtils.loadAvatarPic(profilePic, holder.ivAvatar);
        if (user.getBoolean(ParseUserColumns.ACTIVE)) {
            holder.tvUserActive.setVisibility(View.VISIBLE);
        }
    }

    private void loadTimelinePostInfo(final TimelinePost post, final ViewHolder holder, final int position) {


        //inicialamos imagenes
        holder.ivPostImage.setImageDrawable(null);
        holder.ivPostImage.setVisibility(View.GONE);
        holder.sdvPostImageGif.setVisibility(View.GONE);
        holder.ivPlay.setVisibility(View.GONE);
        holder.llLinkLayout.setVisibility(View.GONE);
        holder.tvSpoilerOpen.setVisibility(View.GONE);
        holder.tvSpoilerText.setVisibility(View.GONE);
        holder.ivCommentImage.setImageDrawable(null);
        holder.ivCommentImage.setVisibility(View.GONE);
        holder.sdvCommentImageGif.setVisibility(View.GONE);
        holder.ivCommentPlay.setVisibility(View.GONE);
        holder.tvCommentSpoilerOpen.setVisibility(View.GONE);
        holder.tvCommentSpoilerText.setVisibility(View.GONE);
        holder.tvViewPreviousComments.setVisibility(View.GONE);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        holder.ivPostImage.setLayoutParams(lp);

        if(!post.getParseObject(TimelinePost.USER_TIMELINE).equals(post.getParseObject(TimelinePost.POSTED_BY))) {
            // Es un post en el muro de otra persona
            PostUtils.setPostedByFromPost(context,post,holder.tvPostedBy,mOnUsernameTappedCallback,null);
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
        if(post.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            PostUtils.loadTimelinePostImageFileToImageView(context,post,holder.sdvPostImageGif,holder.ivPostImage, holder.ivPlay,false);
        } else if(post.getJSONArray(TimelinePost.IMAGES) != null  && post.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            PostUtils.loadTimelinePostImageURLToImageView(context,post,holder.sdvPostImageGif,holder.ivPostImage, holder.ivPlay,false);
        } else if(post.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,post,holder.ivPostImage, holder.ivPlay);
        } else if (post.getJSONObject(TimelinePost.LINK) != null) {
            PostUtils.loadLinkIntoLinkLayout(context,post,holder.llLinkLayout);
        }

        //Like/Share/Comment
        updateLikeRepost(holder.ivLikes,holder.tvLikes,holder.ivRepost,holder.tvRepost,post);
        View.OnClickListener likeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnLikeTappedListener.onLikeTappedListener(post,position);
            }
        };

        holder.ivLikes.setOnClickListener(likeListener);
        holder.tvLikes.setOnClickListener(likeListener);
        holder.tvComments.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.REPLY_COUNT)));
        holder.ivMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnMoreOptionsTappedCallback.onMoreOptionsTappedCallback(post,position);

            }
        });

        //LastReply
        if(post.getParseObject(TimelinePost.LAST_REPLY) != null) {

            holder.llLastComment.setVisibility(View.VISIBLE);
            if(post.getInt(TimelinePost.REPLY_COUNT)>1) {
                holder.tvViewPreviousComments.setVisibility(View.VISIBLE);
            }

            TimelinePost lastReply = (TimelinePost) post.getParseObject(TimelinePost.LAST_REPLY);
            ParseUser userLastComment = (ParseUser)lastReply.getParseObject(TimelinePost.POSTED_BY);
            PostUtils.loadAvatarPic(userLastComment.getParseFile(ParseUserColumns.AVATAR_THUMB), holder.ivCommentAvatar);
            if (userLastComment.getBoolean(ParseUserColumns.ACTIVE)) {
                holder.tvCommentUserActive.setVisibility(View.VISIBLE);
            }
            Spannable username = new SpannableString(userLastComment.getString(ParseUserColumns.AOZORA_USERNAME));
            username.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.inapp_blue)), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvCommentText.setText(username);
            Spannable content = new SpannableString(" " + lastReply.getString(TimelinePost.CONTENT));
            content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.gray3C)), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvCommentText.append(content);

            //SPOILERS NO ABREN EN COMMENTS
            if(lastReply.getBoolean(TimelinePost.HAS_SPOILERS)) {
                holder.rlCommentContent.setVisibility(View.GONE);
                holder.tvCommentSpoilerText.setText(lastReply.getString(TimelinePost.SPOILER_CONTENT));
                holder.tvCommentSpoilerOpen.setVisibility(View.VISIBLE);

            }

            //LOAD VIDEO/IMAGE / LINK???? FOR COMMENT
            if(lastReply.getParseFile(TimelinePost.IMAGE) != null) {
                //File
                PostUtils.loadTimelinePostImageFileToImageView(context,lastReply,holder.sdvCommentImageGif,holder.ivCommentImage, holder.ivCommentPlay,false);
            } else if(lastReply.getJSONArray(TimelinePost.IMAGES) != null  && lastReply.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
                PostUtils.loadTimelinePostImageURLToImageView(context,lastReply,holder.sdvCommentImageGif,holder.ivCommentImage, holder.ivCommentPlay,false);
            } else if(lastReply.getString(TimelinePost.YOUTUBE_ID) != null) {
                PostUtils.loadYoutubeImageIntoImageView(context,lastReply,holder.ivCommentImage,holder.ivCommentPlay);
            }

            //LIKES
            String editedComment = "";
            if (lastReply.getBoolean(TimelinePost.EDITED))
                editedComment = " - Edited";
            holder.tvCommentWhen.setText(PostUtils.getWhenWasPosted(lastReply) + editedComment);
            holder.tvCommentNumberLikes.setText(AoUtils.numberToStringOrZero(lastReply.getNumber(TimelinePost.LIKE_COUNT)));
            List<ParseUser> listLastCommentLiked = lastReply.getList(TimelinePost.LIKED_BY);
            if(listLastCommentLiked != null && listLastCommentLiked.contains(currentUser)) {
                holder.ivCommentLikes.setImageResource(R.drawable.icon_like_small);
            }

        } else {
            holder.llLastComment.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemViewType(int position) {
        return timelinePosts.get(position).getObjectId() != null ? VIEW_ITEM : VIEW_PROG;
    }


    @Override
    public int getItemCount() {
        return timelinePosts.size();
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.pbLoading)
        ProgressBar pbLoading;
        public ProgressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
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
        @BindView(R.id.ivPlay)
        ImageView ivPlay;
        @BindView(R.id.llLinkLayout)
        LinearLayout llLinkLayout;
        @BindView(R.id.rlPostContent)
        RelativeLayout rlPostContent;

        @BindView(R.id.llLastComment)
        LinearLayout llLastComment;

        @BindView(R.id.tvViewPreviousComments)
        TextView tvViewPreviousComments;
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

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

}
