package com.everfox.aozoraforums.models;

import java.io.Serializable;

/**
 * Created by Daniel on 01/03/2017.
 */

public class ImageData implements Serializable {

    public byte[] getImageFile() {
        return imageFile;
    }

    public void setImageFile(byte[] imageFile) {
        this.imageFile = imageFile;
    }

    byte[] imageFile;
    int width;
    int height;
    String imageURL;

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
