package com.appsandlabs.telugubeats.widgets;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.appsandlabs.TeluguBeatsApp;
import com.appsandlabs.com.appsandlabs.helpers.ABTemplating;
import com.appsandlabs.models.Poll;
import com.appsandlabs.models.PollItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhinav on 10/1/15.
 */
public class PollsListView extends ListView {

    private final ArrayList<PollItem> polls;
    private int total = 0;

    public PollsListView(final Context context) {
        super(context);
        setAdapter(new ArrayAdapter<PollItem>(context, -1 , polls = new ArrayList<PollItem>()){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                PollItem poll = getItem(position);
                ABTemplating.ABView pollView = (ABTemplating.ABView) convertView;
                if(convertView==null){
                    pollView = TeluguBeatsApp.abTemplating.getPollView();
                }
                Picasso.with(context).load(poll.song.album.imageUrl).into(pollView.getCell("poll_image").getImage());
                pollView.getCell("poll_title").getLabel().setText(poll.song.title + " - " + poll.song.album.name);
                pollView.getCell("poll_subtitle").getLabel().setText(TextUtils.join(", ", poll.song.singers));
                pollView.getCell("poll_percentage").wgt(poll.pollCount*1.0f/total *100);
                return convertView;
            }
        });
    }

    public int caulculateTotalPolls(){
        total = 0;
        for(int i=0;i< polls.size();i++){
            total += polls.get(i).pollCount;
        }
        return total;
    }
    public void resetPolls(Poll poll){
        this.polls.clear();
        caulculateTotalPolls();
        for(PollItem pollItem : poll.pollItems){
            this.polls.add(pollItem);
            pollItem.color = TeluguBeatsApp.getUiUtils().generateRandomColor(Color.WHITE);
        }
        ((ArrayAdapter)getAdapter()).notifyDataSetChanged();
    }


}
