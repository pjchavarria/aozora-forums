package com.everfox.aozoraforums.activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.controllers.FriendsController;
import com.everfox.aozoraforums.fragments.FollowersFragment;
import com.everfox.aozoraforums.fragments.ForumsFragment;
import com.everfox.aozoraforums.fragments.NotificationsFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.fragments.UserListFragment;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PurchaseUtils;
import com.facebook.common.util.ExceptionWithNoStacktrace;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Date;

public class MainActivity extends AozoraActivity {

    Button btnLogout;
    ImageView btnForum, btnNotifications, btnProfile, btnFeed;
    FrameLayout flContent;
    Integer selectedFragmentIndex = 0;
    ProfileFragment profileFragment;
    ForumsFragment forumsFragmentf;
    ProfileFragment feedFragment;
    NotificationsFragment notificationsFragment;
    LinearLayout llNavBar;
    Boolean hasMenu=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!PurchaseUtils.purchasedProduct(this, PurchaseUtils.PRODUCT_NO_ADS)) {
            AdView mAdView = (AdView) findViewById(R.id.adView);
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
            flContent = (FrameLayout) findViewById(R.id.flContent);
            RelativeLayout.LayoutParams llp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            llp.setMargins(0,0,0,200);
            flContent.setLayoutParams(llp);
        }

        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user",ParseUser.getCurrentUser());
        installation.saveInBackground();
        llNavBar = (LinearLayout) findViewById(R.id.llNavBar);
        btnFeed = (ImageView) findViewById(R.id.btnFeed);
        btnForum = (ImageView) findViewById(R.id.btnForum);
        btnNotifications = (ImageView) findViewById(R.id.btnNotifications);
        btnProfile = (ImageView) findViewById(R.id.btnProfile);
        btnForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFragmentIndex != 0 ) {
                    selectedFragmentIndex = 0;
                    markMenuAsUnselected();
                    btnForum.setColorFilter(ContextCompat.getColor(MainActivity.this,R.color.bluemenu));
                    OpenForumFragment();
                }
            }
        });
        btnNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFragmentIndex != 1 ) {
                    selectedFragmentIndex = 1;
                    markMenuAsUnselected();
                    btnNotifications.setColorFilter(ContextCompat.getColor(MainActivity.this,R.color.bluemenu));
                    OpenNotificationFragment();
                }
            }
        });
        btnFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFragmentIndex != 2 ) {
                    selectedFragmentIndex = 2;
                    markMenuAsUnselected();
                    btnFeed.setColorFilter(ContextCompat.getColor(MainActivity.this,R.color.bluemenu));
                    OpenFeedFragment(ParseUser.getCurrentUser());
                }
            }
        });
        btnProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                if (selectedFragmentIndex != 3 ) {
                    selectedFragmentIndex = 3;
                    markMenuAsUnselected();
                    btnProfile.setColorFilter(ContextCompat.getColor(MainActivity.this,R.color.bluemenu));
                    OpenProfileFragment(ParseUser.getCurrentUser(),true);
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
                if (currentFragment != null && currentFragment instanceof ProfileFragment || currentFragment instanceof FollowersFragment
                        || currentFragment instanceof UserListFragment  || currentFragment instanceof NotificationsFragment) {
                    currentFragment.onResume();
                }
            }
        });

        if(AozoraForumsApp.getProfileToPass()!= null) {
            llNavBar.setVisibility(View.GONE);
            OpenProfileFragment(AozoraForumsApp.getProfileToPass(),false);
            AozoraForumsApp.setProfileToPass(null);
        }
        FriendsController.fetchFollowing();
        OpenForumFragment();
        markMenuAsUnselected();
        btnForum.setColorFilter(ContextCompat.getColor(MainActivity.this,R.color.bluemenu));

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        AozoraForumsApp.setScreenWidth(metrics.widthPixels);
        AozoraForumsApp.setDensity(metrics.density);


    }

    @Override
    protected void onResume() {
        super.onResume();
        createUserDetailsIfMissing();
    }

    private void createUserDetailsIfMissing() {

        if(ParseUser.getCurrentUser().getParseObject("details") == null) {

            final UserDetails userDetails = UserDetails.create(UserDetails.class);
            userDetails.put("joinDate",new Date());
            userDetails.put("gender","Not specified");
            userDetails.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e==null) {
                        ParseUser currentUser = ParseUser.getCurrentUser();
                        currentUser.put("details",userDetails);
                        currentUser.saveInBackground();
                    }
                }
            });
        }
    }

    private void markMenuAsUnselected() {
        int grayColor = ContextCompat.getColor(this,R.color.gray90);
        btnForum.setColorFilter(grayColor);
        btnNotifications.setColorFilter(grayColor);
        btnFeed.setColorFilter(grayColor);
        btnProfile.setColorFilter(grayColor);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Check if fragment is valid?
    }

    private void OpenNotificationFragment() {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {
            if (notificationsFragment == null)
                notificationsFragment = NotificationsFragment.newInstance();
            else
                notificationsFragment.scrollFeedToStart();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, notificationsFragment).commitAllowingStateLoss();
        }
    }

    private void OpenFeedFragment(ParseUser user) {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {
            if (feedFragment == null)
                feedFragment = ProfileFragment.newInstance(user, false, true,null,true);
            else
                feedFragment.scrollFeedToStart();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, feedFragment).commitAllowingStateLoss();
        }
    }

    private void OpenForumFragment() {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {

            if (forumsFragmentf == null)
                forumsFragmentf = ForumsFragment.newInstance();
            else
                forumsFragmentf.scrollThreadsToStart();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, forumsFragmentf).commitAllowingStateLoss();
        }
    }

    private void OpenProfileFragment(ParseUser user,Boolean hasMenu) {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {
            if (profileFragment == null)
                if(ParseUser.getCurrentUser().getObjectId().equals(user.getObjectId()))
                    profileFragment = ProfileFragment.newInstance(user, true, true,null,hasMenu);
                else
                    profileFragment = ProfileFragment.newInstance(user, true, false,null,hasMenu);
            else
                profileFragment.scrollProfileToStart();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, profileFragment).commitAllowingStateLoss();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.search:
                Intent i = new Intent(this,SearchActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
