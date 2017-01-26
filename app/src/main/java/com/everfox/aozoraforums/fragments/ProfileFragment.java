package com.everfox.aozoraforums.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.activities.TimelinePostActivity;
import com.everfox.aozoraforums.adapters.ProfileTimelineAdapter;
import com.everfox.aozoraforums.controllers.PostParseHelper;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.controls.EndlessScrollView;
import com.everfox.aozoraforums.dialogfragments.OptionListDialogFragment;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AoConstants;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.ProfileUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.everfox.aozoraforums.utils.AoUtils.getOptionListFromID;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class ProfileFragment extends Fragment implements ProfileParseHelper.OnGetProfilePostsListener, EndlessScrollView.EndlessScrollListener,
OptionListDialogFragment.OnListSelectedListener, ProfileTimelineAdapter.OnUsernameTappedListener{

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

    public static ProfileFragment newInstance(ParseUser user,Boolean isProfile, Boolean isCurrentUser, String userID) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.user = user;
        fragment.isProfile = isProfile;
        fragment.isCurrentUser = isCurrentUser;
        fragment.userID = userID;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this,view);
        if(isProfile) {
            rvTimeline = (RecyclerView) view.findViewById(R.id.rvTimeline);
        } else {
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
        return view;
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

                scrollView.setVisibility(View.GONE);
            } else {
                //LoadProfile
                loadProfile();
            }

            new ProfileParseHelper(getActivity(), ProfileFragment.this)
                    .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList);
            fetchCount++;
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
                                .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList);
                        fetchCount++;
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
        tvPopularity.setText(AoUtils.numberToStringOrZero(user.getNumber(ParseUserColumns.REPUTATION)));
        if(!isCurrentUser) {
            tvFollow.setVisibility(View.VISIBLE);
            tvFollow.setTypeface(AozoraForumsApp.getAwesomeTypeface());
            //SI LO ESTA SIGIENDO FOLLOWING, SINO FOLLOW

            ParseRelation<ParseObject> relation = ParseUser.getCurrentUser().getRelation(ParseUserColumns.FOLLOWING);
            relation.getQuery().findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(objects.contains(user)) {
                        tvFollow.setText(getString(R.string.fa_check) +  " Following");
                    }else {
                        tvFollow.setText(getString(R.string.fa_plus) + " Follow");
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

        user.getParseObject(ParseUserColumns.DETAILS).fetchIfNeededInBackground(new GetCallback<UserDetails>() {
            @Override
            public void done(UserDetails object, ParseException e) {

                if(object == null) {
                    ParseQuery<UserDetails> queryDetails = ParseQuery.getQuery(UserDetails.class);
                    queryDetails.setLimit(1);
                    queryDetails.whereEqualTo(UserDetails.DETAILS_USER,user);
                    queryDetails.findInBackground(new FindCallback<UserDetails>() {
                        @Override
                        public void done(List<UserDetails> objects, ParseException e) {
                            if(objects != null && e != null && objects.size()>0) {
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
        ivMoreOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = user.getString(ParseUserColumns.AOZORA_USERNAME);
                Date joinDate = user.getDate(ParseUserColumns.JOIN_DATE);
                DateFormat df = new SimpleDateFormat("MMM dd,yyyy");
                String subtitle1 = "Member since: " + df.format(joinDate) ;
                String subtitle2 = "Posts: " + postCount;
                OptionListDialogFragment fragment = OptionListDialogFragment.newInstance(getActivity(),title,subtitle1,subtitle2,ProfileFragment.this, AoConstants.MY_PROFILE_OPTIONS_DIALOG);
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
                .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList);
        fetchCount++;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(isProfile)
            rvTimeline.setVisibility(View.VISIBLE);
        if(user != null)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getString(ParseUserColumns.AOZORA_USERNAME));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void loadUserDetails() {
        tvFollowers.setText(AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.FOLLOWERS)));
        tvFollowing.setText(AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.FOLLOWING)));
        tvIntroduction.setText(userDetails.getString(UserDetails.ABOUT));
        postCount = AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.POSTS));
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

        if(timelineAdapter.getItemCount() == 0) {
            _timelinePosts = AoUtils.clearTimelinePostDuplicates(new ArrayList<>(_timelinePosts));
            lstTimelinePost.addAll(_timelinePosts);
            timelineAdapter = new ProfileTimelineAdapter(getActivity(), lstTimelinePost, ProfileFragment.this, ParseUser.getCurrentUser());
            rvTimeline.setAdapter(timelineAdapter);
            pbLoading.setVisibility(View.GONE);
            rvTimeline.setVisibility(View.VISIBLE);
            if(isProfile) {
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
                .GetProfilePosts(user, fetchCount * ProfileParseHelper.PROFILE_FETCH_LIMIT, ProfileParseHelper.PROFILE_FETCH_LIMIT, selectedList);
        fetchCount++;
    }

    @Override
    public void onListSelected(Integer list, Integer selectedList) {

        List<String> optionList = AoUtils.getOptionListFromID(getActivity(),selectedList);
        String selectedOption = optionList.get(list);
        if(selectedList == AoConstants.MY_PROFILE_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.MY_PROFILE_DISCOVER:
                    OptionListDialogFragment fUserList = OptionListDialogFragment.newInstance(getActivity(),"User Lists","Find fellow anime fans",null,ProfileFragment.this, AoConstants.USER_LIST_OPTIONS_DIALOG);
                    fUserList.setCancelable(true);
                    fUserList.show(getFragmentManager(),"");
                    break;
                case AoConstants.MY_PROFILE_REPUTATION:
                    OptionListDialogFragment fRep = OptionListDialogFragment.newInstance(getActivity(),null,null,null,ProfileFragment.this, AoConstants.REPUTATION_RANKS_OPTIONS_DIALOG);
                    fRep.setCancelable(true);
                    fRep.show(getFragmentManager(),"");
                    break;
                case AoConstants.MY_PROFILE_EDIT:
                    break;
                case AoConstants.MY_PROFILE_THREADS:
                    break;
            }
        }
        if(selectedList == AoConstants.ADMIN_POST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.ADMIN_POST_DELETE:
                    break;
                case AoConstants.ADMIN_POST_EDIT:
                    break;
            }
        }
        if(selectedList == AoConstants.USER_LIST_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.USER_LIST_FOLLOW:
                    break;
                case AoConstants.USER_LIST_NEW:
                    break;
                case AoConstants.USER_LIST_NEWPRO:
                    break;
                case AoConstants.USER_LIST_OLDESTACTIVE:
                    break;
                case AoConstants.USER_LIST_ONLINE:
                    break;
                case AoConstants.USER_LIST_STAFF:
                    break;
            }
        }
        if(selectedList == AoConstants.REPUTATION_RANKS_OPTIONS_DIALOG) {
            switch (selectedOption){
                case AoConstants.REPUTATION_RANKS_TOP500:
                    break;
                case AoConstants.REPUTATION_RANKS_FOLLOWING:
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
                    pf = ProfileFragment.newInstance(userTapped, true, true,null);
                else
                    pf = ProfileFragment.newInstance(userTapped, true, false,null);
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
}
