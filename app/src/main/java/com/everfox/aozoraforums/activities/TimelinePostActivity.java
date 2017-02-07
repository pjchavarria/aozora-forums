package com.everfox.aozoraforums.activities;

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
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.fragments.FollowersFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimelinePostActivity extends AppCompatActivity implements PostParseHelper.OnGetTimelinePostCommentsListener, TimelinePostsAdapter.OnUsernameTappedListener {

    public static String EXTRA_TIMELINEPOST_ID = "TimelinePostID";

    TimelinePost parentPost;
    LinearLayoutManager llm;
    TimelinePostsAdapter postsAdapter;
    Boolean isLoading = false;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvComments)
    RecyclerView rvPostComments;
    @BindView(R.id.llAddComment)
    LinearLayout llAddComment;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshPost;
    @BindView(R.id.rlContent)
    RelativeLayout rlContent;
    @BindView(R.id.flNewFragments)
    FrameLayout flNewFragments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_post);
        ButterKnife.bind(this);
        parentPost = AozoraForumsApp.getTimelinePostToPass();
        llm = new LinearLayoutManager(TimelinePostActivity.this);
        rvPostComments.setLayoutManager(llm);
        postsAdapter = new TimelinePostsAdapter(TimelinePostActivity.this,new ArrayList<TimelinePost>(),TimelinePostActivity.this, ParseUser.getCurrentUser());
        rvPostComments.setAdapter(postsAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvPostComments.setVisibility(View.GONE);
        swipeRefreshPost.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isLoading)
                    reloadPosts();
            }
        });

        if(getIntent().hasExtra(EXTRA_TIMELINEPOST_ID)) {
            String timelinePostID = getIntent().getStringExtra(EXTRA_TIMELINEPOST_ID);
            ParseQuery<TimelinePost> postParseQuery = ParseQuery.getQuery(TimelinePost.class);
            postParseQuery.whereEqualTo(TimelinePost.OBJECT_ID,timelinePostID);
            postParseQuery.getFirstInBackground(new GetCallback<TimelinePost>() {
                @Override
                public void done(TimelinePost object, ParseException e) {
                    if(object != null && e==null) {
                        parentPost = object;
                        setTitle(parentPost.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME));
                        new PostParseHelper(TimelinePostActivity.this, TimelinePostActivity.this)
                                .GetTimelinePostComments(parentPost, 0, 2000);
                    } else {
                        Toast.makeText(TimelinePostActivity.this,"A problem occured, try again later",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } else {

            new PostParseHelper(this, this)
                    .GetTimelinePostComments(parentPost, 0, 2000);
        }

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flNewFragments);
                if (currentFragment != null && currentFragment instanceof ProfileFragment  || currentFragment instanceof FollowersFragment) {
                    currentFragment.onResume();
                }
            }
        });

        isLoading = true;
    }

    private void reloadPosts() {

        isLoading = true;
        new PostParseHelper(this,this)
                .GetTimelinePostComments(parentPost,0,2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(parentPost != null)
            setTitle(parentPost.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME));
    }

    @Override
    public void onTimelinePostComments(List<TimelinePost> timelinePosts) {

        swipeRefreshPost.setRefreshing(false);
        timelinePosts.add(0,parentPost);
        postsAdapter = new TimelinePostsAdapter(TimelinePostActivity.this, timelinePosts, TimelinePostActivity.this, ParseUser.getCurrentUser());
        rvPostComments.setAdapter(postsAdapter);
        pbLoading.setVisibility(View.GONE);
        rvPostComments.setVisibility(View.VISIBLE);
        if(postsAdapter.getItemCount() == 0) {
            RecyclerView.RecycledViewPool recycledViewPool = rvPostComments.getRecycledViewPool();
            recycledViewPool.setMaxRecycledViews(TimelinePostsAdapter.ITEM_FIRST_POST,0);
            recycledViewPool.setMaxRecycledViews(TimelinePostsAdapter.ITEM_COMMENT,0);
            rvPostComments.setRecycledViewPool(recycledViewPool);
            rvPostComments.setItemViewCacheSize(200);
        }
        isLoading = false;
    }

    @Override
    public void onUsernameTapped(ParseUser userTapped) {

        if(!AoUtils.isActivityInvalid(TimelinePostActivity.this)) {
            ProfileFragment profileFragment = null;
            if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null,true);
            else
                profileFragment = ProfileFragment.newInstance(userTapped, true, false,null,true);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flNewFragments, profileFragment).addToBackStack(null).commitAllowingStateLoss();

        }
    }
}
