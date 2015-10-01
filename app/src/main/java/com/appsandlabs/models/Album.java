package com.appsandlabs.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by abhinav on 10/1/15.
 */
public class Album extends  BaseModel{
    public String name;
    public List<String> directors;
    public List<String> actors;
    @SerializedName("music_directors")
    public List<String> musicDirectors;
    public String imageUrl;
}
