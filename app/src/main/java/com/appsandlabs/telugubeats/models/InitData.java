package com.appsandlabs.telugubeats.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abhinav on 10/1/15.
 */
public class InitData {
    public Poll poll;
    @SerializedName("current_song")
    public Song currentSong;
}