package com.everfox.aozoraforums.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.ForumsAdapter;
import com.everfox.aozoraforums.adapters.ReputationRankAdapter;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.controllers.ReputationHelper;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/9/2017.
 */

public class ReputationRankFragment extends Fragment implements ReputationHelper.OnGetReputationListener,
ReputationRankAdapter.OnUsernameTappedListener
{

    final static Integer TOP_500 = 0;
    final static Integer FRIENDS = 1;

    Boolean active = false;

    Integer type;
    ParseUser user;
    AoLinearLayoutManager llm;
    ReputationRankAdapter reputationRankAdapter;
    Boolean isLoading = false;
    ArrayList<ParseObject> lstUsers = new ArrayList<>();
    ReputationHelper reputationHelper;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvUsers)
    RecyclerView rvUsers;
    @BindView(R.id.swipeRefreshForums)
    SwipeRefreshLayout swipeRefreshForums;
    @BindView(R.id.flActiveAll)
    FrameLayout flActiveAll;
    @BindView(R.id.tvActiveAll)
    TextView tvActiveAll;

    public static ReputationRankFragment newInstance(ParseUser user, Integer type) {
        ReputationRankFragment fragment = new ReputationRankFragment();
        fragment.user = user;
        fragment.type = type;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reputation_rank,container,false);
        ButterKnife.bind(this,view);
        setHasOptionsMenu(true);
        reputationHelper = new ReputationHelper(getActivity(),this);
        llm = new AoLinearLayoutManager(getActivity());
        rvUsers.setLayoutManager(llm);
        reputationRankAdapter = new ReputationRankAdapter(getActivity(),new ArrayList<ParseObject>(),this);
        rvUsers.setAdapter(reputationRankAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvUsers.setVisibility(View.GONE);
        isLoading = true;

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshForums.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadUsers();
            }
        });
        flActiveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoading) {
                    if (active) {
                        active = false;
                        tvActiveAll.setText("Active");
                        reloadUsers();

                    } else {
                        active = true;
                        tvActiveAll.setText("All");
                        reloadUsers();
                    }
                }
            }
        });
        loadUsers();
    }


    private void reloadUsers() {
        if(!isLoading) {
            isLoading = true;
            lstUsers.clear();
            reputationRankAdapter.notifyDataSetChanged();
            pbLoading.setVisibility(View.VISIBLE);
            rvUsers.setVisibility(View.GONE);
            loadUsers();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(type == TOP_500)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Top 500 Reputation Rank");
        else {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Friends Reputation Rank");
            flActiveAll.setVisibility(View.GONE);
        }

    }

    private void loadUsers(){

        if(type == TOP_500)
            reputationHelper.GetTopReputationRank(active);
        else
            reputationHelper.GetFriendsReputationRank();
    }


    @Override
    public void onGetReputation(List<ParseObject> users) {
        lstUsers.clear();
        lstUsers.addAll(users);
        reputationRankAdapter = new ReputationRankAdapter(getActivity(),lstUsers,this);
        rvUsers.setAdapter(reputationRankAdapter);
        pbLoading.setVisibility(View.GONE);
        rvUsers.setVisibility(View.VISIBLE);
        swipeRefreshForums.setRefreshing(false);
        isLoading = false;
    }

    @Override
    public void onUsernameTapped(ParseObject userTapped) {
        ProfileFragment profileFragment = null;
        if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
            profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null,true);
        else
            profileFragment = ProfileFragment.newInstance(null, true, false,userTapped.getObjectId(),true);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.flContent, profileFragment).addToBackStack(null).commitAllowingStateLoss();

    }
}
