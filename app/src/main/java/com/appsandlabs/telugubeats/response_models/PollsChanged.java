package com.appsandlabs.telugubeats.response_models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by abhinav on 10/3/15.
 */


public class PollsChanged {

    public static class PollChange{
        @SerializedName("poll_id")
        public String pollId;
        public int count; // inc / dec alue
    }

    @SerializedName("poll_changes")
    public List<PollChange> pollChanges;
}
