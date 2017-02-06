package com.everfox.aozoraforums.adapters;

import android.content.Context;
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
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
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


    @Override
    public void onBindViewHolder(ViewHolderComment vhComment, int position) {

        Post post = postCommentLst.get(position);

        vhComment.ivCommentImage.setImageDrawable(null);
        vhComment.ivCommentImage.setVisibility(View.GONE);
        vhComment.sdvCommentImageGif.setVisibility(View.GONE);
        vhComment.ivCommentPlay.setVisibility(View.GONE);

        ParseUser userComment = (ParseUser)post.getParseObject(TimelinePost.POSTED_BY);
        PostUtils.loadAvatarPic(userComment.getParseFile(ParseUserColumns.AVATAR_THUMB), vhComment.ivCommentAvatar);
        if (userComment.getBoolean(ParseUserColumns.ACTIVE)) {
            vhComment.tvCommentUserActive.setVisibility(View.VISIBLE);
        }
        Spannable username = new SpannableString(userComment.getString(ParseUserColumns.AOZORA_USERNAME));
        username.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.inapp_blue)), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        vhComment.tvCommentText.setText(username);
        Spannable content = new SpannableString(" " + post.getString(TimelinePost.CONTENT));
        content.setSpan(new ForegroundColorSpan(Color.BLACK), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        vhComment.tvCommentText.append(content);

        if(post.getParseFile(TimelinePost.IMAGE) != null) {
            //File
            PostUtils.loadTimelinePostImageFileToImageView(context,post,vhComment.sdvCommentImageGif,vhComment.ivCommentImage, vhComment.ivCommentPlay,true);
        } else if(post.getJSONArray(TimelinePost.IMAGES) != null  && post.getJSONArray(TimelinePost.IMAGES) .length()>0 ) {
            PostUtils.loadTimelinePostImageURLToImageView(context,post,vhComment.sdvCommentImageGif,vhComment.ivCommentImage, vhComment.ivCommentPlay,true);
        } else if(post.getString(TimelinePost.YOUTUBE_ID) != null) {
            PostUtils.loadYoutubeImageIntoImageView(context,post,vhComment.ivCommentImage,vhComment.ivCommentPlay);
        }

        vhComment.tvCommentWhen.setText(PostUtils.getWhenWasPosted(post));
        vhComment.tvCommentNumberLikes.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.LIKE_COUNT)));
        List<ParseUser> listLastCommentLiked = post.getList(TimelinePost.LIKED_BY);
        if(listLastCommentLiked != null && listLastCommentLiked.contains(currentUser)) {
            vhComment.ivCommentLikes.setImageResource(R.drawable.icon_like_small);
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
