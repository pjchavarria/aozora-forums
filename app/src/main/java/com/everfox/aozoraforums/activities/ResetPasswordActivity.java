package com.everfox.aozoraforums.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.utils.AozoraUtils;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btnResetPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(getResources().getString(R.string.activity_resetPassword_tvTitle_Text));
        etEmail = (EditText) findViewById(R.id.etEmail);
        btnResetPassword = (Button) findViewById(R.id.btnResetPassword);
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(CheckFields()) {
                    ParseUser.requestPasswordResetInBackground(etEmail.getText().toString(), new RequestPasswordResetCallback() {
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(ResetPasswordActivity.this,getResources().getString(R.string.activity_resetPassword_validationMessage),Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ResetPasswordActivity.this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }


    private boolean CheckFields() {
        Boolean valid = false;
        if(etEmail.getText().length() == 0 ) {
            Toast.makeText(this, getResources().getString(R.string.activity_signUpForm_emptyUsername), Toast.LENGTH_SHORT).show();
        }
        else if(!AozoraUtils.isNetworkAvailable(getApplicationContext())) {

            Toast.makeText(this, getResources().getString(R.string.message_no_internet), Toast.LENGTH_SHORT).show();
        }
        else {
            valid = true;
        }
        return valid;
    }
}
