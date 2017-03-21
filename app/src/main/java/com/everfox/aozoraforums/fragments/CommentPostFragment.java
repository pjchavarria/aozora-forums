package com.everfox.aozoraforums.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.ThreadActivity;
import com.everfox.aozoraforums.activities.postthread.CreatePostActivity;
import com.everfox.aozoraforums.activities.postthread.SearchImageActivity;
import com.everfox.aozoraforums.activities.postthread.SearchYoutubeActivity;
import com.everfox.aozoraforums.adapters.CommentPostAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.AddPostThreadHelper;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controllers.ThreadHelper;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.models.PUser;
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

/**
 * Created by daniel.soto on 2/6/2017.
 */

public class CommentPostFragment extends Fragment implements ThreadHelper.OnGetPostCommentsListener, CommentPostAdapter.OnUsernameTappedListener, CommentPostAdapter.OnLikeListener,
CommentPostAdapter.OnItemLongClickListener, OptionListDialogFragment.OnListSelectedListener, PostUtils.OnDeletePostCallback, AddPostThreadHelper.OnPerformPost{

    public static final int REQUEST_EDIT_AOTHREAD_REPLY = 503;
    Post post;
    LinearLayoutManager llm;
    CommentPostAdapter commentPostAdapter;
    Boolean isLoading = false;
    ArrayList<Post> lstComments = new ArrayList<>();

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvComments)
    RecyclerView rvThreadComments;
    @BindView(R.id.llAddComment)
    LinearLayout llAddComment;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
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


    public static CommentPostFragment newInstance (Post parentComment) {
        CommentPostFragment commentPostFragment = new CommentPostFragment();
        commentPostFragment.post = parentComment;
        return commentPostFragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_commentthread,container,false);
        ButterKnife.bind(this,view);
        llm = new LinearLayoutManager(getActivity());
        rvThreadComments.setLayoutManager(llm);
        commentPostAdapter = new CommentPostAdapter(getActivity(),new ArrayList<Post>(),this);
        rvThreadComments.setAdapter(commentPostAdapter);
        pbLoading.setVisibility(View.VISIBLE);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!isLoading)
                    reloadComments();
            }
        });
        new ThreadHelper(getActivity(),null,this).GetPostComments(post,0,2000);
        isLoading = true;
        initAddCommentControls();
        return view;
    }

    private void reloadComments() {
        isLoading = true;
        new ThreadHelper(getActivity(),null,this).GetPostComments(post,0,2000);
    }


    @Override
    public void onGetPostComments(List<Post> comments) {

        post.setReplies(new ArrayList<>(comments));
        swipeRefresh.setRefreshing(false);
        comments.add(0,post);
        lstComments.clear();
        lstComments.addAll(comments);
        commentPostAdapter = new CommentPostAdapter(getActivity(),lstComments,this);
        rvThreadComments.setAdapter(commentPostAdapter);
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
        llAddComment.setVisibility(View.VISIBLE);
        RecyclerView.RecycledViewPool recycledViewPool = rvThreadComments.getRecycledViewPool();
        recycledViewPool.setMaxRecycledViews(commentPostAdapter.ITEM_FIRST_POST,0);
        recycledViewPool.setMaxRecycledViews(commentPostAdapter.ITEM_COMMENT,0);
        rvThreadComments.setRecycledViewPool(recycledViewPool);
        rvThreadComments.setItemViewCacheSize(200);

        isLoading = false;
    }

    @Override
    public void onUsernameTapped(ParseUser userTapped) {

        if(!AoUtils.isActivityInvalid(getActivity())) {
            ProfileFragment profileFragment = null;
            if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null,true);
            else
                profileFragment = ProfileFragment.newInstance(userTapped, true, false,null,true);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flContent, profileFragment).addToBackStack(null).commitAllowingStateLoss();

        }
    }

    @Override
    public void onLike(ParseObject object) {
        int position = lstComments.indexOf(object);
        ParseObject newPost = PostUtils.likePost(object);
        if(newPost != null)
            commentPostAdapter.notifyItemChanged(position,newPost);
    }

    ParseObject selectedItem= null;
    int selectedPosition = -1;

    @Override
    public void onItemLongClicked(ParseObject aoThread) {

        selectedItem = aoThread;
        this.selectedPosition = lstComments.indexOf(aoThread);
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(aoThread.getParseUser(TimelinePost.POSTED_BY),getActivity(),CommentPostFragment.this,null,false);
        fragment.setCancelable(true);
        fragment.show(getFragmentManager(),"");
    }

    @Override
    public void onListSelected(Integer list, Integer selectedList) {
        List<String> optionList = AoUtils.getOptionListFromID(getActivity(), selectedList);
        String selectedOption = optionList.get(list);
        if(selectedList == AoConstants.REPORT_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.REPORT_POST_REPORT_CONTENT:
                    AoUtils.reportObject(selectedItem);
                    AoUtils.showAlertWithTitleAndText(getActivity(),"Report Sent!","The report will be reviewed by an admin and deleted/updated if needed.");
                    break;
            }
        }
        else if(selectedList == AoConstants.EDITDELETE_THREAD_OPTIONS_DIALOG || selectedList == AoConstants.EDITDELETE_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.ADMIN_POST_DELETE:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Delete this forever?")
                            .setMessage("You can't undo this")
                            .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                            .setPositiveButton(R.string.dialog_delete_post, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    if(selectedPosition == 0) {
                                        new PostParseHelper(getActivity(), null, CommentPostFragment.this).deletePost(selectedItem, null);
                                    } else {

                                        new PostParseHelper(getActivity(), null, CommentPostFragment.this).deletePost(selectedItem, post);
                                    }
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
    }

    private void editSelectedPost() {

        Intent i = new Intent(getActivity(),CreatePostActivity.class);
        i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.EDIT_AOTHREAD_REPLY);
        AozoraForumsApp.setPostedBy(selectedItem.getParseUser(TimelinePost.POSTED_BY));
        AozoraForumsApp.setPostedIn(null);
        AozoraForumsApp.setPostToUpdate(selectedItem);
        AozoraForumsApp.setUpdatedParentThread(post.getParseObject(Post.THREAD));
        if(selectedPosition != 0) {
            AozoraForumsApp.setUpdatedParentPost(post);
        } else {
            AozoraForumsApp.setUpdatedParentPost(null);
        }
        startActivityForResult(i,REQUEST_EDIT_AOTHREAD_REPLY);
    }


    @Override
    public void onDeletePost() {
        Post post = lstComments.get(selectedPosition);
        lstComments.remove(selectedPosition);
        commentPostAdapter.notifyItemRemoved(selectedPosition);
        if(selectedPosition == 0) {
            if(getActivity() != null && !getActivity().isDestroyed())
                ((ThreadActivity) getActivity()).onPostDeletedFromFragment(post);
            getFragmentManager().popBackStack();
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

        addPostThreadHelper = new AddPostThreadHelper(getActivity(),REQUEST_PICK_IMAGE,ParseUser.getCurrentUser(),null,
                post,post.getParseObject(Post.THREAD),AddPostThreadHelper.NEW_AOTHREAD_REPLY,null);
        addPostThreadHelper.setFragmentCallback(this);

        ivAddPhotoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageDataWeb == null) {
                    Intent i = new Intent(getActivity(), SearchImageActivity.class);
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
                    Intent i = new Intent(getActivity(), SearchYoutubeActivity.class);
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
                btnSendComment.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.button_sending));
                addPostThreadHelper.performPostPost(etComment.getText().toString(),false,imageGallery,imageDataWeb,youtubeID);
            }
        });
    }

    private boolean isValidPost() {
        int max =Math.max(0,etComment.getText().length());
        if(max < 1 && imageDataWeb == null && imageGallery == null && youtubeID == null){
            AoUtils.showAlertWithTitleAndText(getActivity(),"Too Short","Message/spoiler should be 1 character or longer");
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
            Toast.makeText(getActivity(),"An error occured, try again later",Toast.LENGTH_SHORT).show();
            btnSendComment.setText("Send");
            btnSendComment.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.button_send));
        } else {
            btnSendComment.setText("Send");
            btnSendComment.setBackground(ContextCompat.getDrawable(getActivity(),R.drawable.button_send));
            this.post = (Post) parentpost;
            lstComments.add((Post)post);
            commentPostAdapter.notifyItemInserted(lstComments.size()-1);
            etComment.setText("");
            clearAttachments();
            addPostThreadHelper = new AddPostThreadHelper(getActivity(),REQUEST_PICK_IMAGE,ParseUser.getCurrentUser(),null,
                    post,post.getParseObject(Post.THREAD),AddPostThreadHelper.NEW_AOTHREAD_REPLY,null);
            addPostThreadHelper.setFragmentCallback(this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
         if(requestCode == REQUEST_SEARCH_IMAGE) {
            if(data != null && resultCode == SearchImageActivity.RESULT_SUCCESS) {
                clearAttachments();
                if (data.hasExtra(SearchImageActivity.IMAGE_DATA)){
                    imageDataWeb = (ImageData) data.getSerializableExtra(SearchImageActivity.IMAGE_DATA);
                    ivAddPhotoInternet.setColorFilter(ContextCompat.getColor(getActivity(),R.color.red_airing));
                }
            }
        } else if(requestCode == REQUEST_PICK_IMAGE) {
            if(resultCode == getActivity().RESULT_OK) {
                clearAttachments();
                imageGallery = AoUtils.resizeImage(data.getData(),getActivity());
                ivAddPhotoGallery.setColorFilter(ContextCompat.getColor(getActivity(),R.color.red_airing));
            }
        } else if (requestCode == REQUEST_SEARCH_YOUTUBE) {
            if(resultCode == getActivity().RESULT_OK) {

                clearAttachments();
                if (data.hasExtra(SearchYoutubeActivity.YOUTUBE_ID)){
                    youtubeID = data.getStringExtra(SearchYoutubeActivity.YOUTUBE_ID);
                    ivAddVideo.setColorFilter(ContextCompat.getColor(getActivity(),R.color.red_airing));
                }
            }
        } else if (requestCode == REQUEST_EDIT_AOTHREAD_REPLY && resultCode == getActivity().RESULT_OK) {
             Post post = (Post)AozoraForumsApp.getUpdatedPost();
             int position =  lstComments.indexOf(post);
             lstComments.set(position, post);
             commentPostAdapter.notifyItemChanged(position);
         }
    }
}
