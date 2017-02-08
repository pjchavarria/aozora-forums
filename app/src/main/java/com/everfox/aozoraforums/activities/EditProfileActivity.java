package com.everfox.aozoraforums.activities;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditProfileActivity extends AppCompatActivity {

    @BindView(R.id.llAvatar)
    LinearLayout llAvatar;
    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;
    @BindView(R.id.llBanner)
    LinearLayout llBanner;
    @BindView(R.id.ivBanner)
    ImageView ivBanner;
    @BindView(R.id.llEmail)
    LinearLayout llEmail;
    @BindView(R.id.tvEmail)
    EditText tvEmail;
    @BindView(R.id.llAbout)
    LinearLayout llAbout;
    @BindView(R.id.tvAbout)
    EditText tvAbout;
    @BindView(R.id.fabSaveChanges)
    FloatingActionButton fabSaveChanges;
    ParseUser currentUser;
    UserDetails userDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        setTitle("Edit Profile");
        currentUser = ParseUser.getCurrentUser();
        PostUtils.loadAvatarPic(currentUser.getParseFile(ParseUserColumns.AVATAR_THUMB),ivAvatar);
        PostUtils.loadAvatarPic(currentUser.getParseFile(ParseUserColumns.BANNER),ivBanner);
        tvEmail.setText(currentUser.getString(ParseUserColumns.EMAIL));
        loadDetails();
    }

    private void loadDetails() {
        ParseObject details =currentUser.getParseObject(ParseUserColumns.DETAILS);
        if(details == null) {

            ParseQuery<UserDetails> queryDetails = ParseQuery.getQuery(UserDetails.class);
            queryDetails.setLimit(1);
            queryDetails.whereEqualTo(UserDetails.DETAILS_USER, currentUser);
            queryDetails.findInBackground(new FindCallback<UserDetails>() {
                @Override
                public void done(List<UserDetails> objects, ParseException e) {
                    if (objects != null && e == null && objects.size() > 0) {
                        userDetails = objects.get(0);
                        tvAbout.setText(userDetails.getString(UserDetails.ABOUT));
                    }
                }
            });

        } else {
            details.fetchIfNeededInBackground(new GetCallback<UserDetails>() {
                @Override
                public void done(UserDetails object, ParseException e) {

                    if (object == null) {
                        ParseQuery<UserDetails> queryDetails = ParseQuery.getQuery(UserDetails.class);
                        queryDetails.setLimit(1);
                        queryDetails.whereEqualTo(UserDetails.DETAILS_USER, currentUser);
                        queryDetails.findInBackground(new FindCallback<UserDetails>() {
                            @Override
                            public void done(List<UserDetails> objects, ParseException e) {
                                if (objects != null && e == null && objects.size() > 0) {
                                    userDetails = objects.get(0);
                                    tvAbout.setText(userDetails.getString(UserDetails.ABOUT));
                                }
                            }
                        });
                    } else {
                        userDetails = object;
                        tvAbout.setText(userDetails.getString(UserDetails.ABOUT));
                    }
                }
            });
        }
    }
}
