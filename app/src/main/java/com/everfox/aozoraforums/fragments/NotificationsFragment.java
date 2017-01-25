package com.everfox.aozoraforums.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.TimelinePostActivity;
import com.everfox.aozoraforums.adapters.NotificationsAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.NotificationsHelper;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.models.AoNotification;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/25/2017.
 */

public class NotificationsFragment extends Fragment implements NotificationsHelper.OnGetNotificationListener, NotificationsAdapter.OnNotificationTappedListener  {

    LinearLayoutManager llm;
    NotificationsAdapter notiAdapter;
    Boolean isLoading = false;
    ArrayList<AoNotification> lstNotifications = new ArrayList<>();
    int fetchCount = 0;


    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;

    @BindView(R.id.rvNotifications)
    RecyclerView rvNotifications;

    @BindView(R.id.swipeRefreshNoti)
    SwipeRefreshLayout swipeRefreshNoti;

    public static NotificationsFragment newInstance() {
        NotificationsFragment fragment = new NotificationsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        ButterKnife.bind(this,view);

        llm = new LinearLayoutManager(getActivity());
        rvNotifications.setLayoutManager(llm);
        notiAdapter = new NotificationsAdapter(getActivity(),new ArrayList<AoNotification>(),NotificationsFragment.this, ParseUser.getCurrentUser());
        rvNotifications.setAdapter(notiAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvNotifications.setVisibility(View.GONE);
        isLoading = true;
        new NotificationsHelper(getActivity(),this)
                .GetNotifications(ParseUser.getCurrentUser(),0,NotificationsHelper.NOTIFICATION_FETCH_LIMIT);

        fetchCount++;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshNoti.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isLoading)
                    reloadNotifications();
            }
        });

        rvNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (isLoading)
                    return;
                int visibleItemCount = llm.getChildCount();
                int totalItemCount = llm.getItemCount();
                int pastVisibleItems = llm.findFirstVisibleItemPosition();
                if (pastVisibleItems + visibleItemCount >= totalItemCount && !isLoading) {
                    scrolledToEnd();
                }
            }
        });
    }

    private void scrolledToEnd() {

        isLoading = true;
        lstNotifications.add(new AoNotification());
        rvNotifications.post(new Runnable() {
            @Override
            public void run() {
                notiAdapter.notifyItemInserted(lstNotifications.size());
            }
        });
        new NotificationsHelper(getActivity(),this)
                .GetNotifications(ParseUser.getCurrentUser(),fetchCount * NotificationsHelper.NOTIFICATION_FETCH_LIMIT,NotificationsHelper.NOTIFICATION_FETCH_LIMIT);
        fetchCount++;
    }

    private void reloadNotifications() {
        fetchCount = 0;
        lstNotifications.clear();
        notiAdapter.notifyDataSetChanged();
        new NotificationsHelper(getActivity(),this)
                .GetNotifications(ParseUser.getCurrentUser(),0,NotificationsHelper.NOTIFICATION_FETCH_LIMIT);
        fetchCount++;
    }

    @Override
    public void onGetNotificationListener(List<AoNotification> _notifications) {

        if(notiAdapter.getItemCount() == 0) {
            _notifications = AoUtils.filterNotifications(new ArrayList<>(_notifications));
            lstNotifications.addAll(_notifications);
            notiAdapter = new NotificationsAdapter(getActivity(),lstNotifications,this,ParseUser.getCurrentUser());
            rvNotifications.setAdapter(notiAdapter);
            pbLoading.setVisibility(View.GONE);
            rvNotifications.setVisibility(View.VISIBLE);
            swipeRefreshNoti.setRefreshing(false);
        } else {
            lstNotifications.remove(lstNotifications.size()-1);
            notiAdapter.notifyItemRemoved(lstNotifications.size());
            if(_notifications.size()>0) {
                int currentPosition = lstNotifications.size();
                ArrayList<AoNotification> auxList = new ArrayList<>();
                auxList.addAll(lstNotifications);
                auxList.addAll(_notifications);
                auxList = AoUtils.filterNotifications(auxList);
                for (int i = lstNotifications.size(); i < auxList.size(); i++) {
                    lstNotifications.add(auxList.get(i));
                }
                notiAdapter.notifyItemRangeInserted(currentPosition, lstNotifications.size()-currentPosition);
            }
        }
        isLoading = false;
    }


    public void scrollFeedToStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rvNotifications.scrollToPosition(0);
            }
        }, 200);
    }

    @Override
    public void mOnNotificationTapped(AoNotification notificationTaped) {

    }
}
