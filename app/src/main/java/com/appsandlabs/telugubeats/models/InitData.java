package com.appsandlabs.telugubeats.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abhinav on 10/1/15.
 */
public class InitData {
    public Poll poll;
    @SerializedName("current_song")
    public Song currentSong;

    @SerializedName("user_poll_item_id")
    public String userPollItemId;
    public User user;


    public PollItem setCurrentPoll(){
        if(userPollItemId!=null){
            for(PollItem pollItem : poll.pollItems){
                if(pollItem.id.getId().equalsIgnoreCase(userPollItemId)){
                    pollItem.isVoted = true;
                    return pollItem;
                }
            }
        }
        return null;
    }
}
