package com.everfox.aozoraforums.activities;

import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.AoThreadAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.ThreadHelper;
import com.everfox.aozoraforums.fragments.FollowersFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadActivity extends AppCompatActivity implements AoThreadAdapter.OnUsernameTappedListener, ThreadHelper.OnGetThreadCommentsListener,
AoThreadAdapter.OnCommentTappedListener{

    AoThread parentThread;
    LinearLayoutManager llm;
    AoThreadAdapter aoThreadAdapter;
    Boolean isLoading = false;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvComments)
    RecyclerView rvThreadComments;
    @BindView(R.id.llAddComment)
    LinearLayout llAddComment;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.rlContent)
    RelativeLayout rlContent;
    @BindView(R.id.flNewFragments)
    FrameLayout flNewFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_post);
        ButterKnife.bind(this);
        parentThread = AozoraForumsApp.getThreadToPass();
        llm = new LinearLayoutManager(this);
        rvThreadComments.setLayoutManager(llm);
        aoThreadAdapter = new AoThreadAdapter(this,new ArrayList<ParseObject>(),this);
        rvThreadComments.setAdapter(aoThreadAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isLoading)
                    reloadThread();
            }
        });
        new ThreadHelper(this,this).GetThreadComments(parentThread,0,2000);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flNewFragments);
                if (currentFragment != null && currentFragment instanceof ProfileFragment || currentFragment instanceof FollowersFragment) {
                    currentFragment.onResume();
                }
            }
        });
        isLoading = true;
    }

    private void reloadThread() {
        isLoading = true;
        new ThreadHelper(this,this).GetThreadComments(parentThread,0,2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(parentThread != null)
            setTitle(parentThread.getString(AoThread.TITLE));

        //TEST
        ArrayList<ParseObject> lstItems = new ArrayList<>();
        lstItems.add(parentThread);
        onGetThreadComments(lstItems);
    }


    @Override
    public void onGetThreadComments(List<ParseObject> comments) {

        swipeRefresh.setRefreshing(false);
        comments.add(0,parentThread);
        aoThreadAdapter = new AoThreadAdapter(this,comments,this);
        rvThreadComments.setAdapter(aoThreadAdapter);
        pbLoading.setVisibility(View.GONE);
        rvThreadComments.setVisibility(View.VISIBLE);
        if(aoThreadAdapter.getItemCount() == 0){
            RecyclerView.RecycledViewPool recycledViewPool = rvThreadComments.getRecycledViewPool();
            recycledViewPool.setMaxRecycledViews(TimelinePostsAdapter.ITEM_FIRST_POST,0);
            recycledViewPool.setMaxRecycledViews(TimelinePostsAdapter.ITEM_COMMENT,0);
            rvThreadComments.setRecycledViewPool(recycledViewPool);
            rvThreadComments.setItemViewCacheSize(200);
        }
        isLoading = false;
    }


    @Override
    public void onUsernameTapped(ParseUser userTapped) {
        if(!AoUtils.isActivityInvalid(this)) {
            ProfileFragment profileFragment = null;
            if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null);
            else
                profileFragment = ProfileFragment.newInstance(userTapped, true, false,null);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flNewFragments, profileFragment).addToBackStack(null).commitAllowingStateLoss();

        }
    }

    @Override
    public void onCommentTapped(ParseObject commentTapped) {

    }
}
