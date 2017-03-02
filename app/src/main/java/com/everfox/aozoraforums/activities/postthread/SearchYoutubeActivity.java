package com.everfox.aozoraforums.activities.postthread;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 3/2/2017.
 */

public class SearchYoutubeActivity extends AozoraActivity {

    String youtubeURL = "https://m.youtube.com";

    public static String YOUTUBE_ID = "YOUTUBE_ID";

    @BindView(R.id.wvYoutube)
    WebView wvYoutube;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchyoutube);
        ButterKnife.bind(this);

        WebSettings settings = wvYoutube.getSettings();
        settings.setJavaScriptEnabled(true);
        setTitle("Select a video");
        wvYoutube.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
            }


        });


        wvYoutube.loadUrl(youtubeURL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchyoutube_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.searchyoutube:
                if (wvYoutube.getUrl().contains("watch?v=")) {
                    Uri youtubeUri = Uri.parse(wvYoutube.getUrl());
                    String youtubeID = youtubeUri.getQueryParameter("v");
                    Intent result = new Intent();
                    result.putExtra(YOUTUBE_ID,youtubeID);
                    setResult(RESULT_OK,result);
                }
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
