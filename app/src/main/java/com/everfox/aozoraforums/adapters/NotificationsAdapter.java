package com.everfox.aozoraforums.adapters;

import android.content.Context;
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
import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/25/2017.
 */

public class NotificationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {


    private final int VIEW_PROG = 0;
    private final int VIEW_ITEM = 1;
    private List<AoNotification> notifications;
    private Context context;
    private ParseUser currentUser;

    private OnNotificationTappedListener mOnNotificationTappedCallback;
    public interface OnNotificationTappedListener {
        public void mOnNotificationTapped(AoNotification notificationTaped);
    }

    public NotificationsAdapter (Context context, List<AoNotification> nots, Fragment callback, ParseUser parseUser) {
        this.context = context;
        this.notifications = nots;
        mOnNotificationTappedCallback = (OnNotificationTappedListener) callback;
        currentUser = parseUser;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.notification_item, parent, false);

            vh = new ItemViewHolder(v);
        } else {
            View v = LayoutInflater.from(context)
                    .inflate(R.layout.progress_item, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if(holder instanceof ItemViewHolder) {

            final ItemViewHolder vh = (ItemViewHolder) holder;
            final AoNotification notification = notifications.get(position);
            List<ParseObject> lstUsers = notification.getList(AoNotification.READ_BY);
            if(lstUsers.contains(currentUser)) {
                vh.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.grayF8));
            }else {
                vh.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.grayE4));
            }

            if(currentUser.equals(notification.getParseUser(AoNotification.LAST_TRIGGERED_BY))) {
                ParseObject selectedUser = notification.getParseUser(AoNotification.LAST_TRIGGERED_BY);
                List<ParseObject> lstTriggeredUsers = notification.getList(AoNotification.TRIGGERED_BY);
                for(int i=0;i<lstTriggeredUsers.size();i++) {
                    if(!lstTriggeredUsers.get(i).getObjectId().equals(selectedUser.getObjectId())) {
                        selectedUser =  lstTriggeredUsers.get(i);
                        break;
                    }
                }
                PostUtils.loadAvatarPic(selectedUser.getParseFile(ParseUserColumns.AVATAR_THUMB),vh.ivAvatar );
            } else {
                PostUtils.loadAvatarPic(notification.getParseUser(AoNotification.LAST_TRIGGERED_BY).getParseFile(ParseUserColumns.AVATAR_THUMB),vh.ivAvatar );
            }

            if(notification.getParseUser(AoNotification.OWNER).equals(currentUser)) {
                vh.tvNotificationText.setText(notification.getString(AoNotification.MESSAGE_OWNER));
            } else if(notification.getParseObject(AoNotification.LAST_TRIGGERED_BY).equals(currentUser)) {
                vh.tvNotificationText.setText(notification.getString(AoNotification.PREVIOUS_MESSAGE) == null ?
                        notification.getString(AoNotification.MESSAGE) : notification.getString(AoNotification.PREVIOUS_MESSAGE));
            } else {
                vh.tvNotificationText.setText(notification.getString(AoNotification.MESSAGE));
            }

            vh.tvNotificationWhen.setText(PostUtils.getWhenNotificationWasUpdate(notification));

            vh.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnNotificationTappedCallback.mOnNotificationTapped(notification);
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return notifications.get(position).getObjectId() != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public int getItemCount() {
        return notifications.size();
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
        @BindView(R.id.tvNotificationText)
        TextView tvNotificationText;
        @BindView(R.id.tvNotificationWhen)
        TextView tvNotificationWhen;


        public ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
