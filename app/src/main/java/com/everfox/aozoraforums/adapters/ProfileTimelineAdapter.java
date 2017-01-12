package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.sql.Time;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/12/2017.
 */

public class ProfileTimelineAdapter extends RecyclerView.Adapter<ProfileTimelineAdapter.ViewHolder> {

    private List<TimelinePost> timelinePosts;
    private Context context;
    private ParseUser currentUser;


    public ProfileTimelineAdapter (Context context, List<TimelinePost> tlps, Fragment callback, ParseUser parseUser) {
        this.context = context;
        this.timelinePosts = tlps;
        this.currentUser = parseUser;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(context).inflate(R.layout.timeline_post_item, parent, false);
        ViewHolder vh = new ViewHolder(layoutView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //si tiene repostsource sacar info de ahi
        TimelinePost timelinePost = timelinePosts.get(position);
        if(timelinePost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
            //OMG ES REPOST SOUND THE FKING ALARM
            TimelinePost repost = (TimelinePost)timelinePost.getParseObject(TimelinePost.REPOST_SOURCE);
            ParseUser userWhoPosted = repost.getParseUser(TimelinePost.POSTED_BY);
            loadPicOriginalPoster(holder,userWhoPosted);
            holder.tvRepostedBy.setText(context.getString(R.string.fa_icon_reposted_by)+ " " + currentUser.getString(ParseUserColumns.AOZORA_USERNAME) + " Reposted");
            loadTimelinePostInfo(repost,holder);
        } else {

            ParseUser userWhoPosted = timelinePost.getParseUser(TimelinePost.POSTED_BY);
            loadPicOriginalPoster(holder,userWhoPosted);
            loadTimelinePostInfo(timelinePost,holder);
        }
    }

    private void loadPicOriginalPoster(ViewHolder holder, ParseUser user) {
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        loadAvatarPic(profilePic, holder.ivAvatar);
        if (user.getBoolean(ParseUserColumns.ACTIVE)) {
            holder.tvUserActive.setVisibility(View.VISIBLE);
        }
    }

    private void loadTimelinePostInfo(TimelinePost post,ViewHolder holder) {
        if(!post.getParseObject(TimelinePost.USER_TIMELINE).equals(post.getParseObject(TimelinePost.POSTED_BY))) {
            // Es un post en el muro de otra persona
            holder.tvPostedBy.setText(post.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME) +" " + context.getString(R.string.fa_icon_reposted_by) + " " + post.getParseObject(TimelinePost.USER_TIMELINE).getString(ParseUserColumns.AOZORA_USERNAME));
        } else {
            //Post propio
            holder.tvPostedBy.setText(post.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME));
        }
        holder.tvPostedWhen.setText(PostUtils.getWhenWasPosted(post));
        holder.tvPostText.setText(post.getString(TimelinePost.CONTENT));
        //LOAD VIDEO/LINK/IMAGE

        //Like/Share/Comment
        List<ParseUser> listLiked = post.getList(TimelinePost.LIKED_BY);
        if(listLiked != null && listLiked.contains(currentUser)) {
            holder.ivLikes.setImageResource(R.drawable.green_circle);
        }
        holder.tvLikes.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.LIKE_COUNT)));
        holder.tvComments.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.REPLY_COUNT)));
        List<ParseUser> listRepostedBy = post.getList(TimelinePost.REPOSTED_BY);
        if(listRepostedBy != null && listRepostedBy.contains(currentUser)) {
            holder.ivRepost.setImageResource(R.drawable.green_circle);
        }
        holder.tvRepost.setText(AoUtils.numberToStringOrZero(post.getNumber(TimelinePost.REPOST_COUNT)));

        //LastReply
        if(post.getParseObject(TimelinePost.LAST_REPLY) != null) {
            TimelinePost lastReply = (TimelinePost) post.getParseObject(TimelinePost.LAST_REPLY);
            ParseUser userLastComment = (ParseUser)lastReply.getParseObject(TimelinePost.POSTED_BY);
            loadAvatarPic(userLastComment.getParseFile(ParseUserColumns.AVATAR_THUMB), holder.ivLastCommentAvatar);
            if (userLastComment.getBoolean(ParseUserColumns.ACTIVE)) {
                holder.tvLastCommentUserActive.setVisibility(View.VISIBLE);
            }
            Spannable username = new SpannableString(userLastComment.getString(ParseUserColumns.AOZORA_USERNAME));
            username.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context,R.color.inapp_blue)), 0, username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvLastCommentText.setText(username);
            Spannable content = new SpannableString(" " + lastReply.getString(TimelinePost.CONTENT));
            content.setSpan(new ForegroundColorSpan(Color.BLACK), 0, content.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tvLastCommentText.append(content);
            //LOAD VIDEO/IMAGE

            //LIKES
            holder.tvLastCommentWhen.setText(PostUtils.getWhenWasPosted(lastReply));
            holder.tvLastCommentNumberLikes.setText(AoUtils.numberToStringOrZero(lastReply.getNumber(TimelinePost.LIKE_COUNT)));
            List<ParseUser> listLastCommentLiked = lastReply.getList(TimelinePost.LIKED_BY);
            if(listLastCommentLiked != null && listLastCommentLiked.contains(currentUser)) {
                holder.ivLastCommentLikes.setImageResource(R.drawable.green_circle);
            }


        } else {
            holder.llLastComment.setVisibility(View.GONE);
        }
    }

    private void loadAvatarPic(ParseFile profilePic, final ImageView ivAvatar) {
        if(profilePic != null) {
            profilePic.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(data, 0, data.length);
                        ivAvatar.setImageBitmap(bmp);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return timelinePosts.size();
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
        @BindView(R.id.ivPostImage) ImageView ivPostImage;
        @BindView(R.id.ivLikes) ImageView ivLikes;
        @BindView(R.id.tvLikes) TextView tvLikes;
        @BindView(R.id.ivComments) ImageView ivComments;
        @BindView(R.id.tvComments) TextView tvComments;
        @BindView(R.id.ivRepost) ImageView ivRepost;
        @BindView(R.id.ivAddReply) ImageView ivAddReply;

        @BindView(R.id.llLastComment)
        LinearLayout llLastComment;
        @BindView(R.id.ivLastCommentAvatar) ImageView ivLastCommentAvatar;
        @BindView(R.id.tvLastCommentUserActive) View tvLastCommentUserActive;
        @BindView(R.id.tvLastCommentText) TextView tvLastCommentText;
        @BindView(R.id.ivLastCommentImage) ImageView ivLastCommentImage;
        @BindView(R.id.tvLastCommentWhen) TextView tvLastCommentWhen;
        @BindView(R.id.ivLastCommentLikes) ImageView ivLastCommentLikes;
        @BindView(R.id.tvLastCommentNumberLikes) TextView tvLastCommentNumberLikes;
        @BindView(R.id.tvRepost) TextView tvRepost;


        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }


}
