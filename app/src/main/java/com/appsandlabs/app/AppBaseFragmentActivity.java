package com.appsandlabs.app;

import android.support.v4.app.FragmentActivity;

import com.appsandlabs.TeluguBeatsApp;

/**
 * Created by abhinav on 9/27/15.
 */
public class AppBaseFragmentActivity extends FragmentActivity {

    @Override
    protected void onPause() {
        TeluguBeatsApp.onActivityPaused(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        TeluguBeatsApp.onActivityResumed(this);
        super.onResume();
    }
}
