package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AoTubeActivity;
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
 * Created by daniel.soto on 2/6/2017.
 */

public class CommentPostAdapter extends RecyclerView.Adapter<CommentPostAdapter.ViewHolderComment> {

    public static final int ITEM_FIRST_POST = 0;
    public static final int ITEM_COMMENT = 1;

    private List<Post> postCommentLst;
    private Context context;
    Typeface awesomeTypeface;
    ParseUser currentUser;


    public OnItemLongClickListener mOnItemLongClicked;
    public interface OnItemLongClickListener {
        public void onItemLongClicked(ParseObject aoThread);
    }


    public OnLikeListener mOnLikeListener;
    public interface OnLikeListener {
        public void onLike( ParseObject object, int position);
    }

    public OnUsernameTappedListener mOnUsernameTappedCallback;
    public interface OnUsernameTappedListener {
        public void onUsernameTapped(ParseUser userTapped);
    }

    public CommentPostAdapter(Context context, List<Post> postList, Fragment callback){
        this.context = context;
        this.postCommentLst = postList;
        this.currentUser = ParseUser.getCurrentUser();
        awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        mOnUsernameTappedCallback = (OnUsernameTappedListener) callback;
        mOnLikeListener = (OnLikeListener) callback;
        mOnItemLongClicked = (OnItemLongClickListener)callback;
    }

    @Override
    public ViewHolderComment onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolderComment viewHolderComment = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v1 = inflater.inflate(R.layout.layout_comment, parent, false);
        switch (viewType) {
            case ITEM_COMMENT:
                v1.setPadding(40,0,0,0);
                break;
        }
        viewHolderComment = new ViewHolderComment(v1);
        return viewHolderComment;
    }

    private void updateLike(ImageView ivCommentLikes, TextView tvCommentNumberLikes,  Post post){
        List<ParseUser> listLastCommentLiked = post.getList(TimelinePost.LIKED_BY);
        if(listLastCommentLiked != null && listLastCommentLiked.contains(currentUser)) {
            ivCommentLikes.setImageResource(R.drawable.icon_like_filled_small);
        } else {
            ivCommentLikes.setImageResource(R.drawable.icon_like_small);
        }
        tvCommentNumberLikes.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.LIKE_COUNT)));
    }

    @Override
    public void onBindViewHolder(ViewHolderComment holder, int position, List<Object> payloads) {
        if(!payloads.isEmpty()) {
            if (payloads.get(0) instanceof Post) {
                Post comment = (Post)payloads.get(0);
                updateLike(holder.ivCommentLikes,holder.tvCommentNumberLikes,comment);
            }
        } else {
            super.onBindViewHolder(holder,position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolderComment vhComment, final int position) {

        final Post post = postCommentLst.get(position);

        vhComment.ivCommentImage.setImageDrawable(null);
        vhComment.ivCommentImage.setVisibility(View.GONE);
        vhComment.sdvCommentImageGif.setVisibility(View.GONE);
        vhComment.ivCommentPlay.setVisibility(View.GONE);

        ParseUser userComment = (ParseUser)post.getParseObject(TimelinePost.POSTED_BY);
        PostUtils.loadAvatarPic(userComment.getParseFile(ParseUserColumns.AVATAR_THUMB), vhComment.ivCommentAvatar);
        if (userComment.getBoolean(ParseUserColumns.ACTIVE)) {
            vhComment.tvCommentUserActive.setVisibility(View.VISIBLE);
        }

        ThreadUtils.setCommentPostUsernameAndText(context,post,vhComment.tvCommentText,mOnUsernameTappedCallback);
        if(post.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            PostUtils.loadTimelinePostImageFileToImageView(context,post,vhComment.sdvCommentImageGif,vhComment.ivCommentImage, vhComment.ivCommentPlay,true);
        } else if(post.getJSONArray(TimelinePost.IMAGES) != null  && post.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            PostUtils.loadTimelinePostImageURLToImageView(context,post,vhComment.sdvCommentImageGif,vhComment.ivCommentImage, vhComment.ivCommentPlay,true);
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
        View.OnClickListener likeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnLikeListener.onLike(post,position);
            }
        };
        vhComment.ivCommentLikes.setOnClickListener(likeClickListener);
        vhComment.tvCommentNumberLikes.setOnClickListener(likeClickListener);

        vhComment.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mOnItemLongClicked.onItemLongClicked(post);
                return true;
            }
        });
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
        return postCommentLst.size();
    }

    public static class ViewHolderComment extends RecyclerView.ViewHolder {

        @BindView(R.id.ivCommentAvatar)
        ImageView ivCommentAvatar;
        @BindView(R.id.tvCommentUserActive)
        View tvCommentUserActive;
        @BindView(R.id.tvCommentText)
        TextView tvCommentText;
        @BindView(R.id.rlCommentContent)
        RelativeLayout rlCommentContent;
        @BindView(R.id.ivCommentImage) ImageView ivCommentImage;
        @BindView(R.id.tvCommentWhen) TextView tvCommentWhen;
        @BindView(R.id.ivCommentLikes) ImageView ivCommentLikes;
        @BindView(R.id.tvCommentNumberLikes) TextView tvCommentNumberLikes;
        @BindView(R.id.sdvCommentImageGif)
        SimpleDraweeView sdvCommentImageGif;
        @BindView(R.id.ivCommentPlay)
        ImageView ivCommentPlay;

        public ViewHolderComment(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
