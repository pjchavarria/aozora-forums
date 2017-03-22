package com.everfox.aozoraforums.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.ThreadActivity;
import com.everfox.aozoraforums.activities.postthread.CreatePostActivity;
import com.everfox.aozoraforums.adapters.ForumsAdapter;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/9/2017.
 */

public class ThreadByUserFragment extends Fragment implements ForumsHelper.OnGetUserThreadsListener,  ForumsAdapter.OnUpDownVoteListener, ForumsAdapter.OnItemLongClickListener,
        OptionListDialogFragment.OnListSelectedListener, ForumsHelper.OnBanDeletePostCallback, ForumsAdapter.OnImageShareListener {

    private static final int REQUEST_EDIT_AOTHREAD = 501;
    private static final int REQUEST_WRITE_STORAGE = 100;
    View viewToShare;
    ParseUser user;
    AoLinearLayoutManager llm;
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
    @BindView(R.id.llEmptyMessage)
    LinearLayout llEmptyMessage;

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
        forumsHelper = new ForumsHelper(getActivity(), this,null);
        llm = new AoLinearLayoutManager(getActivity());
        rvThreadByUser.setLayoutManager(llm);
        forumsAdapter = new ForumsAdapter(getActivity(), new ArrayList<AoThread>(), -1, this);
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
        if(selectedThread != null) {
            updateThread();
        }
    }


    private void updateThread() {
        int position = lstThreads.indexOf(selectedThread);
        if(position != -1) {
            lstThreads.set(position, selectedThread);
            forumsAdapter.notifyItemChanged(position, selectedThread);
        }
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

        if(threads.size() == 0)
            fetchCount--;
        pbLoading.setVisibility(View.GONE);
        if(fetchCount == 1 && threads.size() == 0 && lstThreads.size() == 0) {
            rvThreadByUser.setVisibility(View.GONE);
            llEmptyMessage.setVisibility(View.VISIBLE);
        } else {
            llEmptyMessage.setVisibility(View.GONE);
            rvThreadByUser.setVisibility(View.VISIBLE);
            if (fetchCount == 1 && threads.size() != 0) {
                lstThreads.addAll(threads);
                forumsAdapter = new ForumsAdapter(getActivity(), lstThreads, -1, this);
                rvThreadByUser.setAdapter(forumsAdapter);
                swipeRefreshForums.setRefreshing(false);
            } else {
                swipeRefreshForums.setRefreshing(false);
                if (lstThreads.size() > 0) {
                    lstThreads.remove(lstThreads.size() - 1);
                    forumsAdapter.notifyItemRemoved(lstThreads.size());
                }
                if (threads.size() > 0) {
                    int currentPosition = lstThreads.size();
                    lstThreads.addAll(threads);
                    forumsAdapter.notifyItemRangeInserted(currentPosition, lstThreads.size() - currentPosition);
                }

            }
        }
        isLoading = false;
    }

    @Override
    public void onUpDownVote(Boolean upvote, AoThread thread) {
        ParseObject newPost;
        int position = lstThreads.indexOf(thread);
        if(upvote)
            newPost = PostUtils.likePost(thread);
        else
            newPost = PostUtils.unlikeThread(thread);
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
        }  else if (requestCode == REQUEST_EDIT_AOTHREAD && resultCode == getActivity().RESULT_OK) {
            AoThread thread = (AoThread)AozoraForumsApp.getUpdatedPost();
            int position =  lstThreads.indexOf(thread);
            lstThreads.set(position, thread);
            forumsAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void onListSelected(Integer list, Integer selectedList) {

        List<String> optionList = AoUtils.getOptionListFromID(getActivity(), selectedList);
        String selectedOption = optionList.get(list);
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
                                    new ForumsHelper(getActivity(),ThreadByUserFragment.this,null).deleteThread(selectedThread);
                                }
                            })
                            .create()
                            .show();
                    break;
                case AoConstants.POST_EDIT:
                    editSelectedThread();
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
                                    new ForumsHelper(getActivity(),ThreadByUserFragment.this,null).banThread(selectedThread);
                                }
                            })
                            .create()
                            .show();
                    break;
                case AoConstants.POST_EDIT:
                    editSelectedThread();
                    break;
            }
        }
    }

    private void editSelectedThread() {

        Intent i = new Intent(getActivity(),CreatePostActivity.class);
        i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.EDIT_AOTHREAD);
        AozoraForumsApp.setPostedBy(selectedThread.getParseUser(TimelinePost.POSTED_BY));
        AozoraForumsApp.setPostedIn(null);
        AozoraForumsApp.setPostToUpdate(selectedThread);
        startActivityForResult(i,REQUEST_EDIT_AOTHREAD);
    }


    @Override
    public void onDeleteOrBan() {
        lstThreads.remove(selectedPosition);
        forumsAdapter.notifyItemRemoved(selectedPosition);
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

    public void setSelectedThread(AoThread aoThread) {
        this.selectedThread = aoThread;
    }
}
