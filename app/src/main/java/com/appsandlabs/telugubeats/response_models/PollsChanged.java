package com.appsandlabs.telugubeats.response_models;

import java.util.List;

/**
 * Created by abhinav on 10/3/15.
 */


public class PollsChanged {

    public static class PollChange{
        public String pollId;
        public int count; // inc / dec alue
    }
    public List<PollChange> pollChanges;
}
