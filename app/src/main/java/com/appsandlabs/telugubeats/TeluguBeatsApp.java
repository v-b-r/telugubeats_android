package com.appsandlabs.telugubeats;

import android.app.Application;
import android.content.Context;
import android.support.v4.app.FragmentActivity;

import com.appsandlabs.telugubeats.activities.AppBaseFragmentActivity;
import com.appsandlabs.telugubeats.helpers.ABTemplating;
import com.appsandlabs.telugubeats.helpers.UiUtils;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.enums.NotifificationProcessingState;
import com.appsandlabs.telugubeats.interfaces.AppEventListener;
import com.appsandlabs.telugubeats.models.Poll;
import com.appsandlabs.telugubeats.models.Song;
import com.appsandlabs.telugubeats.activities.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static AppBaseFragmentActivity currentActivity;
    public static ABTemplating abTemplating;
    public static GenericListener<float[]> onFFTData;
    public static InputStream sfd_ser ;
    private static UserDeviceManager userDeviceManager;



    public static Poll currentPoll= null;
    public static Song currentSong;
    public Gson gson = new Gson();

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
        userDeviceManager = new UserDeviceManager(this);
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
    public static UserDeviceManager getUserDeviceManager() {
        return userDeviceManager;
    }
    public static Context getContext() {
        return currentActivity !=null ? currentActivity  : applicationContext;
    }

    public static AppBaseFragmentActivity getCurrentActivity() {
        return currentActivity;
    }


    public static void onAllActivitiesDestroyed(){
        uiUtils = null;
        applicationContext = null;
        currentActivity = null;
        eventListeners.clear();
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

    public static void onActivityResumed(AppBaseFragmentActivity activity){
        currentActivity = activity;
    }




    public enum AppEvent {
        NONE, POLLS_CHANGED;

        public Object getValue() {
            return value;
        }

        Object value;

        public AppEvent setValue(Object val){
            this.value = val;
            return this;
        }

    }
    static HashMap<String, List<AppEventListener>> eventListeners = new HashMap<String, List<AppEventListener>>();

    private static synchronized  void addListener(String id , AppEventListener listener){
        if(eventListeners.get(id)==null){
            eventListeners.put(id, new ArrayList<AppEventListener>());
        }
        eventListeners.get(id).add(listener);

    }

    public static synchronized void removeListener(String id, AppEventListener listener){
        if (eventListeners.get(id) == null) {
            return;
        }
        eventListeners.get(id).remove(listener);
    }


    public synchronized static void removeListeners(AppEvent event, String permission) {
        String id = event.toString() + (permission == null ? "" : permission);
        eventListeners.remove(id);
    }


    public synchronized static  void addListener(AppEvent type, AppEventListener listener){
        addListener(type.toString(), listener);
    }


    public static synchronized  void addListener(AppEvent type, String permission, AppEventListener listener){
        String listenerId = type.toString()+permission;
        addListener(listenerId, listener);
    }


    public static synchronized void removeListener(AppEvent type, AppEventListener listener) {
        removeListener(type.toString(), listener);
    }

    public synchronized void removeListener(AppEvent type, String permission , AppEventListener listener) {
        String listenerId = type.toString()+permission;
        removeListener(listenerId, listener);
    }

    public static synchronized void broadcastEvent(AppEvent type , Object data){
        String id = type.toString();
        if (eventListeners.get(id) == null) {
            return;
        }
        for(AppEventListener listener : eventListeners.get(id)){
            sendBroadcast(listener, type, data);
        }
    }
    public static void sendBroadcast(final AppEventListener listener, final AppEvent type, final Object data){
        if(getCurrentActivity()!=null) {
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onEvent(type, data);
                }
            });
        }
        else {
            listener.onEvent(type, data);
        }
    }

    public static synchronized void broadcastEvent(AppEvent type ,String permission , Object data){
        String id = type.toString()+permission;
        if (eventListeners.get(id) == null) {
            return;
        }
        for(AppEventListener listener : eventListeners.get(id)){
            sendBroadcast(listener , type, data);
        }
    }




}