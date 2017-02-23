package com.everfox.aozoraforums.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.FirstActivity;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.activities.ThreadActivity;
import com.everfox.aozoraforums.adapters.ForumsAdapter;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.dialogfragments.SimpleLoadingDialogFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.everfox.aozoraforums.utils.ThreadUtils;
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
OptionListDialogFragment.OnListSelectedListener, ForumsAdapter.OnGlobalThreadHideListener, ForumsAdapter.OnUpDownVoteListener,
ForumsAdapter.OnItemLongClickListener, ForumsHelper.OnBanDeletePostCallback, ForumsAdapter.OnImageShareListener{


    private static final int REQUEST_WRITE_STORAGE = 100;
    View viewToShare;
    SimpleLoadingDialogFragment simpleLoadingDialogFragment = new SimpleLoadingDialogFragment();
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
        forumsHelper = new ForumsHelper(getActivity(),this,null);
        forumsHelper.GetGlobalThreads();
        fetchingGlobalThreads = true;
        llm = new LinearLayoutManager(getActivity());
        rvForums.setLayoutManager(llm);
        forumsAdapter = new ForumsAdapter(getActivity(),new ArrayList<AoThread>(),selectedViewType,this);
        rvForums.setAdapter(forumsAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvForums.setVisibility(View.GONE);
        isLoading = true;
        fetchCount = 1;
        lstThreads.clear();
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

        rvForums.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
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

        HideAllMarks();
        if(selectedViewType == ForumsAdapter.VIEW_AOART) {
            vAoArt.setVisibility(View.VISIBLE);
        } else if(selectedViewType == ForumsAdapter.VIEW_AOGUR) {
            vAoGur.setVisibility(View.VISIBLE);
        } else if(selectedViewType == ForumsAdapter.VIEW_AONEWS) {
            vAoNews.setVisibility(View.VISIBLE);
        } else if(selectedViewType == ForumsAdapter.VIEW_AOTALK) {
            vAoTalk.setVisibility(View.VISIBLE);
        } else if(selectedViewType == ForumsAdapter.VIEW_AOOFFICIAL) {
            vOffical.setVisibility(View.VISIBLE);
        }
    }

    public void setSelectedThread (AoThread thread) {
        selectedThread = thread;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Aozora Threads");
        if(selectedThread != null) {
            updateThread();
        }

    }

    private void updateThread() {
        int position = lstThreads.indexOf(selectedThread);
        lstThreads.set(position,selectedThread);
        forumsAdapter.notifyItemChanged(position, selectedThread);
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

        lstThreads.clear();
        lstThreads.addAll(0,AozoraForumsApp.getGlobalThreads());
        forumsAdapter.notifyItemRangeInserted(0,AozoraForumsApp.getGlobalThreads().size());
        fetchingGlobalThreads = false;
    }

    @Override
    public void onGetThreads(final List<AoThread> threads) {

        if(fetchingGlobalThreads) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadForumThreads(threads);
                }
            }, 150);
        } else {
            loadForumThreads(threads);
        }
    }

    private void loadForumThreads(List<AoThread> threads) {

        pbLoading.setVisibility(View.GONE);
        rvForums.setVisibility(View.VISIBLE);
        if (fetchCount == 1) {
            lstThreads.addAll(threads);
            forumsAdapter = new ForumsAdapter(getActivity(),lstThreads,selectedViewType,this);
            rvForums.setAdapter(forumsAdapter);
            pbLoading.setVisibility(View.GONE);
            rvForums.setVisibility(View.VISIBLE);
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
                OptionListDialogFragment optionListDialogFragment = OptionListDialogFragment.newInstance(getActivity(),"Current Sort: " + selectedSort,null,null,this,AoConstants.SORT_OPTIONS_DIALOG,null);
                optionListDialogFragment.setCancelable(true);
                optionListDialogFragment.show(getFragmentManager(),"");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onListSelected(Integer list, Integer selectedList) {
        List<String> optionList = AoUtils.getOptionListFromID(getActivity(), selectedList);
        String selectedOption = optionList.get(list);
        if(selectedList == AoConstants.SORT_OPTIONS_DIALOG) {
            selectedSort = selectedOption;
            reloadThreads();
        } else {
            if(selectedList == AoConstants.REPORT_POST_OPTIONS_DIALOG) {
                switch (selectedOption){
                    case AoConstants.REPORT_POST_REPORT_CONTENT:
                        AoUtils.reportObject(selectedThread);
                        AoUtils.showAlertWithTitleAndText(getActivity(),"Report Sent!","The report will be reviewed by an admin and deleted/updated if needed.");
                        break;
                }
            }
            else if(selectedList == AoConstants.EDITDELETE_THREAD_OPTIONS_DIALOG || selectedList == AoConstants.EDITDELETE_POST_OPTIONS_DIALOG) {
                switch (selectedOption){
                    case AoConstants.ADMIN_POST_DELETE:
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Delete this thread forever?")
                                .setMessage("You can't undo this")
                                .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                                .setPositiveButton(R.string.dialog_delete_post, new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        new ForumsHelper(getActivity(),ForumsFragment.this,null).deleteThread(selectedThread);
                                    }
                                })
                                .create()
                                .show();
                        break;
                    case AoConstants.POST_EDIT:
                        Toast.makeText(getActivity(),"Edit Admin", Toast.LENGTH_SHORT).show();
                        break;
                }
            }   else if(selectedList == AoConstants.EDITBAN_THREAD_OPTIONS_DIALOG) {
                switch (selectedOption){
                    case AoConstants.ADMIN_THREAD_BAN:
                        new AlertDialog.Builder(getActivity())
                                .setTitle("Ban this thread?")
                                .setMessage("Are you sure?")
                                .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                                .setPositiveButton(R.string.dialog_delete_post, new DialogInterface.OnClickListener() {
                                    @Override public void onClick(DialogInterface dialog, int which) {
                                        new ForumsHelper(getActivity(),ForumsFragment.this,null).banThread(selectedThread);
                                    }
                                })
                                .create()
                                .show();
                        break;
                    case AoConstants.POST_EDIT:
                        Toast.makeText(getActivity(),"Edit Admin", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }
    }

    @Override
    public void onDeleteOrBan() {
        lstThreads.remove(selectedPosition);
        forumsAdapter.notifyItemRemoved(selectedPosition);
    }

    @Override
    public void onGlobalThreadHide(AoThread threadToHide) {
        List<AoThread> lst = AozoraForumsApp.getHiddenGlobalThreads();
        lst.add(threadToHide);
        AozoraForumsApp.setHiddenGlobalThreads(lst);
        int position = lstThreads.indexOf(threadToHide);
        lstThreads.remove(position);
        forumsAdapter.notifyItemRemoved(position);
    }

    @Override
    public void onUpDownVote(Boolean upvote, AoThread thread) {
        ParseObject newPost;
        if(upvote)
            newPost = PostUtils.likePost(thread);
        else
            newPost = PostUtils.unlikeThread(thread);
        int position = lstThreads.indexOf(newPost);
        lstThreads.set(position,(AoThread) newPost);
        forumsAdapter.notifyItemChanged(position,newPost);
    }


    AoThread selectedThread= null;
    int selectedPosition = -1;

    @Override
    public void onItemLongClicked(AoThread aoThread) {
        selectedThread = aoThread;
        this.selectedPosition = lstThreads.indexOf(aoThread);
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(aoThread.getParseUser(TimelinePost.POSTED_BY),getActivity(),this,null,true);
        fragment.setCancelable(true);
        fragment.show(getFragmentManager(),"");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 400) {
            if(resultCode == ThreadActivity.PARENT_POST_DELETED) {
                AoThread thread = AozoraForumsApp.getThreadToPass();
                int index = lstThreads.indexOf(thread);
                lstThreads.remove(index);
                forumsAdapter.notifyItemRemoved(index);

            }
        }
    }


    @Override
    public void mShareCallback(View view) {

        boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            viewToShare = view;
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            AoUtils.ShareImageFromView(view, getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AoUtils.ShareImageFromView(viewToShare, getActivity());
                } else {
                    Toast.makeText(getActivity(), "The app was not allowed to write to your storage. Hence, it cannot share the image. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
