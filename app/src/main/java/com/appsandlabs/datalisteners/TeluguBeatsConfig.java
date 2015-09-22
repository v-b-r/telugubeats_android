package com.appsandlabs.datalisteners;

import android.app.Application;

import com.appsandlabs.telugubeats.R;

import java.io.InputStream;

public class TeluguBeatsConfig extends Application{

    public static GenericListener<float[]> onFFTData;
    public static InputStream sfd_ser ;

    @Override
    public void onCreate() {
        super.onCreate();
        sfd_ser = getApplicationContext().getResources().openRawResource(R.raw.sfd);
    }

}
