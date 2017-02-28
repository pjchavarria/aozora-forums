package com.everfox.aozoraforums.activities.postthread;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.activities.SearchActivity;
import com.everfox.aozoraforums.adapters.SearchImageAdapter;
import com.everfox.aozoraforums.controllers.SearchImageHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/28/2017.
 */

public class SearchImageActivity extends AozoraActivity implements SearchImageHelper.OnGetSearchGifsListener, SearchImageHelper.OnGetSearchImagesListener{

    SearchImageHelper searchImageHelper;
    MenuItem searchMenuItem;
    SearchImageAdapter searchImageAdapter;
    private Date _lastTypeTime;
    SearchView searchView;
    int selectedSearchIndex = 0;
    int milisecondsToSearch = 700;
    String currentSearch;
    GridLayoutManager glm;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvSearchResults)
    RecyclerView rvSearchResults;
    @BindView(R.id.tvSearchGifs)
    TextView tvSearchGifs;
    @BindView(R.id.tvSearchImages)
    TextView tvSearchImages;
    @BindView(R.id.tool_bar)
    Toolbar tool_bar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchimage);
        ButterKnife.bind(this);
        tool_bar.setTitle("Search");
        setSupportActionBar(tool_bar);
        glm = new GridLayoutManager(this,2);
        searchImageAdapter = new SearchImageAdapter(this,new ArrayList<String>(),true);
        rvSearchResults.setAdapter(searchImageAdapter);
        tvSearchImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedSearchIndex != 0) {
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    selectedSearchIndex = 0;
                    tvSearchImages.setBackgroundColor(Color.BLACK);
                    tvSearchImages.setTextColor(Color.WHITE);
                    tvSearchGifs.setBackgroundColor(Color.WHITE);
                    tvSearchGifs.setTextColor(ContextCompat.getColor(SearchImageActivity.this,R.color.gray3C));
                }
            }
        });
        tvSearchGifs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedSearchIndex != 1) {
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    selectedSearchIndex = 1;
                    tvSearchGifs.setBackgroundColor(Color.BLACK);
                    tvSearchGifs.setTextColor(Color.WHITE);
                    tvSearchImages.setBackgroundColor(Color.WHITE);
                    tvSearchImages.setTextColor(ContextCompat.getColor(SearchImageActivity.this,R.color.gray3C));
                }
            }
        });
        searchImageHelper = new SearchImageHelper(this,this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_userthread, menu);
        searchMenuItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        final EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(Color.WHITE);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                _lastTypeTime = new Date();
            }

            @Override
            public void onTextChanged(final CharSequence charSequence, int i, int i1, int i2) {
                Timer t = new Timer();
                TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                        Date myRunTime = new Date();
                        if ((_lastTypeTime.getTime() + milisecondsToSearch) <= myRunTime.getTime()) {
                            searchEditText.post(new Runnable() {
                                @Override
                                public void run() {
                                    performSearch(charSequence.toString());
                                }
                            });
                        }
                    }
                };
                t.schedule(tt, milisecondsToSearch);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

        });

        return super.onCreateOptionsMenu(menu);
    }

    private void performSearch(String newText) {

        Boolean isImage = selectedSearchIndex == 0;
        if(newText == ""){
            searchImageAdapter = new SearchImageAdapter(this,new ArrayList<String>(),isImage);
            rvSearchResults.setAdapter(searchImageAdapter);
        } else {
            if (newText.length() > 2) {
                if(isImage)
                    searchImageHelper.SearchImages(newText);
                else
                    searchImageHelper.SearchGifs(newText);
                currentSearch = newText;
                pbLoading.setVisibility(View.VISIBLE);
                rvSearchResults.setVisibility(View.GONE);
            }
        }
    }


    @Override
    public void onGetSearchGifsListener(List<String> results) {

        pbLoading.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        searchImageAdapter = new SearchImageAdapter(this,new ArrayList<>(results),false);
    }

    @Override
    public void onGetSearchImagesListener(List<String> results) {

        pbLoading.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        searchImageAdapter = new SearchImageAdapter(this,new ArrayList<>(results),true);
    }
}
