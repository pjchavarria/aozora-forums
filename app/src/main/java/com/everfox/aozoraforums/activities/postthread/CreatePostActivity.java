package com.everfox.aozoraforums.activities.postthread;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.utils.AoUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/28/2017.
 */

public class CreatePostActivity extends AozoraActivity {

    int REQUEST_SEARCH_YOUTUBE = 402;
    int REQUEST_PICK_IMAGE = 401;
    int REQUEST_SEARCH_IMAGE = 400;
    ImageData imageDataWeb = null;
    ImageData imageGallery = null;
    String youtubeID = "";

    public static final String PARAM_TYPE = "type";
    public static final int NEW_TIMELINEPOST = 0;
    public static final int EDIT_TIMELINEPOST = 1;
    public static final int NEW_TIMELINEPOST_REPLY = 2;
    public static final int EDIT_TIMELINEPOST_REPLY = 3;
    public static final int NEW_AOTHREAD = 4;
    public static final int EDIT_AOTHREAD = 5;
    public static final int NEW_AOTHREAD_REPLY = 6;
    public static final int EDIT_AOTHREAD_REPLY = 7;
    int type;
    Boolean hasSpoilers = false;

    @BindView(R.id.llSpoilerText)
    LinearLayout llSpoilerText;
    @BindView(R.id.etComment)
    EditText etComment;
    @BindView(R.id.etSpoilerText)
    EditText etSpoilerText;
    @BindView(R.id.ivAddPhotoInternet)
    ImageView ivAddPhotoInternet;
    @BindView(R.id.ivAddPhotoGallery)
    ImageView ivAddPhotoGallery;
    @BindView(R.id.ivAddVideo)
    ImageView ivAddVideo;
    @BindView(R.id.ivAddLink)
    ImageView ivAddLink;
    @BindView(R.id.btnSpoilers)
    Button btnSpoilers;
    @BindView(R.id.btnSendComment)
    Button btnSendComment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_createeditpostthread);
        ButterKnife.bind(this);

        if(getIntent().hasExtra(PARAM_TYPE)) {
            type = getIntent().getIntExtra(PARAM_TYPE,0);
        } else {
            finish();
        }

        switch (type){
            case NEW_TIMELINEPOST:
                setTitle("New Post");
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        ivAddPhotoInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageDataWeb == null) {
                    Intent i = new Intent(CreatePostActivity.this, SearchImageActivity.class);
                    startActivityForResult(i, REQUEST_SEARCH_IMAGE);
                } else {
                    imageDataWeb = null;
                    ivAddPhotoInternet.clearColorFilter();
                }
            }
        });
        ivAddPhotoGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageGallery == null) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        boolean hasPermission = (ContextCompat.checkSelfPermission(CreatePostActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                        if (!hasPermission) {

                            ActivityCompat.requestPermissions(CreatePostActivity.this,
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_PICK_IMAGE);
                        } else {
                            openGalleryIntent(REQUEST_PICK_IMAGE);
                        }
                    } else
                        openGalleryIntent(REQUEST_PICK_IMAGE);

                } else {
                    imageGallery = null;
                    ivAddPhotoGallery.clearColorFilter();
                }
            }
        });
        ivAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(youtubeID == null) {
                    Intent i = new Intent(CreatePostActivity.this, SearchYoutubeActivity.class);
                    startActivityForResult(i, REQUEST_SEARCH_YOUTUBE);
                }else {
                    youtubeID = null;
                    ivAddVideo.clearColorFilter();
                }
            }
        });
        btnSpoilers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasSpoilers) {
                    hasSpoilers = false;
                    llSpoilerText.setVisibility(View.GONE);
                    btnSpoilers.setTextColor(ContextCompat.getColor(CreatePostActivity.this,R.color.grayC5));
                } else {
                    hasSpoilers = true;
                    llSpoilerText.setVisibility(View.VISIBLE);
                    btnSpoilers.setTextColor(ContextCompat.getColor(CreatePostActivity.this,R.color.gray3C));
                }
            }
        });


    }


    private void openGalleryIntent(int avatarBanner) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), avatarBanner);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SEARCH_IMAGE) {
            if(data != null && resultCode == SearchImageActivity.RESULT_SUCCESS) {
                clearAttachments();
                if (data.hasExtra(SearchImageActivity.IMAGE_DATA)){
                    imageDataWeb = (ImageData) data.getSerializableExtra(SearchImageActivity.IMAGE_DATA);
                    ivAddPhotoInternet.setColorFilter(ContextCompat.getColor(this,R.color.red_airing));
                }
            }
        } else if(requestCode == REQUEST_PICK_IMAGE) {
            if(resultCode == RESULT_OK) {
                clearAttachments();
                imageGallery = AoUtils.resizeImage(data.getData(),this);
                ivAddPhotoGallery.setColorFilter(ContextCompat.getColor(this,R.color.red_airing));
            }
        } else if (requestCode == REQUEST_SEARCH_YOUTUBE) {
            if(resultCode == RESULT_OK) {

                clearAttachments();
                if (data.hasExtra(SearchYoutubeActivity.YOUTUBE_ID)){
                    youtubeID = data.getStringExtra(SearchYoutubeActivity.YOUTUBE_ID);
                    ivAddVideo.setColorFilter(ContextCompat.getColor(this,R.color.red_airing));
                }
            }
        }
    }

    private void clearAttachments() {
        youtubeID = null;
        imageGallery = null;
        imageDataWeb = null;
        ivAddPhotoInternet.clearColorFilter();
        ivAddPhotoGallery.clearColorFilter();
        ivAddVideo.clearColorFilter();
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

}
