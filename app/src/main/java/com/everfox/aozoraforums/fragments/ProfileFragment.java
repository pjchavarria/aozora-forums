package com.everfox.aozoraforums.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
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

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.ProfileTimelineAdapter;
import com.everfox.aozoraforums.controllers.ProfileParseHelper;
import com.everfox.aozoraforums.controls.EndlessScrollView;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.ProfileUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class ProfileFragment extends Fragment implements ProfileParseHelper.OnGetProfilePostsListener, EndlessScrollView.EndlessScrollListener {

    ParseUser user;
    UserDetails userDetails;
    Boolean isProfile;
    Boolean isCurrentUser;
    ProfileTimelineAdapter timelineAdapter;

    @BindView(R.id.pbLoading) ProgressBar pbLoading;
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
    @BindView(R.id.rvTimeline)
    RecyclerView rvTimeline;
    @BindView(R.id.scrollView)
    EndlessScrollView scrollView;

    public static ProfileFragment newInstance(ParseUser user,Boolean isProfile, Boolean isCurrentUser) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.user = user;
        fragment.isProfile = isProfile;
        fragment.isCurrentUser = isCurrentUser;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    LinearLayoutManager llm;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getString(ParseUserColumns.AOZORA_USERNAME));
        ButterKnife.bind(this,view);
        llm = new LinearLayoutManager(getActivity());
        rvTimeline.setLayoutManager(llm);
        timelineAdapter = new ProfileTimelineAdapter(getActivity(),new ArrayList<TimelinePost>(),ProfileFragment.this,user);
        rvTimeline.setAdapter(timelineAdapter);
        scrollView.setScrollViewListener(this);
        pbLoading.setVisibility(View.VISIBLE);
        llProfileContent.setVisibility(View.GONE);
        ivProfileBanner.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        ParseFile bannerPic = user.getParseFile(ParseUserColumns.BANNER);
        loadAvatarAndBanner(profilePic,bannerPic);
        tvPopularity.setText(AoUtils.numberToStringOrZero(user.getNumber(ParseUserColumns.REPUTATION)));
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


        if(!isProfile) {
            llFeed.setVisibility(View.VISIBLE);
            //Load Following

        } else {
            //LoadProfile
            new ProfileParseHelper(getActivity(),ProfileFragment.this)
                    .GetProfilePosts(user,0,ProfileParseHelper.PROFILE_FETCH_LIMIT,ProfileParseHelper.PROFILE_LIST);

        }

    }

    private void loadUserDetails() {
        tvFollowers.setText(AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.FOLLOWERS)));
        tvFollowing.setText(AoUtils.numberToStringOrZero(userDetails.getNumber(UserDetails.FOLLOWING)));
        tvIntroduction.setText(userDetails.getString(UserDetails.ABOUT));
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
    public void onGetProfilePosts(final List<TimelinePost> timelinePosts) {

        timelineAdapter = new ProfileTimelineAdapter(getActivity(),timelinePosts,ProfileFragment.this,user);
        rvTimeline.setAdapter(timelineAdapter);

        rvTimeline.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = llm.getChildCount();
                int totalItemCount = llm.getItemCount();
                int firstVisibleItems;
                firstVisibleItems = llm.findFirstVisibleItemPosition()  ;
                int pastVisibleItems = 0;
                pastVisibleItems = firstVisibleItems;


                    if ((visibleItemCount + pastVisibleItems) >= totalItemCount
                            && pastVisibleItems >= 0) {
                       AoUtils.fromHtml("ass");
                    }
            }
        });

        pbLoading.setVisibility(View.GONE);
        llProfileContent.setVisibility(View.VISIBLE);
        ivProfileBanner.setVisibility(View.VISIBLE);


    }

    @Override
    public void onScrollChanged(EndlessScrollView scrollView, int x, int y, int oldx, int oldy) {
        // We take the last son in the scrollview
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        int distanceToEnd = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));

        // if diff is zero, then the bottom has been reached
        if (distanceToEnd == 0) {
            AoUtils.fromHtml("ass");

            // do stuff your load more stuff

        }
    }
}
