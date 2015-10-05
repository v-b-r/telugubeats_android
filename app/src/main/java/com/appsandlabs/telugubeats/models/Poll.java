package com.appsandlabs.telugubeats.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by abhinav on 10/1/15.
 */
public class Poll extends  BaseModel{
    @SerializedName("poll_items")
    public List<PollItem> pollItems;
    @SerializedName("stream_id")
    public String streamId;
}
