package com.everfox.aozoraforums.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.FollowersAdapter;
import com.everfox.aozoraforums.controllers.UserListHelper;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/10/2017.
 */

public class UserListFragment extends Fragment implements UserListHelper.OnGetUsersListener, FollowersAdapter.OnFollowTappedListener, FollowersAdapter.OnUserTappedListener {


    Boolean showFollow = true;
    public static final int ONLINE_NOW = 0;
    public static final int NEW_USERS = 1;
    public static final int AOZORA_STAFF = 2;
    public static final int NEW_PRO_MEMBERS = 3;
    public static final int OLDEST_ACTIVE_USERS = 4;

    UserListHelper userListHelper;
    ParseUser user;
    GridLayoutManager gridLayoutManager;
    int listType;
    FollowersAdapter userAdapter;
    String title = "";

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvFollowers)
    RecyclerView rvUsers;

    public static UserListFragment newInstance(int type) {
        UserListFragment userListFragment = new UserListFragment();
        userListFragment.listType = type;
        userListFragment.user = ParseUser.getCurrentUser();
        return userListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followers,container,false);
        ButterKnife.bind(this,view);
        setHasOptionsMenu(true);
        gridLayoutManager = new GridLayoutManager(getActivity(),3);
        userAdapter = new FollowersAdapter(getActivity(),new ArrayList<PUser>(),this,user,showFollow);
        rvUsers.setLayoutManager(gridLayoutManager);
        rvUsers.setAdapter(userAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
        userListHelper = new UserListHelper(getActivity(),this);
        switch (listType) {
            case ONLINE_NOW:
                userListHelper.GetOnlineNow();
                title = "Online Now";
                showFollow = false;
                break;
            case NEW_USERS:
                userListHelper.GetNewUsers();
                title = "New Users";
                showFollow = false;
                break;
            case AOZORA_STAFF:
                userListHelper.GetAozoraStaff();
                title = "Aozora Staff";
                showFollow = false;
                break;
            case NEW_PRO_MEMBERS:
                userListHelper.GetNewProMembers();
                title = "New Pro Members";
                break;
            case OLDEST_ACTIVE_USERS:
                userListHelper.GetOldestActiveUsers();
                title = "Oldest Active Users";
                break;
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        rvUsers.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
    }

    @Override
    public void onGetUsersListener(List<PUser> users) {

        userAdapter = new FollowersAdapter(getActivity(),users,this,user,showFollow);
        rvUsers.setAdapter(userAdapter);
        pbLoading.setVisibility(View.GONE);
        rvUsers.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFollowTapped(PUser userTapped, Boolean isFollowing, int position) {
    }

    @Override
    public void onUserTapped(PUser userTapped) {

        if(!AoUtils.isActivityInvalid(getActivity())) {
            if(!user.getObjectId().equals(userTapped.getObjectId())) {
                rvUsers.setVisibility(View.GONE);
                ProfileFragment pf = null;
                if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                    pf = ProfileFragment.newInstance(userTapped, true, true,null,true);
                else
                    pf = ProfileFragment.newInstance(userTapped, true, false,null,true);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.flContent, pf).addToBackStack(null).commitAllowingStateLoss();
            }
        }
    }
}
