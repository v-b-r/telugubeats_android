package com.appsandlabs.telugubeats;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import com.appsandlabs.telugubeats.activities.AppBaseFragmentActivity;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.enums.NotifificationProcessingState;
import com.appsandlabs.telugubeats.helpers.ServerCalls;
import com.appsandlabs.telugubeats.helpers.UiUtils;
import com.appsandlabs.telugubeats.interfaces.AppEventListener;
import com.appsandlabs.telugubeats.models.Event;
import com.appsandlabs.telugubeats.models.Poll;
import com.appsandlabs.telugubeats.models.PollItem;
import com.appsandlabs.telugubeats.models.Song;
import com.appsandlabs.telugubeats.models.User;
import com.appsandlabs.telugubeats.response_models.PollsChanged;
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
    public static GenericListener<float[]> onFFTData;
    public static InputStream sfd_ser ;
    private static UserDeviceManager userDeviceManager;



    public static Poll currentPoll= null;
    public static Song currentSong;
    public static Gson gson = new Gson();
    public static User currentUser;
    public static Handler onSongChanged= null;
    public static Handler onSongPlayPaused = null;
    public static Handler showDeletenotification= null;
    public static Bitmap blurredCurrentSongBg = null;
    private static List<String> lastFewFeedEvents = new ArrayList<>();
    private static ServerCalls serverCalls;
    public static Bitmap songAlbumArt = null;

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

    public static List<String> getLastFewFeedEvents() {
        return lastFewFeedEvents;
    }

    public static ServerCalls getServerCalls() {
        return serverCalls;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        sfd_ser = getApplicationContext().getResources().openRawResource(R.raw.sfd);
        applicationContext = getApplicationContext();
        uiUtils = new UiUtils(this);
        userDeviceManager = new UserDeviceManager(this);
        serverCalls = new ServerCalls(this);

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
        if(TeluguBeatsApp.showDeletenotification!=null)
            TeluguBeatsApp.showDeletenotification.sendMessage( TeluguBeatsApp.showDeletenotification.obtainMessage());
        uiUtils = null;
        applicationContext = null;
        currentActivity = null;
        eventListeners.clear();
        blurredCurrentSongBg = null;
        serverCalls.closeAll();
        serverCalls = null;
        TeluguBeatsApp.showDeletenotification = null;
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


    public static void onEvent(String eventString){
        Event event = TeluguBeatsApp.gson.fromJson(eventString , Event.class);
        onEvent(event, true);
    }
    public static void onEvent(String eventString, boolean doBroadcast){
        Event event = TeluguBeatsApp.gson.fromJson(eventString , Event.class);
        onEvent(event, doBroadcast);
    }
    public static void onEvent(Event event, boolean doBroadcast) {
            if(event==null) return;
            Object payload = null;
            User eventUser = event.fromUser;
            String feed;
            switch (event.eventId){

                case POLLS_CHANGED:
                    PollsChanged pollsChanged = TeluguBeatsApp.gson.fromJson(event.payload, PollsChanged.class);
                    PollItem changedPollItem = Poll.getChangedPoll(pollsChanged);
                    feed = event.fromUser.name+ " voted up for "+ (changedPollItem!=null ? changedPollItem.song.title: " song ");
                    TeluguBeatsApp.lastFewFeedEvents.add(feed);
                    if(doBroadcast) {
                        TeluguBeatsApp.broadcastEvent(NotifierEvent.GENERIC_FEED, feed);
                    }


                    if(!TeluguBeatsApp.currentUser.isSame(eventUser) && doBroadcast){
                        TeluguBeatsApp.broadcastEvent(NotifierEvent.POLLS_CHANGED, pollsChanged);
                    }
                    break;
                case DEDICATE:
                    feed = event.fromUser.name + " has dedicated this song to " + event.payload ;
                    TeluguBeatsApp.lastFewFeedEvents.add(feed);
                    if(doBroadcast) {
                        TeluguBeatsApp.broadcastEvent(NotifierEvent.GENERIC_FEED, feed);
                    }
                    break;

                case CHAT_MESSAGE:
                    feed = event.fromUser.name + ": "+event.payload;
                    TeluguBeatsApp.lastFewFeedEvents.add(feed);
                    if(doBroadcast) {
                        TeluguBeatsApp.broadcastEvent(NotifierEvent.GENERIC_FEED, feed);
                    }
                    break;
                case SONG_CHANGED:
                    if(doBroadcast) {
                        currentSong = TeluguBeatsApp.gson.fromJson(event.payload, Song.class);
                        blurredCurrentSongBg = null;
                        songAlbumArt = null;
                        TeluguBeatsApp.broadcastEvent(NotifierEvent.SONG_CHANGED,  null);
                    }
                    break;
            }
    }


    public enum NotifierEvent {
        NONE, POLLS_CHANGED, BLURRED_BG_AVAILABLE, GENERIC_FEED, SONG_CHANGED;

        public Object getValue() {
            return value;
        }

        Object value;

        public NotifierEvent setValue(Object val){
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


    public synchronized static void removeListeners(NotifierEvent event, String permission) {
        String id = event.toString() + (permission == null ? "" : permission);
        eventListeners.remove(id);
    }


    public synchronized static  void addListener(NotifierEvent type, AppEventListener listener){
        addListener(type.toString(), listener);
    }


    public static synchronized  void addListener(NotifierEvent type, String permission, AppEventListener listener){
        String listenerId = type.toString()+permission;
        addListener(listenerId, listener);
    }


    public static synchronized void removeListener(NotifierEvent type, AppEventListener listener) {
        removeListener(type.toString(), listener);
    }

    public synchronized void removeListener(NotifierEvent type, String permission , AppEventListener listener) {
        String listenerId = type.toString()+permission;
        removeListener(listenerId, listener);
    }

    public static synchronized void broadcastEvent(NotifierEvent type , Object data){
        String id = type.toString();
        if (eventListeners.get(id) == null) {
            return;
        }
        for(AppEventListener listener : eventListeners.get(id)){
            sendBroadcast(listener, type, data);
        }
    }
    public static void sendBroadcast(final AppEventListener listener, final NotifierEvent type, final Object data){
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

    public static synchronized void broadcastEvent(NotifierEvent type ,String permission , Object data){
        String id = type.toString()+permission;
        if (eventListeners.get(id) == null) {
            return;
        }
        for(AppEventListener listener : eventListeners.get(id)){
            sendBroadcast(listener , type, data);
        }
    }




}