package com.everfox.aozoraforums.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.models.TimelinePost;

import java.sql.Time;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimelinePostActivity extends AppCompatActivity implements PostParseHelper.OnGetTimelinePostCommentsListener {

    TimelinePost parentPost;
    LinearLayoutManager llm;

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
        llm = new LinearLayoutManager(TimelinePostActivity.this);
        rvPostComments.setLayoutManager(llm);
    }

    @Override
    protected void onStart() {
        super.onStart();

        new PostParseHelper(this,this)
                .GetTimelinePostComments(parentPost,0,2000);
    }

    @Override
    public void onTimelinePostComments(List<TimelinePost> timelinePosts) {
        timelinePosts.add(0,parentPost);

    }
}
