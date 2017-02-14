package com.everfox.aozoraforums.activities;

import android.os.Bundle;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer.Provider;


/**
 * Created by daniel.soto on 1/20/2017.
 */

public class AoTubeActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener
{

    public static String YOUTUBEID_PARAM = "youtubeID";
    static private final String DEVELOPER_KEY = "AIzaSyCFrGEa6TyMq6yv49LIqRwjAN-Vbqxq2r0";
    String youtubeID = "";

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_aotube);
        YouTubePlayerView youTubeView = (YouTubePlayerView) findViewById(R.id.ypvYoutube);
        youTubeView.initialize(DEVELOPER_KEY, this);
        youtubeID = getIntent().getExtras().getString(YOUTUBEID_PARAM);
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer youTubePlayer, boolean b) {
        youTubePlayer.loadVideo(youtubeID);
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        finish();
        Toast.makeText(this,"Can't play the video, verify you have the Youtube App installed",Toast.LENGTH_SHORT).show();
    }
}
