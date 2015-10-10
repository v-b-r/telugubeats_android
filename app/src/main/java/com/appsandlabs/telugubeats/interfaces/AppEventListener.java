package com.appsandlabs.telugubeats.interfaces;

import com.appsandlabs.telugubeats.TeluguBeatsApp;

/**
 * Created by abhinav on 10/2/15.
 */
public interface AppEventListener {

    void onEvent(TeluguBeatsApp.NotifierEvent type, Object data);
}
