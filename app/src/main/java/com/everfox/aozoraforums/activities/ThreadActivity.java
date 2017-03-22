package com.everfox.aozoraforums.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.everfox.aozoraforums.adapters.AoThreadAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.AddPostThreadHelper;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controllers.ThreadHelper;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.fragments.CommentPostFragment;
import com.everfox.aozoraforums.fragments.FollowersFragment;
import com.everfox.aozoraforums.fragments.ForumsFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.fragments.UserListFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadActivity extends AozoraActivity implements AoThreadAdapter.OnUsernameTappedListener, ThreadHelper.OnGetThreadCommentsListener,
AoThreadAdapter.OnCommentTappedListener, AoThreadAdapter.OnUpDownVoteListener, AoThreadAdapter.OnItemLongClickListener, OptionListDialogFragment.OnListSelectedListener,
PostUtils.OnDeletePostCallback, ForumsHelper.OnBanDeletePostCallback, AoThreadAdapter.OnImageShareListener, AoThreadAdapter.OnAddPostTappedListener, AddPostThreadHelper.OnPerformPost{

    private static final int REQUEST_EDIT_THREAD_REPLY = 504;
    private static final int REQUEST_EDIT_THREAD = 503;
    private static final int REQUEST_WRITE_STORAGE = 100;
    private static final int REQUEST_ADD_POST = 401;
    View viewToShare;
    Boolean hasMenu = true;
    AoThread parentThread;
    AoLinearLayoutManager llm;
    AoThreadAdapter aoThreadAdapter;
    Boolean isLoading = false;
    ArrayList<ParseObject> lstComments = new ArrayList<>();
    public static int PARENT_POST_DELETED = 400;

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
    @BindView(R.id.flContent)
    FrameLayout flContent;
    ParseObject selectedComment;
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
        parentThread = AozoraForumsApp.getThreadToPass();
        if(parentThread == null) {
            AoUtils.startMainActivity(this);
            return;
        }
        hasMenu = parentThread.getHasMenu();
        llm = new AoLinearLayoutManager(this);
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
        new ThreadHelper(this,this,null).GetThreadComments(parentThread,0,2000);
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
                if (currentFragment != null && currentFragment instanceof ProfileFragment || currentFragment instanceof FollowersFragment || currentFragment instanceof UserListFragment) {
                    currentFragment.onResume();
                }
                if(selectedComment != null && currentFragment == null) {
                    updateComment();
                }
            }
        });
        initAddCommentControls();

        isLoading = true;


    }

    private void reloadThread() {
        isLoading = true;
        new ThreadHelper(this,this,null).GetThreadComments(parentThread,0,2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(parentThread != null)
            setTitle(parentThread.getString(AoThread.TITLE));

    }

    private void updateComment() {

        int position = lstComments.indexOf(selectedComment);
        if(position != -1) {
            lstComments.set(position, selectedComment);
            aoThreadAdapter.notifyItemChanged(position, selectedComment);
        }
    }


    @Override
    public void onGetThreadComments(List<ParseObject> comments) {

        swipeRefresh.setRefreshing(false);
        comments.add(0,parentThread);
        lstComments.clear();
        lstComments.addAll(comments);
        aoThreadAdapter = new AoThreadAdapter(this,lstComments,this);
        rvThreadComments.setAdapter(aoThreadAdapter);
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
        llAddComment.setVisibility(View.VISIBLE);
        RecyclerView.RecycledViewPool recycledViewPool = rvThreadComments.getRecycledViewPool();
        recycledViewPool.setMaxRecycledViews(AoThreadAdapter.ITEM_FIRST_POST,0);
        recycledViewPool.setMaxRecycledViews(AoThreadAdapter.ITEM_COMMENT,0);
        rvThreadComments.setRecycledViewPool(recycledViewPool);
        rvThreadComments.setItemViewCacheSize(200);
        isLoading = false;
        if(parentThread.has(AoThread.LOCKED) && parentThread.getBoolean(AoThread.LOCKED)) {
            llAddComment.setVisibility(View.GONE);
        }
    }


    @Override
    public void onUsernameTapped(ParseUser userTapped) {
        if(!AoUtils.isActivityInvalid(this)) {
            ProfileFragment profileFragment = null;
            if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null,hasMenu);
            else
                profileFragment = ProfileFragment.newInstance(userTapped, true, false,null,hasMenu);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flContent, profileFragment).addToBackStack(null).commitAllowingStateLoss();

        }
    }

    @Override
    public void onCommentTapped(ParseObject commentTapped) {
        if(!AoUtils.isActivityInvalid(this)) {
            if(parentThread.has(AoThread.LOCKED) && parentThread.getBoolean(AoThread.LOCKED)) {
                Toast.makeText(this,"Thread is locked",Toast.LENGTH_SHORT).show();
            } else {
                CommentPostFragment commentPostFragment = null;
                commentPostFragment = CommentPostFragment.newInstance((Post) commentTapped);
                selectedComment = commentTapped;
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.flContent, commentPostFragment).addToBackStack(null).commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void onUpDownVote(Boolean upvote, ParseObject object, int position) {
        ParseObject newPost = null;
        if(object instanceof AoThread) {
            if(upvote) {
                newPost = PostUtils.likePost(object);
            } else {
                newPost = PostUtils.unlikeThread(object);
            }
            if(newPost != null)
                aoThreadAdapter.notifyItemChanged(position,newPost);
        } else {
            newPost = PostUtils.likePost(object);
            if(newPost != null)
                aoThreadAdapter.notifyItemChanged(position,newPost);
        }
    }


    ParseObject selectedItem= null;
    int selectedPosition = -1;


    @Override
    public void onItemLongClicked(ParseObject aoThread) {

        selectedItem = aoThread;
        this.selectedPosition = lstComments.indexOf(aoThread);
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(aoThread.getParseUser(TimelinePost.POSTED_BY),this,null,this,false);
        fragment.setCancelable(true);
        fragment.show(getSupportFragmentManager(),"");
    }

    @Override
    public void onListSelected(Integer list, Integer selectedList) {

        List<String> optionList = AoUtils.getOptionListFromID(this, selectedList);
        String selectedOption = optionList.get(list);
        if(selectedList == AoConstants.REPORT_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.REPORT_POST_REPORT_CONTENT:
                    AoUtils.reportObject(selectedItem);
                    AoUtils.showAlertWithTitleAndText(this,"Report Sent!","The report will be reviewed by an admin and deleted/updated if needed.");
                    break;
            }
        }
        else if(selectedList == AoConstants.EDITDELETE_THREAD_OPTIONS_DIALOG || selectedList == AoConstants.EDITDELETE_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.ADMIN_POST_DELETE:
                    new AlertDialog.Builder(this)
                            .setTitle("Delete this forever?")
                            .setMessage("You can't undo this")
                            .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                            .setPositiveButton(R.string.dialog_delete_post, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    if(selectedItem instanceof AoThread)
                                        new ForumsHelper(ThreadActivity.this,null,ThreadActivity.this).deleteThread((AoThread)selectedItem);
                                    else
                                        new PostParseHelper(ThreadActivity.this,ThreadActivity.this,null).deletePost(selectedItem,null);
                                }
                            })
                            .create()
                            .show();
                    break;
                case AoConstants.POST_EDIT:
                    editSelectedPostTHread();
                    break;
            }
        }

    }

    private void editSelectedPostTHread() {
        if(selectedPosition == 0) {
            //edit thread
            Intent i = new Intent(this,CreatePostActivity.class);
            i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.EDIT_AOTHREAD);
            AozoraForumsApp.setPostedBy(selectedItem.getParseUser(TimelinePost.POSTED_BY));
            AozoraForumsApp.setPostedIn(null);
            AozoraForumsApp.setPostToUpdate(selectedItem);
            startActivityForResult(i,REQUEST_EDIT_THREAD);
        } else {
            //edit post
            Intent i = new Intent(this,CreatePostActivity.class);
            i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.EDIT_AOTHREAD_REPLY);
            AozoraForumsApp.setPostedBy(selectedItem.getParseUser(TimelinePost.POSTED_BY));
            AozoraForumsApp.setPostedIn(null);
            AozoraForumsApp.setPostToUpdate(selectedItem);
            AozoraForumsApp.setUpdatedParentThread(selectedItem.getParseObject(Post.THREAD));
            AozoraForumsApp.setUpdatedParentPost(null);
            startActivityForResult(i,REQUEST_EDIT_THREAD_REPLY);
        }
    }

    @Override
    public void onDeletePost() {

        lstComments.remove(selectedPosition);
        aoThreadAdapter.notifyItemRemoved(selectedPosition);
    }

    @Override
    public void onDeleteOrBan() {

        lstComments.remove(selectedPosition);
        aoThreadAdapter.notifyItemRemoved(selectedPosition);
        setResult(PARENT_POST_DELETED);
        finish();
    }

    public void onPostDeletedFromFragment(Post post) {

        int index = lstComments.indexOf(post);
        lstComments.remove(index);
        aoThreadAdapter.notifyItemRemoved(index);
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
            AoUtils.ShareImageFromView(view, this);
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
        }
    }

    @Override
    public void onAddPostTapped() {
        if(parentThread.has(AoThread.LOCKED) && parentThread.getBoolean(AoThread.LOCKED)) {
            Toast.makeText(this,"Thread is locked",Toast.LENGTH_SHORT).show();
        } else {
            selectedItem = null;
            selectedComment = null;
            Intent i = new Intent(this, CreatePostActivity.class);
            i.putExtra(CreatePostActivity.PARAM_TYPE, CreatePostActivity.NEW_AOTHREAD_REPLY);
            AozoraForumsApp.setPostedBy(ParseUser.getCurrentUser());
            AozoraForumsApp.setPostedIn(null);
            AozoraForumsApp.setUpdatedParentPost(null);
            AozoraForumsApp.setUpdatedParentThread(parentThread);
            startActivityForResult(i, REQUEST_ADD_POST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ADD_POST) {
                if(resultCode == RESULT_OK){
                    parentThread = (AoThread) AozoraForumsApp.getUpdatedParentPost();
                    Post post = (Post)AozoraForumsApp.getUpdatedPost();
                    lstComments.add(post);
                    aoThreadAdapter.notifyItemInserted(lstComments.size()-1);
                }
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
        }  else if (requestCode == REQUEST_EDIT_THREAD && resultCode == RESULT_OK) {
            AoThread thread = (AoThread)AozoraForumsApp.getUpdatedPost();
            lstComments.set(0, thread);
            aoThreadAdapter.notifyItemChanged(0);
        } else if (requestCode == REQUEST_EDIT_THREAD_REPLY && resultCode == RESULT_OK) {
            Post post = (Post) AozoraForumsApp.getUpdatedPost();
            int position = lstComments.indexOf(post);
            lstComments.set(position, post);
            aoThreadAdapter.notifyItemChanged(position);
        }
    }

    final int REQUEST_SEARCH_YOUTUBE = 502;
    final int REQUEST_PICK_IMAGE = 501;
    final int REQUEST_SEARCH_IMAGE = 500;
    ImageData imageDataWeb = null;
    ImageData imageGallery = null;
    String youtubeID = null;
    AddPostThreadHelper addPostThreadHelper;

    private void initAddCommentControls() {

        addPostThreadHelper = new AddPostThreadHelper(this,REQUEST_PICK_IMAGE,ParseUser.getCurrentUser(),null,
                null,parentThread,AddPostThreadHelper.NEW_AOTHREAD_REPLY,null);

        ivAddPhotoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageDataWeb == null) {
                    Intent i = new Intent(ThreadActivity.this, SearchImageActivity.class);
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
                    Intent i = new Intent(ThreadActivity.this, SearchYoutubeActivity.class);
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
                btnSendComment.setBackground(ContextCompat.getDrawable(ThreadActivity.this,R.drawable.button_sending));
                addPostThreadHelper.performPostPost(etComment.getText().toString(),false,imageGallery,imageDataWeb,youtubeID);
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
            this.parentThread = (AoThread) parentpost;
            lstComments.add(post);
            aoThreadAdapter.notifyItemInserted(lstComments.size()-1);
            etComment.setText("");
            clearAttachments();
            addPostThreadHelper = new AddPostThreadHelper(this,REQUEST_PICK_IMAGE,ParseUser.getCurrentUser(),null,null
                    ,parentThread,AddPostThreadHelper.NEW_AOTHREAD_REPLY,null);
        }
    }
}
