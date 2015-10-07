package com.appsandlabs.telugubeats.widgets;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appsandlabs.telugubeats.R;
import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.helpers.ServerCalls;
import com.appsandlabs.telugubeats.models.Poll;
import com.appsandlabs.telugubeats.models.PollItem;
import com.appsandlabs.telugubeats.response_models.PollsChanged;
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
    static final int DEALYED_SERVER_CALL_TIME = 5000;
    private PollItem currentVotedItem = null;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            PollItem pollItem = (PollItem) msg.obj;
            ServerCalls.sendPoll(pollItem, new GenericListener<Boolean>() {
                @Override
                public void onData(Boolean a) {
                    //TOOD:okay
                }
            });
        }
    };




    public static class UiHandle{

        ImageView pollImage;
        TextView pollTitle;
        TextView voted;
        TextView pollSubtitle;
        TextView pollSubtitle2;
        TextView pollSubtitle3;
        LinearLayout pollPercentage;
        TextView pollCount;

    }


    public UiHandle initUiHandle(ViewGroup layout){
        UiHandle uiHandle = new UiHandle();
        uiHandle.pollImage = (ImageView)layout.findViewById(R.id.poll_image);
        uiHandle.pollTitle = (TextView)layout.findViewById(R.id.poll_title);
        uiHandle.voted = (TextView)layout.findViewById(R.id.voted);
        uiHandle.pollSubtitle = (TextView)layout.findViewById(R.id.poll_subtitle);
        uiHandle.pollSubtitle2 = (TextView)layout.findViewById(R.id.poll_subtitle2);
        uiHandle.pollSubtitle3 = (TextView)layout.findViewById(R.id.poll_subtitle3);
        uiHandle.pollPercentage = (LinearLayout)layout.findViewById(R.id.poll_percentage);
        uiHandle.pollCount = (TextView)layout.findViewById(R.id.poll_count);
        return uiHandle;
    }



    public PollsListView(final Context context, AttributeSet attrs) {
        super(context, attrs);
        setAdapter(new ArrayAdapter<PollItem>(context, -1, polls = new ArrayList<PollItem>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final PollItem poll = getItem(position);
                UiHandle uiHandle;
                LinearLayout pollView = (LinearLayout) convertView;
                if (pollView == null) {
                    pollView = (LinearLayout) View.inflate(getContext(), R.layout.poll_item_view, null);
                    uiHandle = initUiHandle(pollView);
                    pollView.setTag(uiHandle);
                }
                uiHandle = (UiHandle) pollView.getTag();

                Picasso.with(context).load(poll.song.album.imageUrl).into(uiHandle.pollImage);
                uiHandle.pollTitle.setText(poll.song.title + " - " + poll.song.album.name);
                uiHandle.pollSubtitle.setText(TextUtils.join(", ", poll.song.singers));
                uiHandle.pollSubtitle2.setText(TextUtils.join(", ", poll.song.album.directors));
                if(poll.song.album.actors!=null && poll.song.album.actors.size()>0) {
                    uiHandle.pollSubtitle3.setVisibility(View.VISIBLE);
                    uiHandle.pollSubtitle3.setText(TextUtils.join(", ", poll.song.album.actors));
                }
                else{
                    uiHandle.pollSubtitle3.setVisibility(View.GONE);
                }


                if (poll.pollCount > 0) {
                    ((ViewGroup) uiHandle.pollPercentage.getParent()).setVisibility(View.VISIBLE);
                    float pollPercentage = (poll.pollCount * 1.0f) / total;
                    ((LinearLayout.LayoutParams) uiHandle.pollPercentage.getLayoutParams()).weight = pollPercentage;
                    uiHandle.pollCount.setText(poll.pollCount == 0 ? "0 votes" : "" + poll.pollCount + " votes");
                    //pollView.getCell("dummy").wgt(1.0f - pollPercentage);
                    uiHandle.pollPercentage.setBackgroundColor(poll.color);
                } else {
                    ((ViewGroup) uiHandle.pollPercentage.getParent()).setVisibility(View.GONE);
                }
                final UiHandle finalUiHandle = uiHandle;
                pollView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (poll.isVoted) return;
                        doUserPoll(poll);
                        finalUiHandle.voted.setBackgroundColor(getColorFromResource(R.color.malachite));

                        mHandler.removeMessages(DO_POLL);
                        Message msg = mHandler.obtainMessage(DO_POLL, poll);//create a new message
                        mHandler.sendMessageDelayed(msg, DEALYED_SERVER_CALL_TIME);
                        notifyDataSetChanged();
                    }
                });

                pollView.setFocusable(false);
                if (poll.isVoted) {
                    uiHandle.voted.setBackgroundColor(getColorFromResource(R.color.malachite));
                } else {
                    uiHandle.voted.setBackgroundColor(Color.TRANSPARENT);
                }

                return pollView;
            }

        });
//        setExpanded(true);
//        setScrollContainer(false);


    }

    private synchronized void doUserPoll(PollItem poll) {
        if(currentVotedItem!=null) {
            currentVotedItem.isVoted = false;
            if(currentVotedItem.pollCount>0) {
                currentVotedItem.pollCount--;
                total--;
            }
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
                    poll.pollCount = Math.max(0 , poll.pollCount);
                }
            }
        }
        notifyDataSetChanged();

    }

    private void notifyDataSetChanged() {
        ((ArrayAdapter)getAdapter()).notifyDataSetChanged();
    }
}
