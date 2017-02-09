package com.everfox.aozoraforums.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.FirstActivity;
import com.everfox.aozoraforums.R;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AppCompatActivity {

    String AOTRACKING_PACKAGE ="com.everfox.animetrackerandroid";
    String AODISCOVER_PACKAGE ="";
    String AOTALK_PACKAGE ="";
    SharedPreferences sharedPreferences;
    public static String FACEBOOK_URL = "https://m.facebook.com/AozoraApp";
    public static String FACEBOOK_PAGE_ID = "713541968752502";

    @BindView(R.id.tvVersion)
    TextView tvVersion;
    @BindView(R.id.tvLogout)
    TextView tvLogout;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.tvRemoveAds)
    TextView tvRemoveAds;
    @BindView(R.id.tvChangeUsername)
    TextView tvChangeUsername;
    @BindView(R.id.tvRate)
    TextView tvRate;
    @BindView(R.id.tvAotracking)
    TextView tvAotracking;
    @BindView(R.id.tvAoTrackingWhat)
    TextView tvAoTrackingWhat;
    @BindView(R.id.tvAoDiscover)
    TextView tvAoDiscover;
    @BindView(R.id.tvAoDiscoverWhat)
    TextView tvAoDiscoverWhat;
    @BindView(R.id.tvAoTalk)
    TextView tvAoTalk;
    @BindView(R.id.tvAoTalkWhat)
    TextView tvAoTalkWhat;
    @BindView(R.id.tvLikeFacebook)
    TextView tvLikeFacebook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setTitle("Settings");
        Typeface typeface = AozoraForumsApp.getAwesomeTypeface();
        tvAoDiscoverWhat.setTypeface(typeface);
        tvAoTalkWhat.setTypeface(typeface);
        tvAoTrackingWhat.setTypeface(typeface);

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    ParseObject.unpinAll();
                } catch (ParseException pEx) {
                }

                sharedPreferences = getSharedPreferences("com.everfox.aozoraforums", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("MAL_User").apply();
                sharedPreferences.edit().remove("MAL_Password").apply();
                AozoraForumsApp.cleanValues();
                ParseUser.logOut();
                Intent intent = new Intent(SettingsActivity.this, FirstActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        try {
            tvVersion.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        
        tvRemoveAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeAdsClicked();
            }
        });

        tvChangeUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUsernameClicked();
            }
        });

        tvRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMarket(getPackageName());
            }
        });

        tvAotracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appRowClicked(AOTRACKING_PACKAGE);
            }
        });

        tvAoTrackingWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWithText(R.string.aotracking_desc);
            }
        });

        tvAoDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appRowClicked(AODISCOVER_PACKAGE);
            }
        });

        tvAoDiscoverWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWithText(R.string.aodiscover_desc);
            }
        });

        tvAoTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appRowClicked(AOTALK_PACKAGE);
            }
        });

        tvAoTalkWhat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialogWithText(R.string.app_inconstruction);
            }
        });


        tvLikeFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW);
                facebookIntent.setData(Uri.parse(FACEBOOK_URL));
                startActivity(facebookIntent);
            }
        });
    }

    private void appRowClicked(String packageName) {

        if(appInstalledOrNot(packageName)) {
            runApp(packageName);
        } else {
            goToMarket(packageName);
        }
    }


    private void changeUsernameClicked() {

        //#kvn93willdoit
    }

    private void removeAdsClicked() {

        //#kvn93willdoit
    }

    private void showDialogWithText(int stringID) {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage(getString(stringID));
        builder1.setCancelable(true);
        builder1.create().show();
    }

    private boolean appInstalledOrNot(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }

    private void goToMarket(String packageName) {

        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(SettingsActivity.this,"Please try again later", Toast.LENGTH_SHORT).show();
        }
    }

    private void runApp(String packageName) {

        Intent LaunchIntent = getPackageManager()
                .getLaunchIntentForPackage(packageName);
        startActivity(LaunchIntent);
    }

}
