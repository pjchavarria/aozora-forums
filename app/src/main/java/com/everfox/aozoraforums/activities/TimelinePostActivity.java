package com.everfox.aozoraforums.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.postthread.CreatePostActivity;
import com.everfox.aozoraforums.activities.postthread.SearchImageActivity;
import com.everfox.aozoraforums.activities.postthread.SearchYoutubeActivity;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.AddPostThreadHelper;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.dialogfragments.SimpleLoadingDialogFragment;
import com.everfox.aozoraforums.fragments.FollowersFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.fragments.UserListFragment;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.everfox.aozoraforums.utils.RecyclerItemClickListener;
import com.everfox.aozoraforums.utils.RecyclerItemLongClickListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimelinePostActivity extends AozoraActivity implements PostParseHelper.OnGetTimelinePostCommentsListener, TimelinePostsAdapter.OnUsernameTappedListener,
TimelinePostsAdapter.OnMoreOptionsTappedListener, OptionListDialogFragment.OnListSelectedListener, PostUtils.OnDeletePostCallback, TimelinePostsAdapter.OnLikeTappedListener, TimelinePostsAdapter.OnRepostTappedListener,
TimelinePostsAdapter.OnImageShareListener, TimelinePostsAdapter.OnCommentTappedListener, AddPostThreadHelper.OnPerformPost{

    private static final int REQUEST_EDIT_TIMELINEPOST_REPLY = 552;
    private static final int REQUEST_EDIT_TIMELINEPOST = 551;
    private static final int REQUEST_NEW_TIMELINEPOST_REPLY = 550;
    private static final int REQUEST_WRITE_STORAGE = 100;
    View viewToShare;
    SimpleLoadingDialogFragment simpleLoadingDialogFragment = new SimpleLoadingDialogFragment();
    public static String EXTRA_TIMELINEPOST_ID = "TimelinePostID";
    ArrayList<TimelinePost> allComments = new ArrayList<>();

    TimelinePost parentPostDelete = null;
    TimelinePost parentPost;
    AoLinearLayoutManager llm;
    TimelinePostsAdapter postsAdapter;
    Boolean isLoading = false;
    ParseUser userOP;
    Boolean actionTapped = false;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvComments)
    RecyclerView rvPostComments;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshPost;
    @BindView(R.id.rlContent)
    RelativeLayout rlContent;
    @BindView(R.id.flContent)
    FrameLayout flContent;
    @BindView(R.id.llAddCommentLayout)
    LinearLayout llAddCommentLayout;
    @BindView(R.id.llAddComment)
    LinearLayout llAddComment;
    @BindView(R.id.etComment)
    EditText etComment;
    @BindView(R.id.ivAddPhotoInternet)
    ImageView ivAddPhotoInternet;
    @BindView(R.id.ivAddPhotoGallery)
    ImageView ivAddPhotoGallery;
    @BindView(R.id.ivAddVideo)
    ImageView ivAddVideo;
    @BindView(R.id.btnSendComment)
    Button btnSendComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thread_post);
        ButterKnife.bind(this);
        parentPost = AozoraForumsApp.getTimelinePostToPass();
        if(parentPost != null)
            userOP = AoUtils.GetOriginalPoster(parentPost);
        llm = new AoLinearLayoutManager(TimelinePostActivity.this);
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
                        initAddCommentControls();
                        new PostParseHelper(TimelinePostActivity.this, TimelinePostActivity.this,null)
                                .GetTimelinePostComments(parentPost, 0, 2000);
                    } else {
                        Toast.makeText(TimelinePostActivity.this,"A problem occured, try again later",Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });
        } else {

            new PostParseHelper(this, this,null)
                    .GetTimelinePostComments(parentPost, 0, 2000);

            initAddCommentControls();
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

        rvPostComments.addOnItemTouchListener(new RecyclerItemLongClickListener(this, rvPostComments, new RecyclerItemLongClickListener.OnItemLongClickListener() {

            @Override
            public void onItemLongClick(View view, final int position) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(position > 0) {
                            if (!actionTapped) {
                                mOnCommentLongTapped(allComments.get(position),position);
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
        new PostParseHelper(this,this,null)
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

        parentPost.setReplies(new ArrayList<>(timelinePosts));
        swipeRefreshPost.setRefreshing(false);
        allComments.clear();
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

        selectedPosition = 0;
        selectedPost = post;
        parentPostTapped = true;
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(userOP,this,null,this,false);
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
                                    Toast.makeText(TimelinePostActivity.this,"Deleting...",Toast.LENGTH_SHORT).show();
                                    new PostParseHelper(TimelinePostActivity.this,TimelinePostActivity.this,null).deletePost(selectedPost,parentPostDelete);
                                    parentPostDelete = null;
                                }
                            })
                            .create()
                            .show();
                    break;
                case AoConstants.POST_EDIT:
                    editSelectedPost();
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

    private void editSelectedPost() {

        Intent i = new Intent(this,CreatePostActivity.class);
        AozoraForumsApp.setPostedBy(selectedPost.getParseUser(TimelinePost.POSTED_BY));
        AozoraForumsApp.setPostedIn(selectedPost.getParseUser(TimelinePost.USER_TIMELINE));
        AozoraForumsApp.setPostToUpdate(selectedPost);
        AozoraForumsApp.setUpdatedParentPost(parentPost);

        if(selectedPosition == 0) {
            i.putExtra(CreatePostActivity.PARAM_TYPE, CreatePostActivity.EDIT_TIMELINEPOST);
            startActivityForResult(i, REQUEST_EDIT_TIMELINEPOST);
        }
        else {
            i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.EDIT_TIMELINEPOST_REPLY);
            startActivityForResult(i, REQUEST_EDIT_TIMELINEPOST_REPLY);
        }

    }

    public static int PARENT_POST_DELETED = 400;
    int selectedPosition = -1;


    @Override
    public void onDeletePost() {
        Toast.makeText(TimelinePostActivity.this,"Deleted",Toast.LENGTH_SHORT).show();
        if(parentPostTapped) {
            setResult(PARENT_POST_DELETED);
            finish();
            parentPostTapped = false;
        } else {
            allComments.remove(selectedPosition);
            postsAdapter.notifyItemRemoved(selectedPosition);
        }
    }

    public void mOnCommentLongTapped(TimelinePost post, int position) {
        parentPostDelete = allComments.get(0);
        selectedPost = AoUtils.GetOriginalPost(post);
        selectedPosition = position;
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(AoUtils.GetOriginalPoster(post), TimelinePostActivity.this, null, TimelinePostActivity.this,false);
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void onLikeTappedListener(TimelinePost post) {

        int position = AoUtils.getPositionOfTimelinePost(allComments,post);
        actionTapped = true;
        ParseObject newPost = PostUtils.likePost(post);
        if(newPost != null) {
            allComments.set(position, (TimelinePost) newPost);
            postsAdapter.notifyItemChanged(position, newPost);
        }
    }

    @Override
    public void onRepostTappedListener(TimelinePost post) {

        TimelinePost sourceObject = (TimelinePost)post.getParseObject(TimelinePost.REPOST_SOURCE);
        if(sourceObject == null) {
            if(ParseUser.getCurrentUser().getObjectId().equals(post.getParseUser(TimelinePost.POSTED_BY).getObjectId())) {
                Toast.makeText(this,"Can't repost your own post",Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            if(ParseUser.getCurrentUser().getObjectId().equals(sourceObject.getParseUser(TimelinePost.POSTED_BY).getObjectId())) {
                Toast.makeText(this,"Can't repost your own post",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        int position = AoUtils.getPositionOfTimelinePost(allComments,sourceObject == null ? post : sourceObject);
        ArrayList<ParseObject> repost = PostUtils.repostPost(post);
        allComments.set(position,(TimelinePost)repost.get(0));
        postsAdapter.notifyItemChanged(position,repost.get(0));
    }


    @Override
    @TargetApi(23)
    public void mShareCallback(View view) {

        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            viewToShare = view;
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            AoUtils.ShareImageFromView(view,this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AoUtils.ShareImageFromView(viewToShare, this);
                } else {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot share the image. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
            case REQUEST_PICK_IMAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    addPostThreadHelper.openGalleryIntent();
                } else {
                    Toast.makeText(this, "Permission denied. Please allow the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    @Override
    public void onCommentTappedListener() {

        selectedPost = null;
        Intent i = new Intent(this,CreatePostActivity.class);
        i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.NEW_TIMELINEPOST_REPLY);
        AozoraForumsApp.setPostedBy(ParseUser.getCurrentUser());
        AozoraForumsApp.setPostedIn(parentPost.getParseUser(TimelinePost.USER_TIMELINE));
        if(parentPost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
            AozoraForumsApp.setUpdatedParentPost(parentPost.getParseObject(TimelinePost.REPOST_SOURCE));
        } else {
            AozoraForumsApp.setUpdatedParentPost(parentPost);
        }
        startActivityForResult(i,REQUEST_NEW_TIMELINEPOST_REPLY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_NEW_TIMELINEPOST_REPLY && resultCode == RESULT_OK) {
            TimelinePost post = (TimelinePost)AozoraForumsApp.getUpdatedPost();
            allComments.add(post);
            postsAdapter.notifyItemInserted(allComments.size()-1);
        } else   if ((requestCode == REQUEST_EDIT_TIMELINEPOST  || requestCode == REQUEST_EDIT_TIMELINEPOST_REPLY) && resultCode == RESULT_OK) {
            TimelinePost post = (TimelinePost)AozoraForumsApp.getUpdatedPost();
            int position = AoUtils.getPositionOfTimelinePost(allComments,post);
            allComments.set(position, post);
            postsAdapter.notifyItemChanged(position);
        } else  if(requestCode == REQUEST_SEARCH_IMAGE) {
            if(data != null && resultCode == SearchImageActivity.RESULT_SUCCESS) {
                clearAttachments();
                if (data.hasExtra(SearchImageActivity.IMAGE_DATA)){
                    imageDataWeb = (ImageData) data.getSerializableExtra(SearchImageActivity.IMAGE_DATA);
                    ivAddPhotoInternet.setColorFilter(ContextCompat.getColor(this,R.color.red_airing));
                }
            }
        } else if(requestCode == REQUEST_PICK_IMAGE) {
            if(resultCode == RESULT_OK) {
                clearAttachments();
                imageGallery = AoUtils.resizeImage(data.getData(),this);
                ivAddPhotoGallery.setColorFilter(ContextCompat.getColor(this,R.color.red_airing));
            }
        } else if (requestCode == REQUEST_SEARCH_YOUTUBE) {
            if(resultCode == RESULT_OK) {

                clearAttachments();
                if (data.hasExtra(SearchYoutubeActivity.YOUTUBE_ID)){
                    youtubeID = data.getStringExtra(SearchYoutubeActivity.YOUTUBE_ID);
                    ivAddVideo.setColorFilter(ContextCompat.getColor(this,R.color.red_airing));
                }
            }
        }
    }

    final int REQUEST_SEARCH_YOUTUBE = 402;
    final int REQUEST_PICK_IMAGE = 401;
    final int REQUEST_SEARCH_IMAGE = 400;
    ImageData imageDataWeb = null;
    ImageData imageGallery = null;
    String youtubeID = null;
    AddPostThreadHelper addPostThreadHelper;

    private void initAddCommentControls() {

        ParseObject realParent;
        if(parentPost.getParseObject(TimelinePost.REPOST_SOURCE) != null) {
            realParent = parentPost.getParseObject(TimelinePost.REPOST_SOURCE);
        } else {
            realParent = parentPost;
        }
        addPostThreadHelper = new AddPostThreadHelper(this,REQUEST_PICK_IMAGE,ParseUser.getCurrentUser(),parentPost.getParseUser(TimelinePost.USER_TIMELINE),
                realParent,null,AddPostThreadHelper.NEW_TIMELINEPOST_REPLY,null);
        ivAddPhotoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageDataWeb == null) {
                    Intent i = new Intent(TimelinePostActivity.this, SearchImageActivity.class);
                    startActivityForResult(i, REQUEST_SEARCH_IMAGE);
                } else {
                    imageDataWeb = null;
                    ivAddPhotoInternet.clearColorFilter();
                }
            }
        });
        ivAddPhotoGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageGallery == null) {
                    addPostThreadHelper.addPhotoGalleryTapped();
                } else {
                    imageGallery = null;
                    ivAddPhotoGallery.clearColorFilter();
                }
            }
        });
        ivAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(youtubeID == null) {
                    Intent i = new Intent(TimelinePostActivity.this, SearchYoutubeActivity.class);
                    startActivityForResult(i, REQUEST_SEARCH_YOUTUBE);
                }else {
                    youtubeID = null;
                    ivAddVideo.clearColorFilter();
                }
            }
        });

        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isValidPost()){
                    return;
                }

                btnSendComment.setText("Sending...");
                btnSendComment.setBackground(ContextCompat.getDrawable(TimelinePostActivity.this,R.drawable.button_sending));
                addPostThreadHelper.performTimelinePost(etComment.getText().toString(),"",false,false,imageGallery,imageDataWeb,youtubeID,null);
            }
        });
    }

    private boolean isValidPost() {
        int max =Math.max(0,etComment.getText().length());
        if(max < 1 && imageDataWeb == null && imageGallery == null && youtubeID == null){
            AoUtils.showAlertWithTitleAndText(this,"Too Short","Message/spoiler should be 1 character or longer");
            return false;
        }
        //check if its muted
        if (PUser.isMuted(ParseUser.getCurrentUser()))
            return false;
        return true;
    }

    private void clearAttachments() {
        youtubeID = null;
        imageGallery = null;
        imageDataWeb = null;
        ivAddPhotoInternet.clearColorFilter();
        ivAddPhotoGallery.clearColorFilter();
        ivAddVideo.clearColorFilter();
    }

    @Override
    public void onPerformPost(ParseObject post, ParseObject parentpost, ParseException e) {

        if(e!= null) {
            Toast.makeText(this,"An error occured, try again later",Toast.LENGTH_SHORT).show();
            btnSendComment.setText("Send");
            btnSendComment.setBackground(ContextCompat.getDrawable(this,R.drawable.button_send));
        } else {
            btnSendComment.setText("Send");
            btnSendComment.setBackground(ContextCompat.getDrawable(this,R.drawable.button_send));
            this.parentPost = (TimelinePost) parentpost;
            allComments.add((TimelinePost)post);
            postsAdapter.notifyItemInserted(allComments.size()-1);
            etComment.setText("");
            clearAttachments();
            addPostThreadHelper = new AddPostThreadHelper(this,REQUEST_PICK_IMAGE,ParseUser.getCurrentUser(),parentPost.getParseUser(TimelinePost.USER_TIMELINE)
                    ,parentPost,null,AddPostThreadHelper.NEW_TIMELINEPOST_REPLY,null);

        }

    }
}
