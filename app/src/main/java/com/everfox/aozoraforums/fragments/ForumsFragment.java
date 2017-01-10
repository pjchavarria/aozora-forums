package com.everfox.aozoraforums.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.FirstActivity;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.MainActivity;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class ForumsFragment extends Fragment {

    Button btnLogout;
    SharedPreferences sharedPreferences;

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
        btnLogout = (Button) view.findViewById(R.id.btnLogout);
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

        return view;
    }
}
