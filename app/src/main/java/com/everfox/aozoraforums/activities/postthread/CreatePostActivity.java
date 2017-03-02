package com.everfox.aozoraforums.activities.postthread;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.models.ImageData;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/28/2017.
 */

public class CreatePostActivity extends AozoraActivity {

    int REQUEST_SEARCH_IMAGE = 400;
    ImageData imageDataWeb = null;

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
                Intent i = new Intent(CreatePostActivity.this,SearchImageActivity.class);
                startActivityForResult(i,REQUEST_SEARCH_IMAGE);
            }
        });

    }


}
