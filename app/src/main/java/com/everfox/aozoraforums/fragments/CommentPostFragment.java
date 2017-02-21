package com.everfox.aozoraforums.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.ThreadActivity;
import com.everfox.aozoraforums.adapters.CommentPostAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controllers.ThreadHelper;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.Post;
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
 * Created by daniel.soto on 2/6/2017.
 */

public class CommentPostFragment extends Fragment implements ThreadHelper.OnGetPostCommentsListener, CommentPostAdapter.OnUsernameTappedListener, CommentPostAdapter.OnLikeListener,
CommentPostAdapter.OnItemLongClickListener, OptionListDialogFragment.OnListSelectedListener, PostUtils.OnDeletePostCallback{

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
        return view;
    }

    private void reloadComments() {
        isLoading = true;
        new ThreadHelper(getActivity(),null,this).GetPostComments(post,0,2000);
    }


    @Override
    public void onGetPostComments(List<Post> comments) {

        swipeRefresh.setRefreshing(false);
        comments.add(0,post);
        lstComments.clear();
        lstComments.addAll(comments);
        commentPostAdapter = new CommentPostAdapter(getActivity(),lstComments,this);
        rvThreadComments.setAdapter(commentPostAdapter);
        pbLoading.setVisibility(View.GONE);
        swipeRefresh.setVisibility(View.VISIBLE);
        llAddComment.setVisibility(View.VISIBLE);
        if(commentPostAdapter.getItemCount() == 0){
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
    public void onLike(ParseObject object, int position) {
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
                                        getFragmentManager().popBackStack();
                                    } else {

                                        new PostParseHelper(getActivity(), null, CommentPostFragment.this).deletePost(selectedItem, post);
                                    }
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


    @Override
    public void onDeletePost() {

        lstComments.remove(selectedPosition);
        commentPostAdapter.notifyItemRemoved(selectedPosition);

    }

}
