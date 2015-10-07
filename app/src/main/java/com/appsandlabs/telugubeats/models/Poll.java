package com.appsandlabs.telugubeats.models;

import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.response_models.PollsChanged;
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



    public static PollItem getChangedPoll(PollsChanged data){
        for(PollsChanged.PollChange change : data.pollChanges) {
            for (PollItem pollItem : TeluguBeatsApp.currentPoll.pollItems) {
                if (change.pollId.equals(pollItem.id.toString())) {
                    return pollItem;
                }
            }
        }
        return null;
    }

    public static boolean isModifiedPoll(PollsChanged data){
        for(PollsChanged.PollChange change : data.pollChanges) {
            for (PollItem pollItem : TeluguBeatsApp.currentPoll.pollItems) {
                if (change.pollId.equals(pollItem.id.toString())) {
                    if(change.count<0)
                        return true;
                }
            }
        }
        return false;
    }
}
