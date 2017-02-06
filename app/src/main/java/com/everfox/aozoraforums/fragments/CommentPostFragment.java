package com.everfox.aozoraforums.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.CommentPostAdapter;
import com.everfox.aozoraforums.adapters.TimelinePostsAdapter;
import com.everfox.aozoraforums.controllers.ThreadHelper;
import com.everfox.aozoraforums.models.Post;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/6/2017.
 */

public class CommentPostFragment extends Fragment implements ThreadHelper.OnGetPostCommentsListener, CommentPostAdapter.OnUsernameTappedListener {

    Post post;
    LinearLayoutManager llm;
    CommentPostAdapter commentPostAdapter;
    Boolean isLoading = false;

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
        commentPostAdapter = new CommentPostAdapter(getActivity(),comments,this);
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
                profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null);
            else
                profileFragment = ProfileFragment.newInstance(userTapped, true, false,null);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flNewFragments, profileFragment).addToBackStack(null).commitAllowingStateLoss();

        }
    }
}
