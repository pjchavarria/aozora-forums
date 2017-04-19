package com.everfox.aozoraforums.activities.postthread;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.activities.SearchActivity;
import com.everfox.aozoraforums.adapters.SearchImageAdapter;
import com.everfox.aozoraforums.controllers.SearchImageHelper;
import com.everfox.aozoraforums.controls.AoGridLayoutManager;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.utils.RecyclerItemClickListener;

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

public class SearchImageActivity extends AozoraActivity implements SearchImageHelper.OnGetSearchGifsListener, SearchImageHelper.OnGetSearchImagesListener,
SearchImageAdapter.OnItemClickListener{

    public static int RESULT_SUCCESS = 500;
    public static String IMAGE_DATA = "IMAGE_DATA";
    List<ImageData> lstImageData = new ArrayList<>();
    SearchImageHelper searchImageHelper;
    MenuItem searchMenuItem;
    SearchImageAdapter searchImageAdapter;
    private Date _lastTypeTime;
    SearchView searchView;
    int selectedSearchIndex = 0;
    int milisecondsToSearch = 750;
    String currentSearch;
    AoGridLayoutManager imagesLayoutManager;
    AoLinearLayoutManager gifLayoutManager;
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
    Boolean isSearching = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchimage);
        ButterKnife.bind(this);
        tool_bar.setTitle("Search");
        setSupportActionBar(tool_bar);
        imagesLayoutManager = new AoGridLayoutManager(this,2);
        gifLayoutManager = new AoLinearLayoutManager(this);
        rvSearchResults.setLayoutManager(imagesLayoutManager);
        searchImageAdapter = new SearchImageAdapter(this,new ArrayList<ImageData>(),true,this);
        rvSearchResults.setAdapter(searchImageAdapter);
        tvSearchImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSearching) {
                if (selectedSearchIndex != 0) {
                    selectedSearchIndex = 0;
                    tvSearchGifs.setBackgroundColor(Color.BLACK);
                    tvSearchGifs.setTextColor(Color.WHITE);
                    tvSearchImages.setBackgroundColor(Color.WHITE);
                    tvSearchImages.setTextColor(ContextCompat.getColor(SearchImageActivity.this,R.color.gray3C));
                    searchImageAdapter = new SearchImageAdapter(SearchImageActivity.this,new ArrayList<ImageData>(),true,SearchImageActivity.this);
                    rvSearchResults.setLayoutManager(imagesLayoutManager);
                    rvSearchResults.setAdapter(searchImageAdapter);
                    if(!searchView.getQuery().toString().equals("")) {
                        performSearch(searchView.getQuery().toString());
                    } else
                        isSearching = false;
                }
            } else {
                Toast.makeText(SearchImageActivity.this,"Search is in progress", Toast.LENGTH_SHORT).show();
            }
            }
        });
        tvSearchGifs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isSearching) {
                    if (selectedSearchIndex != 1) {
                        selectedSearchIndex = 1;
                        tvSearchImages.setBackgroundColor(Color.BLACK);
                        tvSearchImages.setTextColor(Color.WHITE);
                        tvSearchGifs.setBackgroundColor(Color.WHITE);
                        tvSearchGifs.setTextColor(ContextCompat.getColor(SearchImageActivity.this, R.color.gray3C));
                        searchImageAdapter = new SearchImageAdapter(SearchImageActivity.this, new ArrayList<ImageData>(), true,SearchImageActivity.this);
                        rvSearchResults.setLayoutManager(gifLayoutManager);
                        rvSearchResults.setAdapter(searchImageAdapter);
                        if(!searchView.getQuery().toString().equals("")) {
                            performSearch(searchView.getQuery().toString());
                        } else
                            isSearching = false;

                    }
                } else {
                    Toast.makeText(SearchImageActivity.this,"Search is in progress", Toast.LENGTH_SHORT).show();
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
            searchImageAdapter = new SearchImageAdapter(this,new ArrayList<ImageData>(),isImage,this);
            rvSearchResults.setAdapter(searchImageAdapter);
            isSearching = false;
        } else {
            if(!isSearching) {
                if (newText.length() > 2) {
                    isSearching = true;
                    if (isImage)
                        searchImageHelper.SearchImages(newText);
                    else
                        searchImageHelper.SearchGifs(newText);
                    currentSearch = newText;
                    pbLoading.setVisibility(View.VISIBLE);
                    rvSearchResults.setVisibility(View.GONE);
                }
            }
        }
    }


    @Override
    public void onGetSearchGifsListener(List<ImageData> results) {

        if(isSearching) {
            pbLoading.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            searchImageAdapter = new SearchImageAdapter(this, new ArrayList<>(results), false,this);
            rvSearchResults.setAdapter(searchImageAdapter);
            lstImageData.addAll(results);
        }
        isSearching = false;
    }

    @Override
    public void onGetSearchImagesListener(List<ImageData> results) {

        if(isSearching) {
            pbLoading.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            searchImageAdapter = new SearchImageAdapter(this, new ArrayList<>(results), true,this);
            rvSearchResults.setAdapter(searchImageAdapter);
            lstImageData.addAll(results);
        }
        isSearching = false;
    }

    @Override
    public void onItemClicked(ImageData image) {
        Intent intent = new Intent();
        intent.putExtra(IMAGE_DATA,image);
        setResult(RESULT_SUCCESS,intent);
        finish();
    }
}
