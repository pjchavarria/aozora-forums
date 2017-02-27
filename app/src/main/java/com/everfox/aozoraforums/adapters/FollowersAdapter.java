package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/27/2017.
 */

public class FollowersAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    boolean showFollow;
    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private List<PUser> lstUsers;
    private Context context;
    ParseUser userProfile;
    Typeface awesomeTypeface;

    private OnUserTappedListener mOnUserTappedCallback;
    public interface OnUserTappedListener {
        public void onUserTapped(PUser userTapped);
    }

    private OnFollowTappedListener mOnFollowTappedCallback;
    public interface OnFollowTappedListener {
        public void onFollowTapped(PUser userTapped, Boolean isFollowing, int position);
    }

    public FollowersAdapter (Context context, List<PUser> users, Fragment callback, ParseUser parseUser, Boolean showFollow) {
        this.context = context;
        this.lstUsers = users;
        this.userProfile = parseUser;
        this.showFollow = showFollow;
        awesomeTypeface = AozoraForumsApp.getAwesomeTypeface();
        mOnUserTappedCallback = (OnUserTappedListener) callback;
        mOnFollowTappedCallback = (OnFollowTappedListener) callback;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.followers_item, parent, false);

            vh = new FollowersAdapter.ItemViewHolder(v);
        } else {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.progress_item, parent, false);

            vh = new FollowersAdapter.ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if(holder instanceof FollowersAdapter.ItemViewHolder) {
            final FollowersAdapter.ItemViewHolder vh = (ItemViewHolder) holder;
            final PUser user = lstUsers.get(position);
            vh.ivAvatar.setImageDrawable(null);
            PostUtils.loadAvatarPic(user.getParseFile(ParseUserColumns.AVATAR_THUMB),vh.ivAvatar);
            vh.tvUsername.setText(user.getString(ParseUserColumns.AOZORA_USERNAME));
            if(!userProfile.equals(ParseUser.getCurrentUser()) || showFollow == false)
                vh.tvFollow.setVisibility(View.GONE);
            else {
                vh.tvFollow.setVisibility(View.VISIBLE);
                vh.tvFollow.setTypeface(awesomeTypeface);
                if(user.getFollowingThisUser()){
                    vh.tvFollow.setText(context.getString(R.string.fa_check) + " FOLLOWING");
                    vh.tvFollow.setTextColor(ContextCompat.getColor(context,R.color.inapp_blue_darker));
                } else {
                    vh.tvFollow.setText(context.getString(R.string.fa_plus) + " FOLLOW");
                    vh.tvFollow.setTextColor(ContextCompat.getColor(context,R.color.grayA5));
                }
                vh.tvFollow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    PUser.followUser(user,!user.getFollowingThisUser());
                        if(user.getFollowingThisUser()){
                            vh.tvFollow.setText(context.getString(R.string.fa_check) + " FOLLOWING");
                            vh.tvFollow.setTextColor(ContextCompat.getColor(context,R.color.inapp_blue_darker));
                        } else {
                            vh.tvFollow.setText(context.getString(R.string.fa_plus) + " FOLLOW");
                            vh.tvFollow.setTextColor(ContextCompat.getColor(context,R.color.grayA5));
                        }

                    }
                });
            }

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnUserTappedCallback.onUserTapped(user);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return lstUsers.size();
    }


    @Override
    public int getItemViewType(int position) {
        return lstUsers.get(position).getObjectId() != null ? VIEW_ITEM : VIEW_PROG;
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.pbLoading)
        ProgressBar pbLoading;
        public ProgressViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;
        @BindView(R.id.tvUsername)
        TextView tvUsername;
        @BindView(R.id.tvFollow)
        TextView tvFollow;

        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
