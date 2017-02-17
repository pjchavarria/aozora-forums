package com.everfox.aozoraforums.activities;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
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
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.dialogfragments.SimpleLoadingDialogFragment;
import com.everfox.aozoraforums.fragments.FollowersFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.fragments.UserListFragment;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.everfox.aozoraforums.utils.RecyclerItemClickListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimelinePostActivity extends AozoraActivity implements PostParseHelper.OnGetTimelinePostCommentsListener, TimelinePostsAdapter.OnUsernameTappedListener,
TimelinePostsAdapter.OnMoreOptionsTappedListener, OptionListDialogFragment.OnListSelectedListener, PostUtils.OnDeletePostCallback, TimelinePostsAdapter.OnLikeTappedListener, TimelinePostsAdapter.OnRepostTappedListener{

    SimpleLoadingDialogFragment simpleLoadingDialogFragment = new SimpleLoadingDialogFragment();
    public static String EXTRA_TIMELINEPOST_ID = "TimelinePostID";
    ArrayList<TimelinePost> allComments = new ArrayList<>();

    TimelinePost parentPostDelete = null;
    TimelinePost parentPost;
    LinearLayoutManager llm;
    TimelinePostsAdapter postsAdapter;
    Boolean isLoading = false;
    ParseUser userOP;
    Boolean actionTapped = false;

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
    @BindView(R.id.flContent)
    FrameLayout flContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_post);
        ButterKnife.bind(this);
        parentPost = AozoraForumsApp.getTimelinePostToPass();
        if(parentPost != null)
            userOP = AoUtils.GetOriginalPoster(parentPost);
        llm = new LinearLayoutManager(TimelinePostActivity.this);
        rvPostComments.setLayoutManager(llm);
        postsAdapter = new TimelinePostsAdapter(TimelinePostActivity.this,new ArrayList<TimelinePost>(),TimelinePostActivity.this, ParseUser.getCurrentUser());
        rvPostComments.setAdapter(postsAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        rvPostComments.setVisibility(View.GONE);
        swipeRefreshPost.setVisibility(View.GONE);
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
                        userOP = AoUtils.GetOriginalPoster(parentPost);
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
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
                if (currentFragment != null && currentFragment instanceof ProfileFragment || currentFragment instanceof FollowersFragment  || currentFragment instanceof UserListFragment) {
                    currentFragment.onResume();
                }
            }
        });

        rvPostComments.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                if(position > 0) {
                                    if (!actionTapped) {
                                        mOnCommentTapped(allComments.get(position),position);
                                    }
                                    actionTapped = false;
                                }
                            }
                        };
                        android.os.Handler handler = new android.os.Handler();
                        handler.postDelayed(runnable,100);
                    }
                }));
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

        parentPost.setReplies(timelinePosts);
        swipeRefreshPost.setRefreshing(false);
        timelinePosts.add(0,parentPost);
        allComments.addAll(timelinePosts);
        postsAdapter = new TimelinePostsAdapter(TimelinePostActivity.this, allComments, TimelinePostActivity.this, ParseUser.getCurrentUser());
        rvPostComments.setAdapter(postsAdapter);
        pbLoading.setVisibility(View.GONE);
        swipeRefreshPost.setVisibility(View.VISIBLE);
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

        actionTapped = true;
        if(!AoUtils.isActivityInvalid(TimelinePostActivity.this)) {
            ProfileFragment profileFragment = null;
            if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null,true);
            else
                profileFragment = ProfileFragment.newInstance(userTapped, true, false,null,true);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flContent, profileFragment).addToBackStack(null).commitAllowingStateLoss();

        }
    }

    TimelinePost selectedPost = null;
    Boolean parentPostTapped = false;

    @Override
    public void onMoreOptionsTappedCallback(TimelinePost post) {

        selectedPost = post;
        parentPostTapped = true;
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(userOP,this,null,this);
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(),"");
    }

    @Override
    public void onListSelected(Integer list, Integer selectedList) {

        List<String> optionList = AoUtils.getOptionListFromID(this,selectedList);
        String selectedOption = optionList.get(list);

        if(selectedList == AoConstants.EDITDELETE_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.ADMIN_POST_DELETE:
                    new AlertDialog.Builder(this)
                            .setTitle("Delete this post forever?")
                            .setMessage("You can't undo this")
                            .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                            .setPositiveButton(R.string.dialog_delete_post, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    simpleLoadingDialogFragment.show(getSupportFragmentManager(),"loading");
                                    new PostParseHelper(TimelinePostActivity.this,TimelinePostActivity.this).deletePost(selectedPost,parentPostDelete);
                                    parentPostDelete = null;
                                }
                            })
                            .create()
                            .show();
                    break;
                case AoConstants.POST_EDIT:
                    Toast.makeText(this,"Edit Admin", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        if(selectedList == AoConstants.REPORT_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.REPORT_POST_REPORT_CONTENT:
                    AoUtils.reportObject(selectedPost);
                    AoUtils.showAlertWithTitleAndText(this,"Report Sent!","The report will be reviewed by an admin and deleted/updated if needed.");
                    break;
            }
        }
    }

    public static int PARENT_POST_DELETED = 400;
    int selectedPosition = -1;


    @Override
    public void onDeletePost() {
        if (simpleLoadingDialogFragment.getDialog().isShowing())
            simpleLoadingDialogFragment.dismissAllowingStateLoss();
        if(parentPostTapped) {
            setResult(PARENT_POST_DELETED);
            finish();
            parentPostTapped = false;
        } else {
            allComments.remove(selectedPosition);
            postsAdapter.notifyItemRemoved(selectedPosition);
        }
    }

    public void mOnCommentTapped(TimelinePost post, int position) {
        parentPostDelete = allComments.get(0);
        selectedPost = AoUtils.GetOriginalPost(post);
        selectedPosition = position;
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(AoUtils.GetOriginalPoster(post), TimelinePostActivity.this, null, TimelinePostActivity.this);
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void onLikeTappedListener(TimelinePost post, int position) {

        actionTapped = true;
        ParseObject newPost = PostUtils.likePost(post);
        allComments.set(position,(TimelinePost)newPost);
        postsAdapter.notifyItemChanged(position);
    }

    @Override
    public void onRepostTappedListener(TimelinePost post, int position) {

        ArrayList<ParseObject> repost = PostUtils.repostPost(post);
        allComments.set(position,(TimelinePost)repost.get(0));
        postsAdapter.notifyItemChanged(position);
    }
}
