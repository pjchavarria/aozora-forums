package com.everfox.aozoraforums;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.everfox.aozoraforums.activities.LoginFormActivity;
import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.activities.SignUpFormActivity;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

public class FirstActivity extends AppCompatActivity {

    TextView tvAppMessage;
    TextView tvJapaneseName;
    RelativeLayout rlMailSignUp;
    TextView tvLogin;
    RelativeLayout rlFacebook;
    Typeface typeface;
    Integer FacebookRequestCode = 334;
    VideoView vvIntro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        getSupportActionBar().hide();
        tvAppMessage = (TextView) findViewById(R.id.tvAppMessage);
        tvJapaneseName = (TextView) findViewById(R.id.tvJapaneseName);
        rlMailSignUp = (RelativeLayout) findViewById(R.id.rlMailSignUp);
        tvLogin = (TextView) findViewById(R.id.tvLogin);
        rlFacebook = (RelativeLayout) findViewById(R.id.rlFacebook);
        vvIntro = (VideoView) findViewById(R.id.vvIntro);
        rlFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<String> permissions = Arrays.asList("public_profile", "email", "user_friends");
                ParseFacebookUtils.logInWithReadPermissionsInBackground(FirstActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if(err == null) {
                            if (user == null) {
                                //Nothing
                            } else if (user.isNew()) {
                                //SignUp
                                Intent i = new Intent(FirstActivity.this, SignUpFormActivity.class);
                                AozoraForumsApp.setParseFacebookNewUser(user);
                                startActivity(i);

                                finish();
                            } else {
                                //Enter app
                                Intent i = new Intent(FirstActivity.this, MainActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(i);
                            }
                        } else {
                            Toast.makeText(FirstActivity.this,err.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        typeface = AozoraForumsApp.getAwesomeTypeface();
        tvJapaneseName.setTypeface(typeface);


        rlMailSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FirstActivity.this, SignUpFormActivity.class);
                startActivity(i);
            }
        });
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(FirstActivity.this, LoginFormActivity.class);
                startActivity(i);
            }
        });

        Uri uri = Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.intro);
        vvIntro.setVideoURI(uri);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == FacebookRequestCode) {
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        vvIntro.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                vvIntro.start();
            }
        });
        tvAppMessage.setText(AoUtils.fromHtml(getResources().getString(R.string.activity_login_tvAppMessage_Text)));

        if(ParseUser.getCurrentUser() != null) {
            Intent i = new Intent(FirstActivity.this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }
}
