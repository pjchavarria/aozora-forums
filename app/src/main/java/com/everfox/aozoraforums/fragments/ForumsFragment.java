package com.everfox.aozoraforums.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.FirstActivity;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.adapters.ForumsAdapter;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class ForumsFragment extends Fragment implements ForumsHelper.OnGetGlobalThreadsListener, ForumsHelper.OnGetThreadsListener,
OptionListDialogFragment.OnListSelectedListener{


    ForumsAdapter forumsAdapter;
    Boolean isLoading = false;
    int fetchCount = 0;
    Boolean fetchingGlobalThreads = false;
    String selectedList = "aoArt";
    int selectedViewType = ForumsAdapter.VIEW_AOART;
    String selectedSort = AoConstants.POPULAR;
    LinearLayoutManager llm;
    ForumsHelper forumsHelper;
    ArrayList<AoThread> lstThreads = new ArrayList<>();
    Boolean hasMenu = true;


    @BindView(R.id.rlAoArt)
    RelativeLayout rlAoArt;
    @BindView(R.id.rlAoNews)
    RelativeLayout rlAoNews;
    @BindView(R.id.rlAoGur)
    RelativeLayout rlAoGur;
    @BindView(R.id.rlAoTalk)
    RelativeLayout rlAoTalk;
    @BindView(R.id.rlOfficial)
    RelativeLayout rlOfficial;
    @BindView(R.id.vAoArt)
    View vAoArt;
    @BindView(R.id.vAoNews)
    View vAoNews;
    @BindView(R.id.vAoGur)
    View vAoGur;
    @BindView(R.id.vAoTalk)
    View vAoTalk;
    @BindView(R.id.vOffical)
    View vOffical;

    @BindView(R.id.rvForums)
    RecyclerView rvForums;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.swipeRefreshForums)
    SwipeRefreshLayout swipeRefreshForums;


    public static ForumsFragment newInstance() {
        ForumsFragment fragment = new ForumsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_forums, container, false);
        ButterKnife.bind(this,view);
        setHasOptionsMenu(true);
        forumsHelper = new ForumsHelper(getActivity(),this);
        if(AozoraForumsApp.getGlobalThreads().size()==0) {
            forumsHelper.GetGlobalThreads();
            fetchingGlobalThreads = true;
        } else {
            lstThreads.addAll(AozoraForumsApp.getGlobalThreads());
        }
        llm = new LinearLayoutManager(getActivity());
        rvForums.setLayoutManager(llm);
        forumsAdapter = new ForumsAdapter(getActivity(),new ArrayList<AoThread>(),selectedViewType);
        rvForums.setAdapter(forumsAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvForums.setVisibility(View.GONE);
        isLoading = true;
        fetchCount++;
        forumsHelper.GetThreads(selectedList,selectedSort,0,ForumsHelper.THREADS_FETCH_LIMIT);

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

        rvForums.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        rlAoArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoading) {
                    selectedViewType = ForumsAdapter.VIEW_AOART;
                    forumTabSelected(vAoArt,AoConstants.AOART);
                }
            }
        });
        rlAoGur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoading) {
                    selectedViewType = ForumsAdapter.VIEW_AOGUR;
                    forumTabSelected(vAoGur,AoConstants.AOGUR);
                }
            }
        });
        rlAoNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoading) {
                    selectedViewType = ForumsAdapter.VIEW_AONEWS;
                    forumTabSelected(vAoNews,AoConstants.AONEWS);
                }
            }
        });
        rlAoTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoading) {
                    selectedViewType = ForumsAdapter.VIEW_AOTALK;
                    forumTabSelected(vAoTalk,AoConstants.AOTALK);
                }
            }
        });
        rlOfficial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isLoading) {
                    selectedViewType = ForumsAdapter.VIEW_AOOFFICIAL;
                    forumTabSelected(vOffical,AoConstants.OFFICIAL);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Aozora Threads");
    }

    private void scrolledToEnd() {
        if(!isLoading) {
            isLoading = true;
            lstThreads.add(new AoThread());
            rvForums.post(new Runnable() {
                @Override
                public void run() {
                    forumsAdapter.notifyItemInserted(lstThreads.size());
                }
            });
            forumsHelper.GetThreads(selectedList, selectedSort, fetchCount * ForumsHelper.THREADS_FETCH_LIMIT, ForumsHelper.THREADS_FETCH_LIMIT);
            fetchCount++;
        }
    }

    private void reloadThreads() {
        if(!isLoading) {
            isLoading = true;
            fetchCount = 0;
            lstThreads.clear();
            lstThreads.addAll(AozoraForumsApp.getGlobalThreads());
            forumsAdapter.notifyDataSetChanged();
            forumsHelper.GetThreads(selectedList, selectedSort, 0, ForumsHelper.THREADS_FETCH_LIMIT);
            fetchCount++;
        }

    }

    public void scrollThreadsToStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rvForums.scrollToPosition(0);
            }
        }, 200);
    }

    @Override
    public void onGetGlobalThreads() {

        lstThreads.addAll(0,AozoraForumsApp.getGlobalThreads());
        forumsAdapter.notifyItemRangeInserted(0,AozoraForumsApp.getGlobalThreads().size());
        fetchingGlobalThreads = false;
    }

    @Override
    public void onGetThreads(List<AoThread> threads) {

        if (fetchCount == 1) {
            lstThreads.addAll(threads);
            forumsAdapter = new ForumsAdapter(getActivity(),lstThreads,selectedViewType);
            rvForums.setAdapter(forumsAdapter);
            pbLoading.setVisibility(View.GONE);
            rvForums.setVisibility(View.VISIBLE);
            swipeRefreshForums.setRefreshing(false);
        } else {
            swipeRefreshForums.setRefreshing(false);
            lstThreads.remove(lstThreads.size()-1);
            forumsAdapter.notifyItemRemoved(lstThreads.size());
            if(threads.size()>0) {
                int currentPosition = lstThreads.size();
                lstThreads.addAll(threads);
                forumsAdapter.notifyItemRangeInserted(currentPosition, lstThreads.size()-currentPosition);
            }

        }
        isLoading = false;
    }

    private void forumTabSelected(View forumMark,String forum) {
        if (!selectedList.equals(forum)) {
            HideAllMarks();
            forumMark.setVisibility(View.VISIBLE);
            selectedList = forum;
            reloadThreads();
        } else {
            scrollThreadsToStart();
        }
    }

    private void HideAllMarks() {
        vAoArt.setVisibility(View.INVISIBLE);
        vAoNews.setVisibility(View.INVISIBLE);
        vAoGur.setVisibility(View.INVISIBLE);
        vAoTalk.setVisibility(View.INVISIBLE);
        vOffical.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.forums_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.sort:
                OptionListDialogFragment optionListDialogFragment = OptionListDialogFragment.newInstance(getActivity(),"Current Sort: " + selectedSort,null,null,this,AoConstants.SORT_OPTIONS_DIALOG);
                optionListDialogFragment.setCancelable(true);
                optionListDialogFragment.show(getFragmentManager(),"");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onListSelected(Integer list, Integer selectedList) {
        List<String> optionList = AoUtils.getOptionListFromID(getActivity(),selectedList);
        String selectedOption = optionList.get(list);
        selectedSort = selectedOption;
        reloadThreads();
    }
}
