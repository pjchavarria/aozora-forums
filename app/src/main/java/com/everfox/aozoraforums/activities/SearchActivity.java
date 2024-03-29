package com.everfox.aozoraforums.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.adapters.SearchResultsThreadAdapter;
import com.everfox.aozoraforums.adapters.SearchResultsUserAdapter;
import com.everfox.aozoraforums.controllers.SearchHelper;
import com.everfox.aozoraforums.controls.AoLinearLayoutManager;
import com.everfox.aozoraforums.fragments.FollowersFragment;
import com.everfox.aozoraforums.fragments.ProfileFragment;
import com.everfox.aozoraforums.fragments.UserListFragment;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/7/2017.
 */

public class SearchActivity extends AozoraActivity implements SearchHelper.OnGetSearchThreadsListener, SearchHelper.OnGetSearchUsersListener,
SearchResultsUserAdapter.OnUsernameTappedListener, SearchHelper.OnGetSearchPopularThreadsListener{

    ArrayList<AoThread> popularThreads;
    int milisecondsToSearch = 500;
    int selectedSearchIndex = 0;
    MenuItem searchMenuItem;
    SearchView searchView;
    private Date _lastTypeTime   = null;
    SearchResultsThreadAdapter threadAdapter;
    SearchResultsUserAdapter userAdapter;
    AoLinearLayoutManager llm;
    SearchHelper searchHelper;
    String currentSearch;
    List<String> noMessagesResults;
    int zeroResultsCount = -1;

    @BindView(R.id.pbLoading)
    ProgressBar pbLoading;
    @BindView(R.id.tvSearchTitle)
    TextView tvSearchTitle;
    @BindView(R.id.tvSearchThreads)
    TextView tvSearchThreads;
    @BindView(R.id.tvSearchUsers)
    TextView tvSearchUsers;
    @BindView(R.id.llSearchCategory)
    LinearLayout llSearchCategory;
    @BindView(R.id.tool_bar)
    Toolbar tool_bar;
    @BindView(R.id.rvSearchResults)
    RecyclerView rvSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        tool_bar.setTitle("Search");
        setSupportActionBar(tool_bar);

        noMessagesResults = Arrays.asList(getResources().getStringArray(R.array.no_results_message));
        llm = new AoLinearLayoutManager(this);
        rvSearchResults.setLayoutManager(llm);
        threadAdapter = new SearchResultsThreadAdapter(this,new ArrayList<AoThread>());
        userAdapter = new SearchResultsUserAdapter(this,new ArrayList<ParseUser>(),this);
        rvSearchResults.setAdapter(userAdapter);
        tvSearchUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedSearchIndex != 0) {
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    selectedSearchIndex = 0;
                    tvSearchThreads.setBackgroundColor(Color.BLACK);
                    tvSearchThreads.setTextColor(Color.WHITE);
                    tvSearchUsers.setBackgroundColor(Color.WHITE);
                    tvSearchUsers.setTextColor(ContextCompat.getColor(SearchActivity.this,R.color.gray3C));
                }
            }
        });
        tvSearchThreads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedSearchIndex != 1) {
                    searchView.setQuery("", false);
                    searchView.setIconified(true);
                    selectedSearchIndex = 1;
                    tvSearchUsers.setBackgroundColor(Color.BLACK);
                    tvSearchUsers.setTextColor(Color.WHITE);
                    tvSearchThreads.setBackgroundColor(Color.WHITE);
                    tvSearchThreads.setTextColor(ContextCompat.getColor(SearchActivity.this,R.color.gray3C));
                }
            }
        });

        searchHelper = new SearchHelper(this,this);
        searchHelper.SearchPopularThreads();


        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.flContent);
                if (currentFragment != null && currentFragment instanceof ProfileFragment || currentFragment instanceof FollowersFragment || currentFragment instanceof UserListFragment) {
                    currentFragment.onResume();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        zeroResultsCount = -1;
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

        //USERS
        if(selectedSearchIndex == 0) {

            if(newText == ""){
                userAdapter = new SearchResultsUserAdapter(this,new ArrayList<ParseUser>(),this);
                rvSearchResults.setAdapter(userAdapter);
            } else {
                if (newText.length() > 2) {
                    searchHelper.SearchUsers(newText);
                    currentSearch = newText;
                    pbLoading.setVisibility(View.VISIBLE);
                    rvSearchResults.setVisibility(View.GONE);
                }
            }
        } else {
            if(newText == ""){
                threadAdapter = new SearchResultsThreadAdapter(this,new ArrayList<AoThread>());
                rvSearchResults.setAdapter(threadAdapter);
            } else {
                if (newText.length() > 2) {
                    searchHelper.SearchThreads(newText);
                    currentSearch = newText;
                    pbLoading.setVisibility(View.VISIBLE);
                    rvSearchResults.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    public void onGetSearchThreadCallback(List<AoThread> results) {

        pbLoading.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        if(results.size() == 0 && popularThreads != null) {
            zeroResultsCount++;
            loadPopularThreads();
        } else {
            zeroResultsCount = -1;
            threadAdapter = new SearchResultsThreadAdapter(this, results);
            rvSearchResults.setAdapter(threadAdapter);
            if(currentSearch != null) {
                tvSearchTitle.setText("Threads matching " + currentSearch);
            }
        }
    }

    @Override
    public void onGetSearchUsersListener(List<ParseUser> results) {

        pbLoading.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        if(results.size() == 0 && popularThreads != null) {
            zeroResultsCount++;
            loadPopularThreads();
        } else {
            zeroResultsCount = -1;
            userAdapter = new SearchResultsUserAdapter(this, results, this);
            rvSearchResults.setAdapter(userAdapter);
            pbLoading.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);
            if(currentSearch != null) {
                tvSearchTitle.setText("Users matching " + currentSearch);
            }
        }
    }

    @Override
    public void onUsernameTapped(ParseUser userTapped) {

        if(!AoUtils.isActivityInvalid(this)) {
            ProfileFragment profileFragment = null;
            if(ParseUser.getCurrentUser().getObjectId().equals(userTapped.getObjectId()))
                profileFragment = ProfileFragment.newInstance(ParseUser.getCurrentUser(), true, true,null,false);
            else
                profileFragment = ProfileFragment.newInstance(userTapped, true, false,null,false);
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.flContent, profileFragment).addToBackStack(null).commitAllowingStateLoss();

        }

    }

    @Override
    public void onGetSearchPopularThreadCallback(List<AoThread> results) {
        popularThreads = new ArrayList<>(results);
    }

    private void loadPopularThreads() {
        threadAdapter = new SearchResultsThreadAdapter(this,popularThreads);
        rvSearchResults.setAdapter(threadAdapter);
        pbLoading.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        Random rand = new Random();
        String message;
        if(zeroResultsCount > 1) {
            message = noMessagesResults.get(rand.nextInt(noMessagesResults.size()));
        } else {
            message = noMessagesResults.get(0);
        }
        tvSearchTitle.setText(message);
    }
}
