package com.appsandlabs.telugubeats.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abhinav on 10/4/15.
 */
public class Event {
    @SerializedName("event_id")
    public String eventId;
    public String payload;
    public User user;

}
