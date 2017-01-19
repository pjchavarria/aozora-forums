package com.everfox.aozoraforums.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.fragments.ForumsFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.utils.AoUtils;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    Button btnLogout;
    TextView btnForum, btnNotifications, btnProfile;
    FrameLayout flContent;
    Integer selectedFragmentIndex = 0;
    ProfileFragment pf;
    ForumsFragment ff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnForum = (TextView) findViewById(R.id.btnForum);
        btnNotifications = (TextView) findViewById(R.id.btnNotifications);
        btnProfile = (TextView) findViewById(R.id.btnProfile);
        btnProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (selectedFragmentIndex != 2 ) {
                    selectedFragmentIndex = 2;
                    //cambiamos el icono del boton a "selected"

                    //Cargamos AnimeInfoFragment
                    OpenProfileFragment();
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

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
                if (currentFragment instanceof ProfileFragment) {
                    currentFragment.onResume();
                }
            }
        });
    }

    private void OpenForumFragment() {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {

            if (ff == null)
                ff = ForumsFragment.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, ff).commitAllowingStateLoss();
        }
    }

    private void OpenProfileFragment() {
        if(!AoUtils.isActivityInvalid(MainActivity.this)) {

            if (pf == null)
                pf = ProfileFragment.newInstance(ParseUser.getCurrentUser(),true,true);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.flContent, pf).commitAllowingStateLoss();
        }
    }


}
