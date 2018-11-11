package com.rahuljindal.simplepictureapp.Modals;

import java.io.Serializable;

public class ImageDataModal implements Serializable {

    private String imageUrl;
    private String imageDescription;

    public ImageDataModal() {
    }

    public ImageDataModal(String imageUrl, String imageDescription) {
        this.imageUrl = imageUrl;
        this.imageDescription = imageDescription;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public void setImageDescription(String imageDescription) {
        this.imageDescription = imageDescription;
    }

}
