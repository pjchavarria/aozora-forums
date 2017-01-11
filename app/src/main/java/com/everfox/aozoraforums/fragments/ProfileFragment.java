package com.everfox.aozoraforums.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.utils.AozoraUtils;
import com.parse.GetDataCallback;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 1/10/2017.
 */

public class ProfileFragment extends Fragment {

    ParseUser user;

    @BindView(R.id.pbLoading) ProgressBar pbLoading;
    @BindView(R.id.appbar) android.support.design.widget.AppBarLayout appbar;
    @BindView(R.id.profileContent) NestedScrollView profileContent;
    @BindView(R.id.ivAnimeBanner)
    ImageView ivAnimeBanner;
    @BindView(R.id.ivAvatar) ImageView ivAvatar;
    @BindView(R.id.tvPopularity)
    TextView tvPopularity;

    public static ProfileFragment newInstance(ParseUser user) {
        ProfileFragment fragment = new ProfileFragment();
        fragment.user = user;
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(user.getString(ParseUserColumns.AOZORA_USERNAME));
        ButterKnife.bind(this,view);
        pbLoading.setVisibility(View.VISIBLE);
        appbar.setVisibility(View.GONE);
        profileContent.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ParseFile profilePic = user.getParseFile(ParseUserColumns.AVATAR_THUMB);
        ParseFile bannerPic = user.getParseFile(ParseUserColumns.BANNER);
        loadAvatarAndBanner(profilePic,bannerPic);
        tvPopularity.setText(AozoraUtils.numberToStringOrZero(user.getNumber(ParseUserColumns.REPUTATION)));

        pbLoading.setVisibility(View.GONE);
        appbar.setVisibility(View.VISIBLE);
        profileContent.setVisibility(View.VISIBLE);
    }

    private void loadAvatarAndBanner(ParseFile profilePic, ParseFile bannerPic) {
        if(profilePic != null) {
            profilePic.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(data, 0, data.length);
                        ivAvatar.setImageBitmap(bmp);
                    }
                }
            });
        }
        if(bannerPic != null) {
            bannerPic.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] data, com.parse.ParseException e) {
                    if (e == null) {
                        Bitmap bmp = BitmapFactory
                                .decodeByteArray(data, 0, data.length);
                        ivAnimeBanner.setImageBitmap(bmp);
                    }
                }
            });
        }
    }
}
