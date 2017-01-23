package com.everfox.aozoraforums.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.controllers.FriendsController;
import com.everfox.aozoraforums.fragments.ForumsFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.utils.AoUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    Button btnLogout;
    TextView btnForum, btnNotifications, btnProfile, btnFeed;
    FrameLayout flContent;
    Integer selectedFragmentIndex = 0;
    ProfileFragment profileFragment;
    ForumsFragment forumsFragmentf;
    ProfileFragment feedFragment;
    LinearLayout llNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        llNavBar = (LinearLayout) findViewById(R.id.llNavBar);
        btnFeed = (TextView) findViewById(R.id.btnFeed);
        btnForum = (TextView) findViewById(R.id.btnForum);
        btnNotifications = (TextView) findViewById(R.id.btnNotifications);
        btnProfile = (TextView) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (selectedFragmentIndex != 3 ) {
                    selectedFragmentIndex = 3;
                    //cambiamos el icono del boton a "selected"

                    //Cargamos AnimeInfoFragment
                    OpenProfileFragment(ParseUser.getCurrentUser());
                }
            }
        });
        btnForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFragmentIndex != 0 ) {
                    selectedFragmentIndex = 0;
                    //cambiamos el icono del boton a "selected"

                    //Cargamos AnimeInfoFragment
                    OpenForumFragment();
                }
            }
        });
        btnFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedFragmentIndex != 2 ) {
                    selectedFragmentIndex = 2;
                    //cambiamos el icono del boton a "selected"

                    //Cargamos AnimeInfoFragment
                    OpenFeedFragment(ParseUser.getCurrentUser());
                }
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
                if (currentFragment instanceof ProfileFragment) {
                    currentFragment.onResume();
                }
            }
        });

        if(AozoraForumsApp.getProfileToPass()!= null) {
            llNavBar.setVisibility(View.GONE);
            OpenProfileFragment(AozoraForumsApp.getProfileToPass());
            AozoraForumsApp.setProfileToPass(null);
        }
        FriendsController.fetchFollowing();
    }



    private void OpenFeedFragment(ParseUser user) {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {
            if (feedFragment == null)
                feedFragment = ProfileFragment.newInstance(user, false, true);
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
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, forumsFragmentf).commitAllowingStateLoss();
        }
    }

    private void OpenProfileFragment(ParseUser user) {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {
            if (profileFragment == null)
                if(ParseUser.getCurrentUser().getObjectId().equals(user.getObjectId()))
                    profileFragment = ProfileFragment.newInstance(user, true, true);
                else
                    profileFragment = ProfileFragment.newInstance(user, true, false);
            else
                profileFragment.scrollProfileToStart();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, profileFragment).commitAllowingStateLoss();
        }
    }


}
