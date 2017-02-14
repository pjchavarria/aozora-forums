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
import com.everfox.aozoraforums.controllers.FollowersHelper;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/27/2017.
 */

public class FollowersFragment extends Fragment implements FollowersHelper.OnGetFollowersListener,
FollowersAdapter.OnFollowTappedListener, FollowersAdapter.OnUserTappedListener{

    FollowersHelper followersHelper;

    GridLayoutManager gridLayoutManager;
    FollowersAdapter followersAdapter;
    ParseUser user;
    Boolean isFollowers;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;

    @BindView(R.id.rvFollowers)
    RecyclerView rvFollowers;

    public static FollowersFragment newInstance(ParseUser userToSee, Boolean isFollowers) {
        FollowersFragment fragment = new FollowersFragment();
        fragment.user = userToSee;
        fragment.isFollowers = isFollowers;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_followers, container, false);
        ButterKnife.bind(this,view);
        setHasOptionsMenu(true);
        gridLayoutManager = new GridLayoutManager(getActivity(),3);
        followersAdapter = new FollowersAdapter(getActivity(),new ArrayList<PUser>(),this,user,true);
        rvFollowers.setLayoutManager(gridLayoutManager);
        rvFollowers.setAdapter(followersAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvFollowers.setVisibility(View.GONE);
        followersHelper = new FollowersHelper(getActivity(),this);
        if(isFollowers) {
            followersHelper.GetFollowers(user);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Followers");
        }
        else {
            followersHelper.GetFollowing(user);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Following");
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
        rvFollowers.setVisibility(View.VISIBLE);
        if(isFollowers) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Followers");
        }
        else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Following");
        }
    }

    @Override
    public void onGetFollowers(List<PUser> users) {

        followersAdapter = new FollowersAdapter(getActivity(),users,this,user,true);
        rvFollowers.setAdapter(followersAdapter);
        pbLoading.setVisibility(View.GONE);
        rvFollowers.setVisibility(View.VISIBLE);
    }

    @Override
    public void onFollowTapped(PUser userTapped, Boolean isFollowing, int position) {

        Toast.makeText(getActivity(),isFollowing? "Unfollow this user" : "Follow this user",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserTapped(PUser userTapped) {

        if(!AoUtils.isActivityInvalid(getActivity())) {
            if(!user.getObjectId().equals(userTapped.getObjectId())) {
                rvFollowers.setVisibility(View.GONE);
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
