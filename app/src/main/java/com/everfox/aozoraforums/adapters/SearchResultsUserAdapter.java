package com.everfox.aozoraforums.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/7/2017.
 */

public class SearchResultsUserAdapter extends RecyclerView.Adapter {

    private List<ParseUser> lstParseUser;
    private Context context;

    private OnUsernameTappedListener mOnUsernameTappedCallback;
    public interface OnUsernameTappedListener {
        public void onUsernameTapped(ParseUser userTapped);
    }


    public SearchResultsUserAdapter(Context context, List<ParseUser> lst, Activity callback) {
        this.context = context;
        this.lstParseUser = lst;
        mOnUsernameTappedCallback = (OnUsernameTappedListener) callback;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(context).inflate(R.layout.layout_searchuser_item,parent,false);
        vh = new UserViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        UserViewHolder viewHolder = (UserViewHolder)holder;
        final ParseUser user = lstParseUser.get(position);
        viewHolder.tvUsername.setText(user.getString(ParseUserColumns.AOZORA_USERNAME));
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        PostUtils.loadAvatarPic(profilePic, viewHolder.ivAvatar);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnUsernameTappedCallback.onUsernameTapped(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lstParseUser.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;

        @BindView(R.id.tvUsername)
        TextView tvUsername;
        public UserViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
