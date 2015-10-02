package com.appsandlabs.interfaces;

import com.appsandlabs.TeluguBeatsApp;

/**
 * Created by abhinav on 10/2/15.
 */
public interface AppEventListener {

    void onEvent(TeluguBeatsApp.AppEvent type, Object data);
}
