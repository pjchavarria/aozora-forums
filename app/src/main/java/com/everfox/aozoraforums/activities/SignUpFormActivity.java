package com.everfox.aozoraforums.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.controls.RoundedImageView;
import com.everfox.aozoraforums.dialogfragments.SimpleLoadingDialogFragment;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AozoraUtils;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class SignUpFormActivity extends AppCompatActivity {

    EditText etPickUsername;
    EditText etPassword;
    EditText etEmail;
    Button btnSignUp;
    Button btnChooseAvatar;
    Boolean isFacebook = false;
    Integer PICK_IMAGE = 100;
    byte[] avatarByteArray = null;
    String avatarFileName = null;
    RoundedImageView ivChooseAvatar;
    ParseUser newUserFacebook;
    SimpleLoadingDialogFragment simpleLoading  = new SimpleLoadingDialogFragment();

    private static final int STORAGE_PERMISSION = 23;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_form);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.ic_launcher);
        getSupportActionBar().setTitle(getResources().getString(R.string.activity_signUpForm_tvTitle_Text));
        etPickUsername = (EditText) findViewById(R.id.etPickUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etEmail = (EditText) findViewById(R.id.etEmail);
        btnSignUp = (Button) findViewById(R.id.btnSignUp);
        btnChooseAvatar = (Button) findViewById(R.id.btnChooseAvatar);
        ivChooseAvatar = (RoundedImageView) findViewById(R.id.ivChooseAvatar);

        newUserFacebook = AozoraForumsApp.getParseFacebookNewUser();
        if(newUserFacebook != null) isFacebook = true;
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( CheckFields()) {

                    if(AozoraUtils.canAddSimpleLoadingDialog(simpleLoading,getSupportFragmentManager()))
                        simpleLoading.show(getSupportFragmentManager(),"loading");
                    SignUp();
                }
            }
        });
        btnChooseAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                avatarClicked();
            }
        });
        ivChooseAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                avatarClicked();
            }
        });

        btnChooseAvatar.setBackground(ResourcesCompat.getDrawable(getResources(),R.drawable.button_circle_avatar,null));

        if(isFacebook)
            etPassword.setVisibility(View.GONE);

    }

    private void avatarClicked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasPermission = (ContextCompat.checkSelfPermission(SignUpFormActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {

                ActivityCompat.requestPermissions(SignUpFormActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION);
            } else {

                openGalleryIntent();
            }
        } else
            openGalleryIntent();
    }

    private void openGalleryIntent(){

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, ""), PICK_IMAGE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGalleryIntent();
                } else {
                    Toast.makeText(SignUpFormActivity.this, "Permission denied. Please allow the permission to set the avatar", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE) {
                Uri selectedImageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME};
                Cursor cursor =
                        getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                if(cursor == null) {
                    Toast.makeText(SignUpFormActivity.this,"File not found",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cursor.moveToFirst()) {
                    int fileNameIndex = cursor.getColumnIndex(filePathColumn[1]);
                    avatarFileName = cursor.getString(fileNameIndex);
                }
                cursor.close();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    avatarByteArray = getBytes(inputStream);
                    btnChooseAvatar.setVisibility(View.INVISIBLE);
                    ivChooseAvatar.setImageURI(selectedImageUri);
                    ivChooseAvatar.setVisibility(View.VISIBLE);
                }catch (FileNotFoundException e) {
                    Toast.makeText(SignUpFormActivity.this,"File not found",Toast.LENGTH_SHORT).show();
                }
                catch (IOException ioEx) {

                }
            }
        }

    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    private void SignUp() {
        try {

        final ParseUser user;
        if(isFacebook)
            user = newUserFacebook;
        else
            user = new ParseUser();
        final UserDetails userDetails = new UserDetails();
        user.setUsername(etPickUsername.getText().toString().toLowerCase());
        if(!isFacebook)
            user.setPassword(etPassword.getText().toString());
        user.setEmail(etEmail.getText().toString().trim().toLowerCase());
        user.put("aozoraUsername", etPickUsername.getText().toString());
        user.put("joinDate", new Date());
        userDetails.put("joinDate",new Date());
        userDetails.put("gender","Not specified");

        if(avatarByteArray != null) {
            final ParseFile file = new ParseFile(avatarFileName, avatarByteArray);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    user.put("avatarThumb",file);
                    userDetails.put("avatarRegular",file);

                }
            });
        } else {
            //Ponemos imagen por defecto, por ahora ic_launcher
            Drawable drawable = ResourcesCompat.getDrawable(getResources(),R.drawable.profile_placeholder,null);
            Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bitMapData = stream.toByteArray();
            final ParseFile file = new ParseFile(avatarFileName, bitMapData);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    user.put("avatarThumb",file);
                    userDetails.put("avatarRegular",file);
                }
            });
            bitmap.recycle();
        }

        if(isFacebook) {
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    simpleLoading.dismissAllowingStateLoss();
                    EnterApp(user,userDetails);
                }
            });
        } else {

            user.signUpInBackground(new SignUpCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null) {
                        Toast.makeText(SignUpFormActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        EnterApp(user, userDetails);
                    }
                }
            });
        }

        }
        catch(Exception ex) {
            simpleLoading.dismissAllowingStateLoss();
        }
    }

    private void EnterApp(final ParseUser user, final UserDetails userDetails) {

        if(isFacebook) {
            userDetails.put("details",user);
            userDetails.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    user.put("details",userDetails);
                    user.saveInBackground();
                    AozoraForumsApp.setParseFacebookNewUser(null);
                    Intent i = new Intent(SignUpFormActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
            });
        }
         else {
            userDetails.put("details", user);
            userDetails.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Toast.makeText(SignUpFormActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        ParseUser.logInInBackground(user.getUsername(), etPassword.getText().toString(), new LogInCallback() {
                            public void done(ParseUser user, ParseException e) {
                                if (e != null) {
                                    Toast.makeText(SignUpFormActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                } else {

                                    user.put("details",userDetails);
                                    user.saveInBackground();
                                    AozoraForumsApp.setParseFacebookNewUser(null);
                                    simpleLoading.dismissAllowingStateLoss();
                                    Intent i = new Intent(SignUpFormActivity.this, MainActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(i);
                                }
                            }
                        });
                    }

                }
            });
        }
    }

    private Boolean CheckFields() {
        Boolean valid = false;
        if(etPickUsername.getText().length() == 0 ) {
            Toast.makeText(this, getResources().getString(R.string.activity_signUpForm_emptyUsername),Toast.LENGTH_SHORT).show();
        }
        else if(!isFacebook && etPassword.getText().length() == 0) {
            Toast.makeText(this,getResources().getString(R.string.activity_signUpForm_emptyPassword),Toast.LENGTH_SHORT).show();
        }
        else if( etEmail.getText().length() == 0) {
            Toast.makeText(this,getResources().getString(R.string.activity_signUpForm_emptyEmail),Toast.LENGTH_SHORT).show();
        }
        else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(etEmail.getText()).matches()) {
            Toast.makeText(this,getResources().getString(R.string.activity_signUpForm_badFormatEmail),Toast.LENGTH_SHORT).show();
        } else  if (!AozoraUtils.isNetworkAvailable(getApplicationContext()) ) {
            Toast.makeText(this,getResources().getString(R.string.message_no_internet),Toast.LENGTH_SHORT).show();
        }
        else {
            valid = true;
        }
        return valid;

    }
}
