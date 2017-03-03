package com.everfox.aozoraforums.activities.postthread;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.activities.MainActivity;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.models.LinkData;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    String youtubeID = null;
    String selectedLinkUrl = null;
    LinkData selectedLinkData = null;

    Boolean fetchingData = true;
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

        ivAddLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedLinkUrl == null) {
                    Toast.makeText(CreatePostActivity.this,"Paste any link in the text area", Toast.LENGTH_SHORT).show();
                } else {
                    selectedLinkUrl = null;
                    ivAddLink.clearColorFilter();
                }
            }
        });

        etComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(selectedLinkUrl == null) {
                    List<String> lstUrls = AoUtils.extractLinks(etComment.getText().toString());
                    if (lstUrls != null && lstUrls.size() > 0) {
                        String link = lstUrls.get(0);
                        Uri linkUri = Uri.parse(link);
                        String host = linkUri.getHost();
                        if (host != null && host.contains("youtube.com") || host.contains("youtu.be")) {
                            //Video
                            if (host.contains("youtube.com")) {
                                clearAttachments();
                                youtubeID = linkUri.getQueryParameter("v");
                            } else if (host.contains("youtu.be")) {
                                youtubeID = linkUri.getPathSegments().get(1);
                            }
                            ivAddVideo.setColorFilter(ContextCompat.getColor(CreatePostActivity.this, R.color.red_airing));
                            return;
                        }
                        if (link.toLowerCase().endsWith(".png") || link.toLowerCase().endsWith(".jpeg")
                                || link.toLowerCase().endsWith("jpg") || link.toLowerCase().endsWith(".gif")) {
                            scrapeImageWithURL(link);
                            return;
                        }

                        selectedLinkUrl = link;
                        scrapeLinkWithURL(link);
                        return;
                    }
                }

            }
        });



    }

    private void scrapeLinkWithURL(String link) {
        fetchingData = true;
        HashMap<String,String> parametersMap = new HashMap<>();
        parametersMap.put("url",link);
        ParseCloud.callFunctionInBackground("Scrapper.ScrapeURLMetadata", parametersMap, new FunctionCallback<Object>() {
            @Override
            public void done(Object object, ParseException e) {
                fetchingData = false;
                if(e== null) {

                    selectedLinkData = LinkData.mapHashMap((HashMap)object);
                    ivAddLink.setColorFilter(ContextCompat.getColor(CreatePostActivity.this,R.color.red_airing));
                } else {
                    selectedLinkUrl = null;
                    ivAddLink.clearColorFilter();
                }
            }
        });
    }

    private void scrapeImageWithURL(String link) {

        fetchingData = true;
        DownloadImage task = new DownloadImage();
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,link);
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
        selectedLinkUrl = null;
        ivAddPhotoInternet.clearColorFilter();
        ivAddPhotoGallery.clearColorFilter();
        ivAddVideo.clearColorFilter();
        ivAddLink.clearColorFilter();

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

    private class DownloadImage extends AsyncTask<String, Void, Void>{

        private Bitmap theBitmap = null;
        private String imgURL = "";

        @Override
        protected Void doInBackground(String... strings) {

            try {
                imgURL = strings[0];
                theBitmap = Glide.with(CreatePostActivity.this)
                        .load(imgURL)
                        .asBitmap()
                        .into(-1, -1).get();
            } catch (final ExecutionException e) {
                Log.e("a", e.getMessage());
            } catch (final InterruptedException e) {
                Log.e("a", e.getMessage());
            }
            fetchingData = false;
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if(theBitmap != null) {
                imageDataWeb = new ImageData();
                imageDataWeb.setHeight(theBitmap.getHeight());
                imageDataWeb.setWidth(theBitmap.getWidth());
                imageDataWeb.setImageURL(imgURL);
                clearAttachments();
                ivAddPhotoInternet.setColorFilter(ContextCompat.getColor(CreatePostActivity.this,R.color.red_airing));
            }
        }
    }

}
