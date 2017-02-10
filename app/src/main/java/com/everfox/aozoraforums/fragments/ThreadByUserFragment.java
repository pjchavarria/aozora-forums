package com.everfox.aozoraforums.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.ForumsAdapter;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/9/2017.
 */

public class ThreadByUserFragment extends Fragment implements ForumsHelper.OnGetUserThreadsListener {

    ParseUser user;
    LinearLayoutManager llm;
    ForumsAdapter forumsAdapter;
    Boolean isLoading = false;
    int fetchCount = 0;
    ArrayList<AoThread> lstThreads = new ArrayList<>();
    ForumsHelper forumsHelper;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvThreadByUser)
    RecyclerView rvThreadByUser;
    @BindView(R.id.swipeRefreshForums)
    SwipeRefreshLayout swipeRefreshForums;

    public static ThreadByUserFragment newInstance(ParseUser user) {
        ThreadByUserFragment threadByUserFragment = new ThreadByUserFragment();
        threadByUserFragment.user = user;
        return threadByUserFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_threadsbyuser, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);
        forumsHelper = new ForumsHelper(getActivity(), this);
        llm = new LinearLayoutManager(getActivity());
        rvThreadByUser.setLayoutManager(llm);
        forumsAdapter = new ForumsAdapter(getActivity(), new ArrayList<AoThread>(), -1);
        rvThreadByUser.setAdapter(forumsAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvThreadByUser.setVisibility(View.GONE);
        isLoading = true;
        forumsHelper.GetUserThreads(user, 0, ForumsHelper.THREADS_FETCH_LIMIT);
        fetchCount= 1;
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshForums.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadThreads();
            }
        });

        rvThreadByUser.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Threads by " + user.getString(ParseUserColumns.AOZORA_USERNAME));
    }

    private void reloadThreads() {

        if(!isLoading) {
            isLoading = true;
            fetchCount = 0;
            lstThreads.clear();
            forumsAdapter.notifyDataSetChanged();
            forumsHelper.GetUserThreads(user, 0, ForumsHelper.THREADS_FETCH_LIMIT);
            fetchCount++;
        }
    }


    private void scrolledToEnd() {
        if(!isLoading) {
            isLoading = true;
            lstThreads.add(new AoThread());
            rvThreadByUser.post(new Runnable() {
                @Override
                public void run() {
                    forumsAdapter.notifyItemInserted(lstThreads.size());
                }
            });
            forumsHelper.GetUserThreads(user, fetchCount * ForumsHelper.THREADS_FETCH_LIMIT, ForumsHelper.THREADS_FETCH_LIMIT);
            fetchCount++;
        }
    }

    @Override
    public void onGetUserThreads(List<AoThread> threads) {

        if (fetchCount == 1) {
            lstThreads.addAll(threads);
            forumsAdapter = new ForumsAdapter(getActivity(),lstThreads,-1);
            rvThreadByUser.setAdapter(forumsAdapter);
            pbLoading.setVisibility(View.GONE);
            rvThreadByUser.setVisibility(View.VISIBLE);
            swipeRefreshForums.setRefreshing(false);
        } else {
            swipeRefreshForums.setRefreshing(false);
            if(lstThreads.size()>0) {
                lstThreads.remove(lstThreads.size() - 1);
                forumsAdapter.notifyItemRemoved(lstThreads.size());
            }
            if(threads.size()>0) {
                int currentPosition = lstThreads.size();
                lstThreads.addAll(threads);
                forumsAdapter.notifyItemRangeInserted(currentPosition, lstThreads.size()-currentPosition);
            }

        }
        isLoading = false;
    }
}
