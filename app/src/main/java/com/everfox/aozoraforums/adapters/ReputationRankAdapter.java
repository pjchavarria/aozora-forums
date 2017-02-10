package com.everfox.aozoraforums.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/9/2017.
 */

public class ReputationRankAdapter extends RecyclerView.Adapter<ReputationRankAdapter.ViewHolder> {

    ArrayList<ParseObject> lstUsers;
    private Context context;


    private OnUsernameTappedListener mOnUsernameTappedCallback;
    public interface OnUsernameTappedListener {
        public void onUsernameTapped(ParseObject userTapped);
    }


    public ReputationRankAdapter(Context context, ArrayList<ParseObject> users, Fragment fragment) {
        this.context = context;
        this.lstUsers = users;
        this.mOnUsernameTappedCallback = (OnUsernameTappedListener) fragment;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        ViewHolder vh;
        View v = LayoutInflater.from(context).inflate(R.layout.layout_reputationrank_item,parent,false);
        vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final ParseObject user = lstUsers.get(position);
        holder.tvRank.setText("#" + String.valueOf(position+1));
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        PostUtils.loadAvatarPic(profilePic, holder.ivAvatar);
        holder.tvUsername.setText(user.getString(ParseUserColumns.AOZORA_USERNAME));
        holder.tvPopularity.setText(AoUtils.reputationToString(user.getNumber(ParseUserColumns.REPUTATION)));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOnUsernameTappedCallback.onUsernameTapped(user);
            }
        });
        if(user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.reputation_currentuser));
        }
    }

    @Override
    public int getItemCount() {
        return lstUsers.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvRank)
        TextView tvRank;
        @BindView(R.id.ivAvatar)
        ImageView ivAvatar;
        @BindView(R.id.tvUsername)
        TextView tvUsername;
        @BindView(R.id.tvPopularity)
        TextView tvPopularity;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
}
