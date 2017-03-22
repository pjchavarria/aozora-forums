package com.everfox.aozoraforums.activities.postthread;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.adapters.SelectTagAdapter;
import com.everfox.aozoraforums.controllers.ThreadTagHelper;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.models.Anime;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.utils.AoConstants;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 3/13/2017.
 */

public class SelectTagActivity extends AozoraActivity implements ThreadTagHelper.OnGetSearchAnimeListener, ThreadTagHelper.OnGetSearchTagsListener,
SelectTagAdapter.OnItemTappedListener{

    AoLinearLayoutManager linearLayoutManager;

    MenuItem searchMenuItem;
    @BindView(R.id.tool_bar)
    Toolbar tool_bar;
    @BindView(R.id.llTagForums)
    LinearLayout llTagForums;
    @BindView(R.id.tvAoGur)
    TextView tvAoGur;
    @BindView(R.id.tvAoTalk)
    TextView tvAoTalk;
    @BindView(R.id.tvAoArt)
    TextView tvAoArt;
    @BindView(R.id.tvAoNews)
    TextView tvAoNews;
    @BindView(R.id.tvForumDescription)
    TextView tvForumDescription;
    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.rvSearchResults)
    RecyclerView rvSearchResults;

    List<AoThreadTag> lstTags = new ArrayList<>();
    Boolean isSearching = false;
    private Date _lastTypeTime;
    SearchView searchView;
    int selectedSearchIndex = 0;
    int milisecondsToSearch = 500;
    String currentSearch;
    SelectTagAdapter selectTagAdapter;
    ThreadTagHelper threadTagHelper;
    Boolean isAdmin = false;
    private String selectedForum;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_tag);
        ButterKnife.bind(this);
        tool_bar.setTitle("Search an anime to talk about");
        setSupportActionBar(tool_bar);
        linearLayoutManager= new AoLinearLayoutManager(this);
        rvSearchResults.setLayoutManager(linearLayoutManager);
        selectTagAdapter = new SelectTagAdapter(this,new ArrayList<AoThreadTag>(),null,this);
        rvSearchResults.setAdapter(selectTagAdapter);
        tvAoGur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               searchTags(tvAoGur,AoConstants.AOGUR,R.string.aogur_desc,0);
            }
        });
        tvAoTalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTags(tvAoTalk,AoConstants.AOTALK,R.string.aotalkforum_desc,1);
            }
        });
        tvAoArt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTags(tvAoArt,AoConstants.AOART,R.string.aoart_desc,2);
            }
        });
        tvAoNews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchTags(tvAoNews,AoConstants.AONEWS,R.string.aonews_desc,3);
            }
        });
        threadTagHelper = new ThreadTagHelper(this,this);
        if(AozoraForumsApp.getIsAdmin(ParseUser.getCurrentUser())>0)
            isAdmin = true;
        selectedForum = AoConstants.AOGUR;
        threadTagHelper.SearchTags(isAdmin);
        isSearching = true;
        tvForumDescription.setText(getString(R.string.aogur_desc));

    }

    private void searchTags(TextView selectedTextView,String forum, int forumDescResID,int searchIndex) {
        if(!isSearching){
            if(selectedSearchIndex != searchIndex) {
                selectedSearchIndex = searchIndex;
                clearAllForums(true);
                selectedTextView.setBackgroundColor(Color.WHITE);
                selectedTextView.setTextColor(ContextCompat.getColor(SelectTagActivity.this,R.color.gray3C));
                tvForumDescription.setText(getString(forumDescResID));
                selectedForum = forum;
                filterTags();
            }
        } else {
            Toast.makeText(SelectTagActivity.this,"Search is in progress", Toast.LENGTH_SHORT).show();
        }
    }

    private void filterTags() {

        ArrayList<AoThreadTag> lstFilteredTags = new ArrayList<>();
        for(int i=0;i<lstTags.size();i++) {
            if(lstTags.get(i).getString(AoThreadTag.SUB_TYPE).equals(selectedForum))
                lstFilteredTags.add(lstTags.get(i));
        }
        selectTagAdapter = new SelectTagAdapter(this,lstFilteredTags,null,this);
        rvSearchResults.setAdapter(selectTagAdapter);

    }

    private void clearAllForums(Boolean clearSearch){
        tvAoArt.setBackgroundColor(Color.BLACK);
        tvAoArt.setTextColor(Color.WHITE);
        tvAoGur.setBackgroundColor(Color.BLACK);
        tvAoGur.setTextColor(Color.WHITE);
        tvAoNews.setBackgroundColor(Color.BLACK);
        tvAoNews.setTextColor(Color.WHITE);
        tvAoTalk.setBackgroundColor(Color.BLACK);
        tvAoTalk.setTextColor(Color.WHITE);
        if(clearSearch) {
            searchView.setIconified(true);
            searchView.setQuery("", false);
        }
    }

    @Override
    public void onGetSearchAnimeCallback(List<Anime> results) {

        if(isSearching) {
            pbLoading.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            selectTagAdapter = new SelectTagAdapter(this,null,results,this);
            rvSearchResults.setAdapter(selectTagAdapter);
        }
        isSearching = false;
    }

    @Override
    public void onGetSearchTagsListener(List<AoThreadTag> results) {
        if(isSearching) {
            pbLoading.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            lstTags = results;
            filterTags();
        }
        isSearching = false;
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


    private void performSearch(String newText){
        if(newText != "") {
            if(!isSearching) {
                if (newText.length() > 3){
                    pbLoading.setVisibility(View.VISIBLE);
                    rvSearchResults.setVisibility(View.GONE);
                    isSearching = true;
                    clearAllForums(false);
                    tvAoTalk.setBackgroundColor(Color.WHITE);
                    tvAoTalk.setTextColor(ContextCompat.getColor(SelectTagActivity.this,R.color.gray3C));
                    selectedSearchIndex = 1;
                    tvForumDescription.setText(getString(R.string.aotalk_desc));
                    threadTagHelper.SearchAnimes(newText);
                }
            }
        }
    }

    @Override
    public void mOnItemTappedListener(ParseObject itemTapped) {

        AozoraForumsApp.setTagToPass(itemTapped);
        setResult(RESULT_OK);
        finish();
    }
}
