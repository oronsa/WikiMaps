package com.example.oronsa.wikimaps;


import android.graphics.Bitmap;

class Item
{
    private String title;
    private Bitmap image;


     String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    Bitmap getImage() {
        return image;
    }

    void setImage(Bitmap image) {
        this.image = image;
    }

}
