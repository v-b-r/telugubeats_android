package com.appsandlabs.telugubeats.fragments;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appsandlabs.telugubeats.R;
import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.helpers.UiUtils;
import com.appsandlabs.telugubeats.interfaces.AppEventListener;
import com.appsandlabs.telugubeats.response_models.PollsChanged;
import com.appsandlabs.telugubeats.widgets.PollsListView;

/**
 * Created by abhinav on 10/2/15.
 */
public class PollsFragment extends Fragment implements AppEventListener {
    private AppEventListener blurredBgListener;
    private LinearLayout layout;

    @Override
    public void onEvent(TeluguBeatsApp.NotifierEvent type, Object data) {
        switch (type){
            case POLLS_CHANGED:
                uiHandle.livePollsList.pollsChanged((PollsChanged) data);
        }
    }

    public static class UiHandle{

        TextView livePollsHeading;
        PollsListView livePollsList;

    }

    UiHandle uiHandle = new UiHandle();

    public UiHandle initUiHandle(ViewGroup layout){

        uiHandle.livePollsHeading = (TextView)layout.findViewById(R.id.live_polls_heading);
        uiHandle.livePollsList = (PollsListView)layout.findViewById(R.id.live_polls_list);

        return uiHandle;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = (LinearLayout) inflater.inflate(R.layout.polls_fragment_layout, null);
        initUiHandle(layout);
        Context ctx = inflater.getContext();
        if (TeluguBeatsApp.currentPoll != null)
            uiHandle.livePollsList.resetPolls(TeluguBeatsApp.currentPoll);


        if(TeluguBeatsApp.blurredCurrentSongBg!=null){
            UiUtils.setBg(layout, new BitmapDrawable(TeluguBeatsApp.blurredCurrentSongBg));
        }

        blurredBgListener = new AppEventListener() {
            @Override
            public void onEvent(TeluguBeatsApp.NotifierEvent type, Object data) {
                UiUtils.setBg(layout, new BitmapDrawable(TeluguBeatsApp.blurredCurrentSongBg));
            }
        };
        TeluguBeatsApp.addListener(TeluguBeatsApp.NotifierEvent.BLURRED_BG_AVAILABLE, blurredBgListener);

        return layout;
    }


    @Override
    public void onResume() {
        TeluguBeatsApp.addListener(TeluguBeatsApp.NotifierEvent.POLLS_CHANGED, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        TeluguBeatsApp.removeListener(TeluguBeatsApp.NotifierEvent.POLLS_CHANGED, this);
        super.onPause();
    }
}