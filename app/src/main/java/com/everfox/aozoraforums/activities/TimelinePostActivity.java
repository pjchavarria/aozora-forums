package com.everfox.aozoraforums.activities;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.ProfileTimelineAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimelinePostActivity extends AppCompatActivity implements PostParseHelper.OnGetTimelinePostCommentsListener, TimelinePostsAdapter.OnUsernameTappedListener {

    TimelinePost parentPost;
    LinearLayoutManager llm;
    TimelinePostsAdapter postsAdapter;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvPostComments)
    RecyclerView rvPostComments;
    @BindView(R.id.llAddComment)
    LinearLayout llAddComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_post);
        ButterKnife.bind(this);
        parentPost = AozoraForumsApp.getTimelinePostToPass();
        llm = new LinearLayoutManager(TimelinePostActivity.this);
        rvPostComments.setLayoutManager(llm);
        postsAdapter = new TimelinePostsAdapter(TimelinePostActivity.this,new ArrayList<TimelinePost>(),TimelinePostActivity.this, ParseUser.getCurrentUser());
        rvPostComments.setAdapter(postsAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvPostComments.setVisibility(View.GONE);
        new PostParseHelper(this,this)
                .GetTimelinePostComments(parentPost,0,2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(parentPost.getParseObject(TimelinePost.POSTED_BY).getString(ParseUserColumns.AOZORA_USERNAME));
    }

    @Override
    public void onTimelinePostComments(List<TimelinePost> timelinePosts) {

        if(postsAdapter.getItemCount() == 0) {
            timelinePosts.add(0,parentPost);
            postsAdapter = new TimelinePostsAdapter(TimelinePostActivity.this, timelinePosts, TimelinePostActivity.this, ParseUser.getCurrentUser());
            rvPostComments.setAdapter(postsAdapter);
            pbLoading.setVisibility(View.GONE);
            rvPostComments.setVisibility(View.VISIBLE);
            RecyclerView.RecycledViewPool recycledViewPool = rvPostComments.getRecycledViewPool();
            recycledViewPool.setMaxRecycledViews(TimelinePostsAdapter.ITEM_FIRST_POST,0);
            recycledViewPool.setMaxRecycledViews(TimelinePostsAdapter.ITEM_COMMENT,0);
            rvPostComments.setRecycledViewPool(recycledViewPool);
            rvPostComments.setItemViewCacheSize(200);
        }
    }

    @Override
    public void onUsernameTapped(ParseUser userTapped) {

        if(!AoUtils.isActivityInvalid(TimelinePostActivity.this)) {
            //Abrimos Main Activity y le mandamos usuario
            AozoraForumsApp.setProfileToPass(userTapped);
            Toast.makeText(this,userTapped.getString(ParseUserColumns.AOZORA_USERNAME),Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
        }
    }
}
