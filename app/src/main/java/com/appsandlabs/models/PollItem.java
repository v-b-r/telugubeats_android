package com.appsandlabs.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by abhinav on 10/1/15.
 */
public class PollItem extends  BaseModel{
    public List<String> userFriends;
    @SerializedName("poll_count")
    public int pollCount;
    public Song song;
    public int color;
}
