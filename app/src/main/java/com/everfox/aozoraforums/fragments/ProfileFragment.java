package com.everfox.aozoraforums.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.EditProfileActivity;
import com.everfox.aozoraforums.activities.SettingsActivity;
import com.everfox.aozoraforums.activities.TimelinePostActivity;
import com.everfox.aozoraforums.activities.postthread.CreatePostActivity;
import com.everfox.aozoraforums.adapters.ProfileTimelineAdapter;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.controls.EndlessScrollView;
import com.everfox.aozoraforums.dialogfragments.MuteUserDialogFragment;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.dialogfragments.SimpleLoadingDialogFragment;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.everfox.aozoraforums.utils.ProfileUtils;
import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by daniel.soto on 1/10/2017.
 */

public class ProfileFragment extends Fragment implements ProfileParseHelper.OnGetProfilePostsListener, EndlessScrollView.EndlessScrollListener,
OptionListDialogFragment.OnListSelectedListener, ProfileTimelineAdapter.OnUsernameTappedListener, ProfileTimelineAdapter.OnMoreOptionsTappedListener,
PostUtils.OnDeletePostCallback, ProfileTimelineAdapter.OnItemTappedListener, ProfileTimelineAdapter.OnLikeTappedListener, ProfileTimelineAdapter.OnRepostTappedListener,
        ProfileTimelineAdapter.OnImageShareListener, ProfileTimelineAdapter.OnCommentTappedListener
{

    private static final int REQUEST_NEW_TIMELINEPOST_REPLY = 402;
    private static final int REQUEST_NEW_TIMELINEPOST = 401;
    private static final int REQUEST_WRITE_STORAGE = 100;
    View viewToShare;
    SimpleLoadingDialogFragment simpleLoadingDialogFragment = new SimpleLoadingDialogFragment();
    Boolean isFollowing;
    ParseUser user;
    UserDetails userDetails;
    Boolean isProfile;
    Boolean isCurrentUser;
    ProfileTimelineAdapter timelineAdapter;
    String postCount;
    LinearLayoutManager llm;
    Boolean loadingMorePosts = false;
    private int fetchCount = 0;
    ArrayList<TimelinePost> lstTimelinePost = new ArrayList<>();
    int selectedList;
    String userID = null;
    Date lastItemDate = null;
    Boolean hasMenu = true;
    Boolean firstTime = true;

    RecyclerView rvTimeline;

    @BindView(R.id.pbLoading) ProgressBar pbLoading;
    @BindView(R.id.llFeedFragment)
    LinearLayout llFeedFragment;
    @BindView(R.id.llProfileContent)
    LinearLayout llProfileContent;
    @BindView(R.id.ivProfileBanner)
    ImageView ivProfileBanner;
    @BindView(R.id.ivAvatar) ImageView ivAvatar;
    @BindView(R.id.tvPopularity)
    TextView tvPopularity;
    @BindView(R.id.tvPro) TextView tvPro;
    @BindView(R.id.tvBadge) TextView tvBadge;
    @BindView(R.id.tvUsername) TextView tvUsername;
    @BindView(R.id.tvLastActive) TextView tvLastActive;
    @BindView(R.id.tvFollowing) TextView tvFollowing;
    @BindView(R.id.tvFollowers) TextView tvFollowers;
    @BindView(R.id.llFeed) LinearLayout llFeed;
    @BindView(R.id.tvIntroduction) TextView tvIntroduction;
    @BindView(R.id.scrollView)
    EndlessScrollView scrollView;
    @BindView(R.id.tvFollow)
    TextView tvFollow;
    @BindView(R.id.ivMoreOptions)
    ImageView ivMoreOptions;
    @BindView(R.id.followingMark)
    View vFollowingMark;
    @BindView(R.id.aozoraMark)
    View vAozoraMark;
    @BindView(R.id.tvFollowingTab)
    LinearLayout tvFollowingTab;
    @BindView(R.id.tvAozoraTab)
    LinearLayout tvAozoraTab;
    @BindView(R.id.swipeRefreshFeed)
    SwipeRefreshLayout swipeRefreshFeed;
    @BindView(R.id.llProfileTimeline)
    LinearLayout llProfileTimeline;
    @BindView(R.id.llFollowers)
    LinearLayout llFollowers;
    @BindView(R.id.llFollowing)
    LinearLayout llFollowing;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.fabNewTimelinePost)
    FloatingActionButton fabNewTimelinePost;

    public static ProfileFragment newInstance(ParseUser user,Boolean isProfile, Boolean isCurrentUser, String userID, Boolean hasMenu) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.user = user;
        fragment.isProfile = isProfile;
        fragment.isCurrentUser = isCurrentUser;
        fragment.userID = userID;
        fragment.hasMenu = hasMenu;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this,view);
        lstTimelinePost = new ArrayList<>();
        firstTime = true;
        fetchCount = 0;
        if(isProfile) {
            rvTimeline = (RecyclerView) view.findViewById(R.id.rvTimeline);
        } else {
            fabNewTimelinePost.setVisibility(View.GONE);
            rvTimeline = (RecyclerView) view.findViewById(R.id.rvFeed);
        }
        llm = new LinearLayoutManager(getActivity());
        rvTimeline.setLayoutManager(llm);
        timelineAdapter = new ProfileTimelineAdapter(getActivity(),new ArrayList<TimelinePost>(),ProfileFragment.this,ParseUser.getCurrentUser());
        rvTimeline.setAdapter(timelineAdapter);
        scrollView.setScrollViewListener(this);
        pbLoading.setVisibility(View.VISIBLE);
        llProfileContent.setVisibility(View.GONE);
        ivProfileBanner.setVisibility(View.GONE);
        llProfileTimeline.setVisibility(View.GONE);
        if(user != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getString(ParseUserColumns.AOZORA_USERNAME));
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if(!hasMenu) {
            menu.clear();
        }
        if(menu.findItem(R.id.settings) == null)
            inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(user != null) {
            if (!isProfile) {
                selectedList = ProfileParseHelper.FOLLOWING_LIST;
                tvFollowingTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selectedList != ProfileParseHelper.FOLLOWING_LIST) {
                            if (!loadingMorePosts) {
                                selectedList = ProfileParseHelper.FOLLOWING_LIST;
                                vFollowingMark.setVisibility(View.VISIBLE);
                                vAozoraMark.setVisibility(View.INVISIBLE);
                                reloadPosts(false);
                            } else {
                                Toast.makeText(getActivity(), "Currently loading posts, please try in a few seconds", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            scrollFeedToStart();
                        }
                    }
                });
                tvAozoraTab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (selectedList != ProfileParseHelper.AOZORA_LIST) {
                            if (!loadingMorePosts) {
                                selectedList = ProfileParseHelper.AOZORA_LIST;
                                vFollowingMark.setVisibility(View.INVISIBLE);
                                vAozoraMark.setVisibility(View.VISIBLE);
                                reloadPosts(false);

                            } else {
                                Toast.makeText(getActivity(), "Currently loading posts, please try in a few seconds", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            scrollFeedToStart();
                        }
                    }
                });

                rvTimeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        if (loadingMorePosts)
                            return;
                        int visibleItemCount = llm.getChildCount();
                        int totalItemCount = llm.getItemCount();
                        int pastVisibleItems = llm.findFirstVisibleItemPosition();
                        if (pastVisibleItems + visibleItemCount >= totalItemCount && !loadingMorePosts) {
                            if(lstTimelinePost.size()>0) {
                                lastItemDate = lstTimelinePost.get(lstTimelinePost.size()-1).getDate(TimelinePost.CREATED_AT);
                            }
                            scrolledToEnd();
                        }
                    }
                });

                swipeRefreshFeed.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        reloadPosts(true);
                    }
                });
                swipeRefresh.setVisibility(View.GONE);
                scrollView.setVisibility(View.GONE);
            } else {
                //LoadProfile
                swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        reloadPosts(true);
                    }
                });

                fabNewTimelinePost.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selectedPost = null;
                        Intent i = new Intent(getActivity(),CreatePostActivity.class);
                        i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.NEW_TIMELINEPOST);
                        AozoraForumsApp.setPostedBy(ParseUser.getCurrentUser());
                        AozoraForumsApp.setPostedIn(user);
                        startActivityForResult(i,REQUEST_NEW_TIMELINEPOST);
                    }
                });
                loadProfile();
            }

            new ProfileParseHelper(getActivity(), ProfileFragment.this)
                    .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList,null);
            fetchCount = 1;
        } else {

            ParseQuery<ParseUser> userParseQuery = ParseQuery.getQuery(ParseUser.class);
            userParseQuery.whereEqualTo(ParseUserColumns.OBJECT_ID,userID);
            userParseQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    if(object != null && e==null) {
                        user = object;
                        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getString(ParseUserColumns.AOZORA_USERNAME));
                        loadProfile();
                        new ProfileParseHelper(getActivity(), ProfileFragment.this)
                                .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList,null);
                        fetchCount = 1;
                    } else {
                        Toast.makeText(getActivity(),"A problem occured, try again later",Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }


    private void loadProfile() {


        selectedList = ProfileParseHelper.PROFILE_LIST;
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        ParseFile bannerPic = user.getParseFile(ParseUserColumns.BANNER);
        loadAvatarAndBanner(profilePic,bannerPic);
        tvPopularity.setText(AoUtils.reputationToString(user.getNumber(ParseUserColumns.REPUTATION)));
        if(!isCurrentUser) {
            tvFollow.setVisibility(View.VISIBLE);
            tvFollow.setTypeface(AozoraForumsApp.getAwesomeTypeface());
            //SI LO ESTA SIGIENDO FOLLOWING, SINO FOLLOW

            ParseRelation<ParseObject> relation = ParseUser.getCurrentUser().getRelation(ParseUserColumns.FOLLOWING);
            relation.getQuery().whereEqualTo(ParseUserColumns.OBJECT_ID, user.getObjectId()).countInBackground(new CountCallback() {
                @Override
                public void done(int count, ParseException e) {
                    if (count > 0) {
                        tvFollow.setText(getString(R.string.fa_check) + " Following");
                        isFollowing = true;
                    } else {
                        tvFollow.setText(getString(R.string.fa_plus) + " Follow");
                        isFollowing = false;
                    }
                }
            });
            tvFollow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PUser.followUser(user,!isFollowing);
                    if(isFollowing) {
                        tvFollow.setText(getString(R.string.fa_plus) + " Follow");
                        isFollowing = false;
                    } else {
                        tvFollow.setText(getString(R.string.fa_check) + " Following");
                        isFollowing = true;
                    }
                }
            });
        }

        String badgePro = ProfileUtils.badgesArrayToPro(user.getJSONArray(ParseUserColumns.BADGES));
        if(badgePro == "")
            tvPro.setVisibility(View.INVISIBLE);
        else
            tvPro.setText(badgePro);
        String badge = ProfileUtils.badgesArrayToBadge(user.getJSONArray(ParseUserColumns.BADGES));
        if(badge == "")
            tvBadge.setVisibility(View.INVISIBLE);
        else
            tvBadge.setText(badge);
        tvUsername.setText(user.getString(ParseUserColumns.AOZORA_USERNAME));
        if(isCurrentUser)
            tvLastActive.setText(getResources().getString(R.string.active_now));
        else
            tvLastActive.setText(ProfileUtils.lastActiveFromUser(user));

        getUserDetails();

        ivMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = user.getString(ParseUserColumns.AOZORA_USERNAME);
                Date joinDate = user.getDate(ParseUserColumns.JOIN_DATE);
                DateFormat df = new SimpleDateFormat("MMM dd,yyyy");
                String subtitle1 = "Member since: " + df.format(joinDate) ;
                String subtitle2 = "Posts: " + postCount;
                OptionListDialogFragment fragment;
                if(isCurrentUser)
                    fragment = OptionListDialogFragment.newInstance(getActivity(),title,subtitle1,subtitle2,ProfileFragment.this, AoConstants.MY_PROFILE_OPTIONS_DIALOG,null);
                else
                    fragment = OptionListDialogFragment.newInstance(getActivity(),title,subtitle1,subtitle2,ProfileFragment.this, AoConstants.MY_PROFILE_OTHER_USER_OPTIONS_DIALOG,null);
                fragment.setCancelable(true);
                fragment.show(getFragmentManager(),"");
            }
        });

    }

    private void reloadPosts(Boolean isFromPull) {
        fetchCount = 0;
        if(!isFromPull) {
            rvTimeline.setVisibility(View.GONE);
            if (!isProfile)
                pbLoading.setVisibility(View.VISIBLE);
        }
        lstTimelinePost.clear();
        timelineAdapter.notifyDataSetChanged();
        new ProfileParseHelper(getActivity(), ProfileFragment.this)
                .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList,null);
        fetchCount++;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(user != null && !firstTime) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getString(ParseUserColumns.AOZORA_USERNAME));
            if(selectedPost != null)
                updatePost();
            if(isProfile) {
                getUserDetails();
                ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
                ParseFile bannerPic = user.getParseFile(ParseUserColumns.BANNER);
                loadAvatarAndBanner(profilePic, bannerPic);
            }
        }
        firstTime = false;

    }

    private void updatePost() {
        int position = AoUtils.getPositionOfTimelinePost(lstTimelinePost, selectedPost);
        if (position != -1) {
            lstTimelinePost.set(position, selectedPost);
            timelineAdapter.notifyItemChanged(position, selectedPost);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void getUserDetails() {
        ParseObject details =user.getParseObject(ParseUserColumns.DETAILS);
        if(details == null) {

            ParseQuery<UserDetails> queryDetails = ParseQuery.getQuery(UserDetails.class);
            queryDetails.setLimit(1);
            queryDetails.whereEqualTo(UserDetails.DETAILS_USER, user);
            queryDetails.findInBackground(new FindCallback<UserDetails>() {
                @Override
                public void done(List<UserDetails> objects, ParseException e) {
                    if (objects != null && e == null && objects.size() > 0) {
                        userDetails = objects.get(0);
                        loadUserDetails();
                    }
                }
            });

        } else {
            details.fetchInBackground(new GetCallback<UserDetails>() {
                @Override
                public void done(UserDetails object, ParseException e) {

                    if (object == null) {
                        ParseQuery<UserDetails> queryDetails = ParseQuery.getQuery(UserDetails.class);
                        queryDetails.setLimit(1);
                        queryDetails.whereEqualTo(UserDetails.DETAILS_USER, user);
                        queryDetails.findInBackground(new FindCallback<UserDetails>() {
                            @Override
                            public void done(List<UserDetails> objects, ParseException e) {
                                if (objects != null && e == null && objects.size() > 0) {
                                    userDetails = objects.get(0);
                                    loadUserDetails();
                                }
                            }
                        });
                    } else {
                        userDetails = object;
                        loadUserDetails();
                    }
                }
            });
        }
    }

    private void loadUserDetails() {
        tvFollowers.setText(AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.FOLLOWERS)));
        llFollowers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!AoUtils.isActivityInvalid(getActivity())) {
                    OpenFollowersFragment(true);
                }
            }
        });
        tvFollowing.setText(AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.FOLLOWING)));
        llFollowing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!AoUtils.isActivityInvalid(getActivity())) {
                    OpenFollowersFragment(false);
                }
            }
        });
        tvIntroduction.setText(userDetails.getString(UserDetails.ABOUT));
        postCount = AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.POSTS));
    }

    private void OpenFollowersFragment(Boolean isFollowers) {
        FollowersFragment ff = null;
        ff = FollowersFragment.newInstance(user,isFollowers);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.flContent, ff).addToBackStack(null).commitAllowingStateLoss();
    }

    private void loadAvatarAndBanner(ParseFile profilePic, ParseFile bannerPic) {
        if(profilePic != null) {
            profilePic.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(data, 0, data.length);
                        ivAvatar.setImageBitmap(bmp);
                    }
                }
            });
        }
        if(bannerPic != null) {
            bannerPic.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(data, 0, data.length);
                        ivProfileBanner.setImageBitmap(bmp);
                    }
                }
            });
        }
    }

    @Override
    public void onGetProfilePosts(List<TimelinePost> _timelinePosts) {

        if(_timelinePosts.size() == 0)
            fetchCount--;
        if(timelineAdapter.getItemCount() == 0) {
            _timelinePosts = AoUtils.clearTimelinePostDuplicates(new ArrayList<>(_timelinePosts));
            lstTimelinePost.addAll(_timelinePosts);
            timelineAdapter = new ProfileTimelineAdapter(getActivity(), lstTimelinePost, ProfileFragment.this, ParseUser.getCurrentUser());
            rvTimeline.setAdapter(timelineAdapter);
            pbLoading.setVisibility(View.GONE);
            rvTimeline.setVisibility(View.VISIBLE);
            if(isProfile) {
                swipeRefresh.setRefreshing(false);
                llProfileContent.setVisibility(View.VISIBLE);
                ivProfileBanner.setVisibility(View.VISIBLE);
                llProfileTimeline.setVisibility(View.VISIBLE);
            }
            else {
                swipeRefreshFeed.setRefreshing(false);
                llFeed.setVisibility(View.VISIBLE);
                llFeedFragment.setVisibility(View.VISIBLE);
            }
        } else {
            //remove progress item
            lstTimelinePost.remove(lstTimelinePost.size() - 1);
            timelineAdapter.notifyItemRemoved(lstTimelinePost.size());
            if(_timelinePosts.size() > 0 ) {
                int currentPosition = lstTimelinePost.size();
                ArrayList<TimelinePost> auxList = new ArrayList<>();
                auxList.addAll(lstTimelinePost);
                auxList.addAll(_timelinePosts);
                auxList = AoUtils.clearTimelinePostDuplicates(auxList);
                for (int i = lstTimelinePost.size(); i < auxList.size(); i++) {
                    lstTimelinePost.add(auxList.get(i));
                }
                timelineAdapter.notifyItemRangeInserted(currentPosition, lstTimelinePost.size() - currentPosition);
            }
        }
        loadingMorePosts = false;


    }

    @Override
    public void onScrollChanged(EndlessScrollView scrollView, int x, int y, int oldx, int oldy) {
        // We take the last son in the scrollview
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int distanceToEnd = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
        // if diff is zero, then the bottom has been reached
        if (distanceToEnd < 500 && !loadingMorePosts) {
            if(lstTimelinePost.size()>0) {
                lastItemDate = lstTimelinePost.get(lstTimelinePost.size()-1).getDate(TimelinePost.CREATED_AT);
            }
            scrolledToEnd();
        }
    }

    private void scrolledToEnd() {

        loadingMorePosts = true;
        lstTimelinePost.add(new TimelinePost());
        rvTimeline.post(new Runnable() {
            @Override
            public void run() {
                timelineAdapter.notifyItemInserted(lstTimelinePost.size());
            }
        });
        new ProfileParseHelper(getActivity(), ProfileFragment.this)
                .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList,lastItemDate);
        fetchCount++;
    }

    @Override
    public void onListSelected(Integer list, Integer selectedList) {

        List<String> optionList = AoUtils.getOptionListFromID(getActivity(),selectedList);
        String selectedOption = optionList.get(list);
        if(selectedList == AoConstants.MY_PROFILE_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.MY_PROFILE_DISCOVER:
                    OptionListDialogFragment fUserList = OptionListDialogFragment.newInstance(getActivity(),"User Lists","Find fellow anime fans",null,ProfileFragment.this, AoConstants.USER_LIST_OPTIONS_DIALOG,null);
                    fUserList.setCancelable(true);
                    fUserList.show(getFragmentManager(),"");
                    break;
                case AoConstants.MY_PROFILE_REPUTATION:
                    OptionListDialogFragment fRep = OptionListDialogFragment.newInstance(getActivity(),null,null,null,ProfileFragment.this, AoConstants.REPUTATION_RANKS_OPTIONS_DIALOG,null);
                    fRep.setCancelable(true);
                    fRep.show(getFragmentManager(),"");
                    break;
                case AoConstants.MY_PROFILE_EDIT:
                    Intent i = new Intent(getActivity(), EditProfileActivity.class);
                    startActivity(i);
                    break;
                case AoConstants.MY_PROFILE_THREADS:
                    ThreadByUserFragment pf = ThreadByUserFragment.newInstance(user);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.flContent, pf).addToBackStack(null).commitAllowingStateLoss();
                    break;
            }
        }
        if(selectedList == AoConstants.MY_PROFILE_OTHER_USER_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.MY_PROFILE_OTHERUSER_THREADS:
                    ThreadByUserFragment pf = ThreadByUserFragment.newInstance(user);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.add(R.id.flContent, pf).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case AoConstants.MY_PROFILE_OTHERUSER_MUTE:
                    MuteUserDialogFragment fragment;
                    fragment = MuteUserDialogFragment.newInstance(getActivity(),user);
                    fragment.setCancelable(true);
                    fragment.show(getFragmentManager(),"");
                    break;
            }
        }
        if(selectedList == AoConstants.EDITDELETE_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.ADMIN_POST_DELETE:
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Delete this post forever?")
                            .setMessage("You can't undo this")
                            .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                            .setPositiveButton(R.string.dialog_delete_post, new DialogInterface.OnClickListener() {
                                @Override public void onClick(DialogInterface dialog, int which) {
                                    simpleLoadingDialogFragment.show(getFragmentManager(),"loading");
                                    new ProfileParseHelper(getActivity(),ProfileFragment.this).deletePost(selectedPost,null);
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
        if(selectedList == AoConstants.EDIT_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.POST_EDIT:
                    Toast.makeText(getActivity(),"Edit Post", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        if(selectedList == AoConstants.REPORT_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.REPORT_POST_REPORT_CONTENT:
                    AoUtils.reportObject(selectedPost);
                    AoUtils.showAlertWithTitleAndText(getActivity(),"Report Sent!","The report will be reviewed by an admin and deleted/updated if needed.");
                    break;
            }
        }
        if(selectedList == AoConstants.USER_LIST_OPTIONS_DIALOG) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            switch (selectedOption){
                case AoConstants.USER_LIST_NEW:
                    UserListFragment fragment = UserListFragment.newInstance(UserListFragment.NEW_USERS);
                    fragmentTransaction.add(R.id.flContent, fragment).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case AoConstants.USER_LIST_NEWPRO:
                    UserListFragment fragment1 = UserListFragment.newInstance(UserListFragment.NEW_PRO_MEMBERS);
                    fragmentTransaction.add(R.id.flContent, fragment1).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case AoConstants.USER_LIST_OLDESTACTIVE:
                    UserListFragment fragment2 = UserListFragment.newInstance(UserListFragment.OLDEST_ACTIVE_USERS);
                    fragmentTransaction.add(R.id.flContent, fragment2).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case AoConstants.USER_LIST_ONLINE:
                    UserListFragment fragment3 = UserListFragment.newInstance(UserListFragment.ONLINE_NOW);
                    fragmentTransaction.add(R.id.flContent, fragment3).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case AoConstants.USER_LIST_STAFF:
                    UserListFragment fragment4 = UserListFragment.newInstance(UserListFragment.AOZORA_STAFF);
                    fragmentTransaction.add(R.id.flContent, fragment4).addToBackStack(null).commitAllowingStateLoss();
                    break;
            }
        }
        if(selectedList == AoConstants.REPUTATION_RANKS_OPTIONS_DIALOG) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            switch (selectedOption){
                case AoConstants.REPUTATION_RANKS_TOP500:
                    ReputationRankFragment fragment = ReputationRankFragment.newInstance(user,ReputationRankFragment.TOP_500);
                    fragmentTransaction.add(R.id.flContent, fragment).addToBackStack(null).commitAllowingStateLoss();
                    break;
                case AoConstants.REPUTATION_RANKS_FOLLOWING:
                    ReputationRankFragment fragment1 = ReputationRankFragment.newInstance(user,ReputationRankFragment.FRIENDS);
                    fragmentTransaction.add(R.id.flContent, fragment1).addToBackStack(null).commitAllowingStateLoss();
                    break;
            }
        }
    }

    @Override
    public void onUsernameTapped(ParseUser userTapped) {
        if(!AoUtils.isActivityInvalid(getActivity())) {
            if(!user.getObjectId().equals(userTapped.getObjectId())) {
                rvTimeline.setVisibility(View.GONE);
                ProfileFragment pf = null;
                if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                    pf = ProfileFragment.newInstance(userTapped, true, true,null,hasMenu);
                else
                    pf = ProfileFragment.newInstance(userTapped, true, false,null,hasMenu);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.flContent, pf).addToBackStack(null).commitAllowingStateLoss();
            }
        }
    }

    public void scrollFeedToStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rvTimeline.scrollToPosition(0);
            }
        }, 200);
    }

    public void scrollProfileToStart() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0,0);
            }
        }, 200);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    TimelinePost selectedPost = null;
    int selectedPosition = -1;

    @Override
    public void onMoreOptionsTappedCallback(TimelinePost post) {
        selectedPost = AoUtils.GetOriginalPost(post);
        this.selectedPosition = AoUtils.getPositionOfTimelinePost(lstTimelinePost,post);
        OptionListDialogFragment fragment = AoUtils.getDialogFragmentMoreOptions(AoUtils.GetOriginalPoster(post),getActivity(),this,null,false);
        fragment.setCancelable(true);
        fragment.show(getFragmentManager(),"");
    }

    @Override
    public void onDeletePost() {
        if (simpleLoadingDialogFragment.isVisible())
            simpleLoadingDialogFragment.dismissAllowingStateLoss();
        lstTimelinePost.remove(selectedPosition);
        timelineAdapter.notifyItemRemoved(selectedPosition);
    }

    int OPEN_TIMELINE_POST = 100;

    @Override
    public void onItemTappedListener(TimelinePost post, int position) {
        selectedPosition = position;
        selectedPost = post;
        AozoraForumsApp.setTimelinePostToPass(post);
        Intent i = new Intent(getActivity(), TimelinePostActivity.class);
        startActivityForResult(i,OPEN_TIMELINE_POST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == OPEN_TIMELINE_POST) {
            if(resultCode == TimelinePostActivity.PARENT_POST_DELETED)
                onDeletePost();
        } else if (requestCode == REQUEST_NEW_TIMELINEPOST && resultCode == getActivity().RESULT_OK) {
            reloadPosts(true);
        }else if (requestCode == REQUEST_NEW_TIMELINEPOST_REPLY && resultCode == getActivity().RESULT_OK) {
            TimelinePost post = (TimelinePost)AozoraForumsApp.getUpdatedParentPost();
            int position = AoUtils.getPositionOfTimelinePost(lstTimelinePost,post);
            lstTimelinePost.set(position, post);
            timelineAdapter.notifyItemChanged(position, post);
        }

    }

    @Override
    public void onLikeTappedListener(TimelinePost post) {
        ParseObject newPost = PostUtils.likePost(post);
        if(newPost != null) {
            int position = AoUtils.getPositionOfTimelinePost(lstTimelinePost,post);
            lstTimelinePost.set(position, (TimelinePost) newPost);
            timelineAdapter.notifyItemChanged(position, newPost);
        }
    }

    @Override
    public void onRepostTappedListener(TimelinePost post) {
            ArrayList<ParseObject> repost = PostUtils.repostPost(post);
        if(isProfile) {
            if(repost.size()==1) {
                //Se borro el repost
                int position = AoUtils.getPositionOfTimelinePost(lstTimelinePost,post);
                lstTimelinePost.remove(position);
                timelineAdapter.notifyItemRemoved(position);
            } else {
                //Reemplazamos para que se actualize icono de repost
                int position = AoUtils.getPositionOfTimelinePost(lstTimelinePost,post);
                lstTimelinePost.set(position,(TimelinePost)repost.get(0));
                timelineAdapter.notifyItemChanged(position,repost.get(0));
                //Agregamos post al comienzo
                lstTimelinePost.add(0,(TimelinePost) repost.get(1));
                if(repost.get(1).getObjectId() != null)
                    timelineAdapter.notifyItemInserted(0);
            }
        } else {
            int position = AoUtils.getPositionOfTimelinePost(lstTimelinePost,post);
            lstTimelinePost.set(position,(TimelinePost)repost.get(0));
            timelineAdapter.notifyItemChanged(position,repost.get(0));
        }
    }


    @Override
    public void mShareCallback(View view) {

        boolean hasPermission = (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            viewToShare = view;
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            AoUtils.ShareImageFromView(view, getActivity());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AoUtils.ShareImageFromView(viewToShare, getActivity());
                } else {
                    Toast.makeText(getActivity(), "The app was not allowed to write to your storage. Hence, it cannot share the image. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    @Override
    public void onCommentTapped(TimelinePost post) {

        selectedPost = null;
        Intent i = new Intent(getActivity(),CreatePostActivity.class);
        i.putExtra(CreatePostActivity.PARAM_TYPE,CreatePostActivity.NEW_TIMELINEPOST_REPLY);
        AozoraForumsApp.setPostedBy(ParseUser.getCurrentUser());
        AozoraForumsApp.setPostedIn(user);
        AozoraForumsApp.setUpdatedParentPost(post);
        startActivityForResult(i,REQUEST_NEW_TIMELINEPOST_REPLY);
    }
}
