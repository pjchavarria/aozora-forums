package com.everfox.aozoraforums.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.FirstActivity;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.controllers.ForumsHelper;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class ForumsFragment extends Fragment {


    SharedPreferences sharedPreferences;
    LinearLayoutManager llm;
    ForumsHelper forumsHelper;


    @BindView(R.id.btnLogout)
    Button btnLogout;

    @BindView(R.id.rlAoArt)
    RelativeLayout rlAoArt;
    @BindView(R.id.rlAoNews)
    RelativeLayout rlAoNews;
    @BindView(R.id.rlAoGur)
    RelativeLayout rlAoGur;
    @BindView(R.id.rlAoTalk)
    RelativeLayout rlAoTalk;
    @BindView(R.id.rlOfficial)
    RelativeLayout rlOfficial;

    @BindView(R.id.vAoArt)
    RelativeLayout vAoArt;
    @BindView(R.id.vAoNews)
    RelativeLayout vAoNews;
    @BindView(R.id.vAoGur)
    RelativeLayout vAoGur;
    @BindView(R.id.vAoTalk)
    RelativeLayout vAoTalk;
    @BindView(R.id.vOffical)
    RelativeLayout vOffical;

    @BindView(R.id.rvForums)
    RelativeLayout rvForums;
    @BindView(R.id.swipeRefreshForums)
    SwipeRefreshLayout swipeRefreshForums;




    public static ForumsFragment newInstance() {
        ForumsFragment fragment = new ForumsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_forums, container, false);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    ParseObject.unpinAll();
                } catch (ParseException pEx) {
                }

                sharedPreferences = getActivity().getSharedPreferences("com.everfox.aozoraforums", Context.MODE_PRIVATE);
                sharedPreferences.edit().remove("MAL_User").apply();
                sharedPreferences.edit().remove("MAL_Password").apply();
                AozoraForumsApp.cleanValues();
                ParseUser.logOut();
                Intent intent = new Intent(getActivity(), FirstActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        ButterKnife.bind(this,view);




        return view;
    }
}
