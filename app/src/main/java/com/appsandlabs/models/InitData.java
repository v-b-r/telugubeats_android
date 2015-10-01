package com.appsandlabs.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by abhinav on 10/1/15.
 */
public class InitData {
    public Poll poll;
    @SerializedName("current_song")
    public Song currentSong;
}
