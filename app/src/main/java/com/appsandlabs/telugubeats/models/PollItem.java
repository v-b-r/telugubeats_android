package com.appsandlabs.telugubeats.models;

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

    @SerializedName("is_voted")
    public boolean isVoted;
    public Id poll;


    public boolean _is_added;
}
