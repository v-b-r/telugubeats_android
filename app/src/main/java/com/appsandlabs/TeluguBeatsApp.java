package com.appsandlabs;

import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.appsandlabs.com.appsandlabs.helpers.ABTemplating;
import com.appsandlabs.com.appsandlabs.helpers.UiUtils;
import com.appsandlabs.datalisteners.GenericListener;
import com.appsandlabs.enums.NotifificationProcessingState;
import com.appsandlabs.telugubeats.MainActivity;
import com.appsandlabs.telugubeats.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by abhinav on 7/13/15.
 */
public class TeluguBeatsApp extends Application {
    /**
     * The Analytics singleton. The field is set in onCreate method override when the application
     * class is initially created.
     */
    private static GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is
     * initially created.
     */
    private static Tracker tracker;
    public static HashMap<NotificationReciever.NotificationType, ArrayList<NotificationReciever.NotificationPayload>> pendingNotifications = new HashMap<NotificationReciever.NotificationType, ArrayList<NotificationReciever.NotificationPayload>>();
    public static NotifificationProcessingState nState = NotifificationProcessingState.CONTINUE;
    private static UiUtils uiUtils;
    private static Context applicationContext;
    private static FragmentActivity currentActivity;
    public static ABTemplating abTemplating;
    public static GenericListener<float[]> onFFTData;
    public static InputStream sfd_ser ;
    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either
     * set android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
     */
    public static GoogleAnalytics analytics() {
        return analytics;
    }

    /**
     * The default app tracker. If this method returns null you forgot to either set
     * android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.tracker field in onCreate method override.
     */
    public static Tracker tracker() {
        return tracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sfd_ser = getApplicationContext().getResources().openRawResource(R.raw.sfd);
        applicationContext = getApplicationContext();
        uiUtils = new UiUtils(this);
        abTemplating = new ABTemplating(this);
        nActivities = new AtomicInteger(0);

        analytics = GoogleAnalytics.getInstance(this);

        // TODO: Replace the tracker-id with your app one from https://www.google.com/analytics/web/
        tracker = analytics.newTracker(getResources().getString(R.string.ga_trackingId));

        // Provide unhandled exceptions reports. Do that first after creating the tracker
       tracker.enableExceptionReporting(true);

        // Enable Remarketing, Demographics & Interests reports
        // https://developers.google.com/analytics/devguides/collection/android/display-features
//        tracker.enableAdvertisingIdCollection(true);

        // Enable automatic activity tracking for your app
        tracker.enableAutoActivityTracking(true);

        TeluguBeatsApp.tracker().send(new HitBuilders.EventBuilder()
                .setCategory(Tracking.APP_ACTIVITY.toString())
                .setAction(Tracking.LAUNCH.toString())
                .build());
    }

    public static UiUtils getUiUtils() {
        return uiUtils;
    }

    public static Context getContext() {
        return currentActivity !=null ? currentActivity  : applicationContext;
    }

    public static FragmentActivity getCurrentActivity() {
        return currentActivity;
    }


    public static void onAllActivitiesDestroyed(){
        uiUtils = null;
        applicationContext = null;
        currentActivity = null;
    }


    public static AtomicInteger nActivities = new AtomicInteger(0);
    public static void onActivityDestroyed(FragmentActivity activity) {
        int d = nActivities.decrementAndGet();
        if(d==0){
            onAllActivitiesDestroyed();
        }
    }

    public static void onActivityCreated(FragmentActivity activity) {
        nActivities.incrementAndGet();
    }

    public static void onActivityPaused(FragmentActivity activity){
        currentActivity = null;
    }

    public static void onActivityResumed(FragmentActivity activity){
        currentActivity = activity;
    }

}