package com.rahuljindal.simplepictureapp.Modals;

/**
 * Created by Anuj Jindal on 3/21/2018.
 */

public class CompressedImageDataModal {
    private String ImageUrl;
    private String ImageDownloaded;

    public CompressedImageDataModal() {
    }

    public CompressedImageDataModal(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getImageDownloaded() {
        return ImageDownloaded;
    }

    public void setImageDownloaded(String imageDownloaded) {
        ImageDownloaded = imageDownloaded;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }
}
