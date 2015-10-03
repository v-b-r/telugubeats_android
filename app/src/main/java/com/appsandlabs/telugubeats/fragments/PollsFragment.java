package com.appsandlabs.telugubeats.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.helpers.ABTemplating;
import com.appsandlabs.telugubeats.interfaces.AppEventListener;
import com.appsandlabs.telugubeats.response_models.PollsChanged;
import com.appsandlabs.telugubeats.widgets.PollsListView;

/**
 * Created by abhinav on 10/2/15.
 */
public class PollsFragment extends Fragment implements AppEventListener {
    @Override
    public void onEvent(TeluguBeatsApp.AppEvent type, Object data) {
        switch (type){
            case POLLS_CHANGED:
                uiHandle.polls.pollsChanged((PollsChanged) data);
        }
    }

    private static class UiHandle {
        TextView pollsHeading;
        PollsListView polls;
    }

    UiHandle uiHandle = new UiHandle();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ABTemplating.ABView layout = TeluguBeatsApp.abTemplating.getPollsView();
        Context ctx = inflater.getContext();
        uiHandle.pollsHeading = layout.getCell("live_polls_heading").getLabel();
        layout.getCell("live_polls_list").addView(uiHandle.polls = new PollsListView(ctx));
        if (TeluguBeatsApp.currentPoll != null)
            uiHandle.polls.resetPolls(TeluguBeatsApp.currentPoll);
        return layout;
    }


    @Override
    public void onResume() {
        TeluguBeatsApp.addListener(TeluguBeatsApp.AppEvent.POLLS_CHANGED, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        TeluguBeatsApp.removeListener(TeluguBeatsApp.AppEvent.POLLS_CHANGED, this);
        super.onPause();
    }
}