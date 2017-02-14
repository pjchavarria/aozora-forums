package com.everfox.aozoraforums.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.dialogfragments.SimpleLoadingDialogFragment;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.UserDetails;
import com.everfox.aozoraforums.utils.AoUtils;
import com.everfox.aozoraforums.utils.PostUtils;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditProfileActivity extends AozoraActivity {

    Integer PICK_IMAGE_AVATAR = 100;
    Integer PICK_IMAGE_BANNER = 101;
    String avatarFileName;
    String bannerFileName;
    byte[] avatarByteArray;
    byte[] bannerByteArray;

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


    SimpleLoadingDialogFragment dialog = new SimpleLoadingDialogFragment();
    private static final int STORAGE_PERMISSION = 23;
    Boolean aboutChanged = false;
    Boolean emailChanged = false;
    Boolean avatarChanged = false;
    Boolean bannerChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        setTitle("Edit Profile");
        currentUser = ParseUser.getCurrentUser();
        PostUtils.loadAvatarPic(currentUser.getParseFile(ParseUserColumns.AVATAR_THUMB),ivAvatar);
        PostUtils.loadBannerPic(currentUser.getParseFile(ParseUserColumns.BANNER),ivBanner);
        tvEmail.setText(currentUser.getString(ParseUserColumns.EMAIL));
        tvEmail.addTextChangedListener(emailTextWatcher);
        loadDetails();
        llAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageClicked(PICK_IMAGE_AVATAR);
            }
        });
        llBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageClicked(PICK_IMAGE_BANNER);
            }
        });
        fabSaveChanges.setOnClickListener(fabSaveChangesListener);
    }

    private void imageClicked(int avatarBanner) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        avatarBanner);
            } else {

                openGalleryIntent(avatarBanner);
            }
        } else
            openGalleryIntent(avatarBanner);
    }

    private void openGalleryIntent(int avatarBanner) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), avatarBanner);
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
                        tvAbout.addTextChangedListener(aboutTextWatcher);
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
                                    tvAbout.addTextChangedListener(aboutTextWatcher);
                                }
                            }
                        });
                    } else {
                        userDetails = object;
                        tvAbout.setText(userDetails.getString(UserDetails.ABOUT));
                        tvAbout.addTextChangedListener(aboutTextWatcher);

                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGalleryIntent(requestCode);
        } else {
            Toast.makeText(this, "Permission denied. Please allow the permission to set the avatar", Toast.LENGTH_LONG).show();
        }
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == PICK_IMAGE_AVATAR || requestCode == PICK_IMAGE_BANNER) {
                Uri selectedImageUri = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME};
                Cursor cursor =
                        getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
                if(cursor == null) {
                    Toast.makeText(this,"File not found",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (cursor.moveToFirst()) {
                    int fileNameIndex = cursor.getColumnIndex(filePathColumn[1]);
                    if(requestCode == PICK_IMAGE_AVATAR)
                        avatarFileName = cursor.getString(fileNameIndex);
                    else
                        bannerFileName = cursor.getString(fileNameIndex);
                }
                cursor.close();

                try {
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    if(requestCode == PICK_IMAGE_AVATAR ) {
                        avatarByteArray = AoUtils.getBytesFromStream(inputStream);
                        ivAvatar.setImageURI(selectedImageUri);
                        avatarChanged = true;
                    } else {
                        bannerByteArray = AoUtils.getBytesFromStream(inputStream);
                        ivBanner.setImageURI(selectedImageUri);
                        bannerChanged = true;
                    }

                }catch (FileNotFoundException e) {
                    Toast.makeText(this,"File not found",Toast.LENGTH_SHORT).show();
                }
                catch (IOException ioEx) {

                }
            }
        }
    }

    View.OnClickListener fabSaveChangesListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            dialog.show(getSupportFragmentManager(),"");

            if(!aboutChanged && !emailChanged && !avatarChanged && !bannerChanged) {
                dialog.dismissAllowingStateLoss();
                return;
            }
            if(aboutChanged) {
                userDetails.put(UserDetails.ABOUT,tvAbout.getText().toString());
            }
            if(emailChanged) {
                if(validEmail(tvEmail.getText().toString())) {
                    currentUser.put(ParseUserColumns.EMAIL, tvEmail.getText().toString());
                }
                else
                    Toast.makeText(EditProfileActivity.this,getResources().getString(R.string.activity_signUpForm_badFormatEmail),Toast.LENGTH_SHORT).show();
            }
            if(avatarChanged) {

                final ParseFile file = new ParseFile(avatarFileName, avatarByteArray);
                file.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        currentUser.put("avatarThumb",file);
                        userDetails.put("avatarRegular",file);
                        changedBanner();

                    }
                });
            } else {
                changedBanner();
            }

        }
    };

    private void changedBanner() {
        if(bannerChanged) {
            final ParseFile file = new ParseFile(bannerFileName, bannerByteArray);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    currentUser.put(ParseUserColumns.BANNER,file);
                    saveUser();
                }
            });
        } else {
            saveUser();
        }
    }

    private void saveUser() {
        if(aboutChanged || avatarChanged) {
            userDetails.saveInBackground();
        }
        currentUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismissAllowingStateLoss();
                Toast.makeText(EditProfileActivity.this,"Profile updated",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private Boolean validEmail(String email) {
        if (email.length() == 0) {
            return false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false;
        }
        return true;
    }

    TextWatcher aboutTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            aboutChanged = true;

        }
    };

    TextWatcher emailTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            emailChanged = true;

        }
    };




}
