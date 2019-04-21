package com.example.zmorgan.deafradio;

public class Radio {

    private String Title;
    private String Category;
    private String Description;
    private String URL;
    private int Thumbnail;


    public Radio() {
    }

    public Radio(String title, String category, String description, int thumbnail, String url) {
        //Class to represent a radio station, stores title, category, description, thumbnail and a url
        Title = title;
        Category = category;
        Description = description;
        Thumbnail = thumbnail;
        URL = url;
    }

    public String getTitle() {
        return Title;
    }

    public String getURL() { return URL;}

    public String getCategory() {
        return Category;
    }

    public String getDescription() {
        return Description;
    }

    public int getThumbnail() {
        return Thumbnail;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setThumbnail(int thumbnail) {
        Thumbnail = thumbnail;
    }
}
