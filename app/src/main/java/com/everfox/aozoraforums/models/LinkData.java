package com.everfox.aozoraforums.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Daniel on 02/03/2017.
 */

public class LinkData {

    String url;
    String type;
    String title;
    String description;
    String siteName;
    String imageUrl;
    String[] relatedImages;
    int imageWidth;
    int imageHeight;
    JSONArray images;

    public JSONArray getImages() {
        return images;
    }

    public void setImages(JSONArray images) {
        this.images = images;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String[] getRelatedImages() {
        return relatedImages;
    }

    public void setRelatedImages(String[] relatedImages) {
        this.relatedImages = relatedImages;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public static LinkData mapHashMap(HashMap dictionary) {
        LinkData linkData = new LinkData();
        try {
            if (dictionary.containsKey("url") && dictionary.get("url") != null) {
                linkData.setUrl(dictionary.get("url").toString());
            }
            if (dictionary.containsKey("type") && dictionary.get("type") != null) {
                linkData.setType(dictionary.get("type").toString());
            }
            if (dictionary.containsKey("title") && dictionary.get("title") != null) {
                linkData.setTitle(dictionary.get("title").toString());
            }
            if (dictionary.containsKey("image") && dictionary.get("image") != null) {
                linkData.setImageUrl(dictionary.get("image").toString());
                JSONArray jsonArray = new JSONArray();
                jsonArray.put(dictionary.get("image"));
                linkData.setImages(jsonArray);
            }
            if (dictionary.containsKey("description") && dictionary.get("description") != null) {
                linkData.setDescription(dictionary.get("description").toString());
            }
            if (dictionary.containsKey("image_width")) {
                linkData.setImageWidth(Integer.valueOf(dictionary.get("image_width").toString()));
            }
            if (dictionary.containsKey("image_height")) {
                linkData.setImageWidth(Integer.valueOf(dictionary.get("image_height").toString()));
            }
            if (dictionary.containsKey("siteName") && dictionary.get("siteName") != null) {
                linkData.setSiteName(dictionary.get("siteName").toString());
            }

            //TODO: RELATED IMAGES

        } catch (Exception ex ){

        }
        return linkData;
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", getTitle());
            jsonObject.put("description", getDescription());
            jsonObject.put("url", getUrl());
            jsonObject.put("images", getImages());
        } catch (JSONException jEx) {

        }
        return jsonObject;
    }
}
