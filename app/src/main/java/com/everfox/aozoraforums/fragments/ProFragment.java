package com.everfox.aozoraforums.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.everfox.aozoraforums.R;

public class ProFragment extends Fragment {

    private static final String ARG_PARAM1 = "position";

    TextView tvPro;
    ImageView ivPro;

    private int position;


    public ProFragment() {
        // Required empty public constructor
    }

    public static ProFragment newInstance(int position) {
        ProFragment fragment = new ProFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pro, container, false);
        tvPro = (TextView) rootView.findViewById(R.id.tvPro);
        ivPro = (ImageView) rootView.findViewById(R.id.ivPro);
        if (position == 1) {
            tvPro.setText("GET A PRO BADGE");
            ivPro.setImageResource(R.drawable.in_app_purchases_3);
        } else {
            tvPro.setText("NO MORE ADS");
            ivPro.setImageResource(R.drawable.in_app_purchases_4);
        }
        return rootView;
    }

}
