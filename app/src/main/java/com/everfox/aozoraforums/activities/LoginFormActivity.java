package com.everfox.aozoraforums.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.FirstActivity;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.dialogfragments.SimpleLoadingDialogFragment;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PurchaseUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.List;

public class LoginFormActivity extends AppCompatActivity {

    Button btnLogin;
    EditText etUsername;
    EditText etPassword;
    TextView tvForgotPassword;
    View rlFacebook;
    SimpleLoadingDialogFragment simpleLoading  = new SimpleLoadingDialogFragment();
    Integer FacebookRequestCode = 334;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(getResources().getString(R.string.activity_loginForm_tvTitle_Text));
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
        rlFacebook = findViewById(R.id.rlFacebook);

        rlFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final List<String> permissions = Arrays.asList("public_profile", "email", "user_friends");
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginFormActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException err) {
                        if(err == null) {
                            if (user == null) {
                                //Nothing
                            } else if (user.isNew()) {
                                //SignUp
                                Intent i = new Intent(LoginFormActivity.this, SignUpFormActivity.class);
                                AozoraForumsApp.setParseFacebookNewUser(user);
                                startActivity(i);

                                finish();
                            } else {
                                //Enter app
                                logInSuccesfull(user);
                            }
                        } else {
                            Toast.makeText(LoginFormActivity.this,err.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckFields()) {
                    if(AoUtils.canAddSimpleLoadingDialog(simpleLoading,getSupportFragmentManager()))
                        simpleLoading.show(getSupportFragmentManager(),"loading");

                    if(!AoUtils.isNetworkAvailable(getApplicationContext())) {
                        Toast.makeText(LoginFormActivity.this, getResources().getString(R.string.message_no_internet), Toast.LENGTH_SHORT).show();
                        simpleLoading.dismissAllowingStateLoss();
                    } else {
                        ParseUser.logInInBackground(etUsername.getText().toString().toLowerCase(), etPassword.getText().toString(), new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                if (user != null) {
                                    logInSuccesfull(user);
                                    simpleLoading.dismissAllowingStateLoss();
                                } else {
                                    //Intentamos segunda vez)
                                    tryLoginSecondTime(etUsername.getText().toString().toLowerCase(), etPassword.getText().toString());
                                }
                            }
                        });
                    }
                }
            }
        });
        tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginFormActivity.this, ResetPasswordActivity.class);
                startActivity(i);
            }
        });
    }

    private void tryLoginSecondTime(String user, String password) {

        password = Character.toString(password.charAt(0)).toUpperCase()+password.substring(1);
        ParseUser.logInInBackground(user, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    logInSuccesfull(user);
                } else {
                    //Intentamos segunda vez)
                    tryLoginAsUserType(etUsername.getText().toString(), etPassword.getText().toString());

                }
                simpleLoading.dismissAllowingStateLoss();
            }
        });
    }

    private void tryLoginAsUserType(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            public void done(ParseUser user, ParseException e) {
                if (user != null) {
                    logInSuccesfull(user);
                } else {
                    if(e != null)
                        Toast.makeText(LoginFormActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                simpleLoading.dismissAllowingStateLoss();
            }
        });
    }

    private void logInSuccesfull(ParseUser user) {
        PurchaseUtils.deletePurchases(LoginFormActivity.this);
        JSONArray purchased = user.getJSONArray("unlockContent");
        if(purchased != null ) {
            for (int i = 0; i < purchased.length(); i++) {
                try {
                    PurchaseUtils.purchaseProduct(LoginFormActivity.this, purchased.getString(i));
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        }
        Intent i = new Intent(LoginFormActivity.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }


    private boolean CheckFields() {
        Boolean valid = false;
        if(etUsername.getText().length() == 0 ) {
            Toast.makeText(this, getResources().getString(R.string.activity_signUpForm_emptyUsername), Toast.LENGTH_SHORT).show();
        }
        else if(etPassword.getText().length() == 0) {
            Toast.makeText(this,getResources().getString(R.string.activity_signUpForm_emptyPassword),Toast.LENGTH_SHORT).show();
        }
        else {
            valid = true;
        }
        return valid;
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

}
