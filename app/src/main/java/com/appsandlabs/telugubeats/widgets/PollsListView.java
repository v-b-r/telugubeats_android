package com.appsandlabs.telugubeats.widgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.helpers.ABTemplating;
import com.appsandlabs.telugubeats.helpers.ServerCalls;
import com.appsandlabs.telugubeats.models.Poll;
import com.appsandlabs.telugubeats.models.PollItem;
import com.appsandlabs.telugubeats.response_models.PollsChanged;
import com.appsandlabs.telugubeats.activities.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.appsandlabs.telugubeats.helpers.UiUtils.getColorFromResource;

/**
 * Created by abhinav on 10/1/15.
 */
public class PollsListView extends ListView {

    private final ArrayList<PollItem> polls;
    private int total = 0;
    static final int DO_POLL = 1000;
    static final int DEALYED_SERVER_CALL_TIME = 1000;
    private PollItem currentVotedItem = null;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            PollItem poll = (PollItem) msg.obj;
            ServerCalls.sendPoll(poll, new GenericListener<Boolean>() {
                @Override
                public void onData(Boolean a) {
                    //TOOD:okay
                }
            });
        }
    };

    public PollsListView(final Context context) {
        super(context);
        setAdapter(new ArrayAdapter<PollItem>(context, -1, polls = new ArrayList<PollItem>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final PollItem poll = getItem(position);
                ABTemplating.ABView pollView = (ABTemplating.ABView) convertView;
                if (pollView == null) {
                    pollView = TeluguBeatsApp.abTemplating.getPollView();
                    pollView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                }
                Picasso.with(context).load(poll.song.album.imageUrl).into(pollView.getCell("poll_image").getImage());
                pollView.getCell("poll_title").getLabel().setText(poll.song.title + " - " + poll.song.album.name);
                pollView.getCell("poll_subtitle").getLabel().setText(TextUtils.join(", ", poll.song.singers));
                pollView.getCell("poll_subtitle2").getLabel().setText(TextUtils.join(", ", poll.song.album.directors));
                pollView.getCell("poll_subtitle3").getLabel().setText(TextUtils.join(", ", poll.song.album.actors));
                float pollPercentage = (poll.pollCount * 1.0f) / total;
                pollView.getCell("poll_percentage").wgt(pollPercentage);
                pollView.getCell("poll_percentage").setBackgroundColor(poll.color);
                pollView.getCell("poll_count").getLabel().setText(poll.pollCount + "");
                pollView.getCell("poll_count").wgt(1.0f - pollPercentage);
                final ABTemplating.ABView finalPollView = pollView;

                pollView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doUserPoll(poll);
                        finalPollView.getCell("voted").setBackgroundColor(getColorFromResource(R.color.malachite));

                        mHandler.removeMessages(DO_POLL);
                        Message msg = mHandler.obtainMessage(DO_POLL, poll);//create a new message
                        mHandler.sendMessageDelayed(msg, DEALYED_SERVER_CALL_TIME);
                        notifyDataSetChanged();
                    }
                });

                pollView.setFocusable(false);
                if(poll.isVoted) {
                    pollView.getCell("voted").setBackgroundColor(getColorFromResource(R.color.malachite));
                }
                else{
                    pollView.getCell("voted").setBackgroundColor(Color.TRANSPARENT);
                }

                return pollView;
            }

        });
        setDivider(null);
//        setExpanded(true);
//        setScrollContainer(false);
    }

    private synchronized void doUserPoll(PollItem poll) {
        if(currentVotedItem!=null) {
            currentVotedItem.isVoted = false;
            currentVotedItem.pollCount--;
            total--;
        }
        poll.isVoted = true;
        currentVotedItem  = poll;
        poll.pollCount++;
        total++;
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
        currentVotedItem = null;
        for(PollItem pollItem : poll.pollItems){
            this.polls.add(pollItem);
            pollItem.color = TeluguBeatsApp.getUiUtils().generateRandomColor(Color.WHITE);
            if(pollItem.isVoted)
                currentVotedItem = pollItem;
        }
        caulculateTotalPolls();
        ((ArrayAdapter)getAdapter()).notifyDataSetChanged();
    }

    public void pollsChanged(PollsChanged data) {
        for(PollsChanged.PollChange change : data.pollChanges) {
            for (PollItem poll : polls) {
                if (change.pollId.equals(poll.id.toString())) {
                    poll.pollCount += change.count;
                }
            }
        }
        notifyDataSetChanged();

    }

    private void notifyDataSetChanged() {
        ((ArrayAdapter)getAdapter()).notifyDataSetChanged();
    }
}
