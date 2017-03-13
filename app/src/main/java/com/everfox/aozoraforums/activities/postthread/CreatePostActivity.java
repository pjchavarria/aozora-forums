package com.everfox.aozoraforums.activities.postthread;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.everfox.aozoraforums.AozoraForumsApp;
import com.everfox.aozoraforums.R;
import com.everfox.aozoraforums.activities.AozoraActivity;
import com.everfox.aozoraforums.controllers.AddPostThreadHelper;
import com.everfox.aozoraforums.models.Anime;
import com.everfox.aozoraforums.models.AoThread;
import com.everfox.aozoraforums.models.AoThreadTag;
import com.everfox.aozoraforums.models.ImageData;
import com.everfox.aozoraforums.models.LinkData;
import com.everfox.aozoraforums.models.PUser;
import com.everfox.aozoraforums.models.ParseUserColumns;
import com.everfox.aozoraforums.models.TimelinePost;
import com.everfox.aozoraforums.utils.AoUtils;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by daniel.soto on 2/28/2017.
 */

public class CreatePostActivity extends AozoraActivity implements AddPostThreadHelper.OnPerformPost {

    int REQUEST_SELECT_TAG = 403;
    int REQUEST_SEARCH_YOUTUBE = 402;
    int REQUEST_PICK_IMAGE = 401;
    int REQUEST_SEARCH_IMAGE = 400;
    ImageData imageDataWeb = null;
    ImageData imageGallery = null;
    String youtubeID = null;
    String selectedLinkUrl = null;
    LinkData selectedLinkData = null;
    TimelinePost timelinePost = new TimelinePost();
    ParseUser postedBy;
    ParseUser postedIn;
    Boolean fetchingData = false;
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_TIMELINEPOST_PARENT = "timelinePostParent";
    public static final String PARAM_POSTEDBY = "postedBy";
    public static final String PARAM_POSTEDIN = "postedIn";
    public static final int NEW_TIMELINEPOST = 0;
    public static final int EDIT_TIMELINEPOST = 1;
    public static final int NEW_TIMELINEPOST_REPLY = 2;
    public static final int EDIT_TIMELINEPOST_REPLY = 3;
    public static final int NEW_AOTHREAD = 4;
    public static final int EDIT_AOTHREAD = 5;
    public static final int NEW_AOTHREAD_REPLY = 6;
    public static final int EDIT_AOTHREAD_REPLY = 7;
    int type;
    public static final int CONTENTTYPE_LINK = 0;
    public static final int CONTENTTYPE_IMAGE = 1;
    public static final int CONTENTTYPE_VIDEO = 2;
    int postContentType;
    Boolean hasSpoilers = false;
    Boolean isEditing = false;
    ParseObject parentPost = null;
    AddPostThreadHelper addPostThreadHelper;


    @BindView(R.id.llSpoilerText)
    LinearLayout llSpoilerText;
    @BindView(R.id.etTitle)
    EditText etTitle;
    @BindView(R.id.etComment)
    EditText etComment;
    @BindView(R.id.etSpoilerText)
    EditText etSpoilerText;
    @BindView(R.id.tvTag)
    TextView tvTag;
    @BindView(R.id.llTags)
    LinearLayout llTags;
    @BindView(R.id.ivAddPhotoInternet)
    ImageView ivAddPhotoInternet;
    @BindView(R.id.ivAddPhotoGallery)
    ImageView ivAddPhotoGallery;
    @BindView(R.id.ivAddVideo)
    ImageView ivAddVideo;
    @BindView(R.id.ivAddLink)
    ImageView ivAddLink;
    @BindView(R.id.btnForum)
    Button btnForum;
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

        postedBy = AozoraForumsApp.getPostedBy();
        postedIn = AozoraForumsApp.getPostedIn();


        switch (type){
            case NEW_TIMELINEPOST:
                setTitle("New Post");
                break;
            case NEW_TIMELINEPOST_REPLY:
                setTitle("New Post Reply");
                parentPost =  AozoraForumsApp.getUpdatedParentPost();
                ivAddLink.setVisibility(View.GONE);
                btnSpoilers.setVisibility(View.GONE);
                break;
            case NEW_AOTHREAD:
                setTitle("New Thread");
                etComment.setHint("Write thread details");
                etTitle.setVisibility(View.VISIBLE);
                llTags.setVisibility(View.VISIBLE);
                btnForum.setVisibility(View.VISIBLE);
                btnSpoilers.setVisibility(View.GONE);
                btnForum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(CreatePostActivity.this,SelectTagActivity.class);
                        startActivityForResult(i,REQUEST_SELECT_TAG);
                    }
                });
                break;
            case NEW_AOTHREAD_REPLY:
                setTitle("New Thread Reply");
                ivAddLink.setVisibility(View.GONE);
                btnSpoilers.setVisibility(View.GONE);
                break;
        }
        addPostThreadHelper = new AddPostThreadHelper(this,REQUEST_PICK_IMAGE,postedBy,postedIn,parentPost,type);
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
                    addPostThreadHelper.addPhotoGalleryTapped();
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

        if(type == NEW_TIMELINEPOST || type == NEW_AOTHREAD || type == EDIT_TIMELINEPOST || type == EDIT_AOTHREAD) {
            etComment.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    if (selectedLinkUrl == null) {
                        List<String> lstUrls = AoUtils.extractLinks(etComment.getText().toString());
                        if (lstUrls != null && lstUrls.size() > 0) {
                            String link = lstUrls.get(0);
                            Uri linkUri = Uri.parse(link);
                            String host = linkUri.getHost();
                            if (host != null && (host.contains("youtube.com") || host.contains("youtu.be"))   ) {
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

        btnSendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isValidPost()){
                    return;
                }
                if(fetchingData) {
                    AoUtils.showAlertWithText(CreatePostActivity.this, "Fetching link data...");
                    return;
                }

                btnSendComment.setText("Sending");
                btnSendComment.setBackground(ContextCompat.getDrawable(CreatePostActivity.this,R.drawable.button_sending));
                btnSendComment.requestLayout();
                btnSendComment.setEnabled(false);
                switch (type){
                    case NEW_TIMELINEPOST:
                    case NEW_TIMELINEPOST_REPLY:
                        String content = etComment.getText().toString();
                        String spoilers = etSpoilerText.getText().toString();
                        addPostThreadHelper.performTimelinePost(content,spoilers,hasSpoilers
                                ,isEditing,imageGallery,imageDataWeb, youtubeID,selectedLinkData);
                        break;
                }


            }
        });
    }


    private boolean isValidPost() {
        int max =Math.max(etSpoilerText.getText().length(),etComment.getText().length());
        if(max < 1 && imageDataWeb == null && imageGallery == null && youtubeID == null && selectedLinkData == null){
            AoUtils.showAlertWithTitleAndText(this,"Too Short","Message/spoiler should be 1 character or longer");
            return false;
        }
        //check if its muted
        if (PUser.isMuted(ParseUser.getCurrentUser()))
            return false;
        return true;
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
        } else if (requestCode == REQUEST_SELECT_TAG) {
            if(resultCode == RESULT_OK){
                ParseObject tag = AozoraForumsApp.getTagToPass();
                if(tag instanceof AoThreadTag) {
                    tvTag.setText("#"+tag.getString(AoThreadTag.NAME));
                } else {
                    tvTag.setText("#"+tag.getString(Anime.TITLE));
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
            addPostThreadHelper.openGalleryIntent();
        } else {
            Toast.makeText(this, "Permission denied. Please allow the permission", Toast.LENGTH_LONG).show();
        }
        return;
    }

    @Override
    public void onPerformPost(ParseObject post, ParseObject parentpost, ParseException e) {

        if(e!= null) {
            Toast.makeText(this,"An error occured, try again later",Toast.LENGTH_SHORT).show();
            btnSendComment.setText("Send");
            btnSendComment.setBackground(ContextCompat.getDrawable(this,R.drawable.button_send));
            btnSendComment.setEnabled(true);
        } else {
            btnSendComment.setText("Send");
            btnSendComment.setBackground(ContextCompat.getDrawable(this,R.drawable.button_send));
            btnSendComment.setEnabled(true);
            AozoraForumsApp.setUpdatedParentPost(parentPost);
            AozoraForumsApp.setUpdatedPost(post);
            Intent intent = new Intent();
            intent.putExtra("type",type);
            setResult(RESULT_OK,intent);
            finish();
        }
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
                clearAttachments();
                imageDataWeb = new ImageData();
                imageDataWeb.setHeight(theBitmap.getHeight());
                imageDataWeb.setWidth(theBitmap.getWidth());
                imageDataWeb.setUrl(imgURL);
                ivAddPhotoInternet.setColorFilter(ContextCompat.getColor(CreatePostActivity.this,R.color.red_airing));
            }
        }
    }

}
