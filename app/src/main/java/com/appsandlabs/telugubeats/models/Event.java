package com.appsandlabs.telugubeats.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abhinav on 10/4/15.
 */
public class Event {

    public enum EventId{
        SONG_CHANGED,
        POLLS_CHANGED,
        CHAT_MESSAGE,
        DEDICATE;
    }

    @SerializedName("event_id")
    public EventId eventId;
    public String payload;
    @SerializedName("from_user")
    public User fromUser;




}
