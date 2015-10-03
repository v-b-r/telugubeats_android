package com.appsandlabs.telugubeats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.appsandlabs.telugubeats.config.Config;
import com.appsandlabs.telugubeats.helpers.UiUtils;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.enums.NotifificationProcessingState;
import com.google.gson.Gson;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/*
This is a little dirty , two instances of this class operate , one with iAppAlive= true , when the app comes to foreground
and other verion if with false for default. will refactor it later.
 */
public class NotificationReciever extends BroadcastReceiver {


	private static boolean isAppAlive= false;

	public static void setOffline() {
		isAppAlive = false;
		destroyAllListeners();
	}

	public static void setOnline(){
		destroyAllListeners();
		isAppAlive = true;
	}

	public static class NotificationPayload{
				public int messageType = -1;
				public String fromUser;
				public String fromUserName;
				public String quizPoolWaitId;
				public String serverId;
				public String quizId;
				public String quizName;
				public String textMessage;
				public String payload1;
				public String payload2;
				public String payload3;
				public String payload4;

				public static NotificationPayload getNotificationPayloadFromBundle(Bundle bundle){
					JSONObject json = new JSONObject();
					Set<String> keys = bundle.keySet();
					for (String key : keys) {
						try {
							// json.put(key, bundle.get(key)); see edit below
							json.put(key, bundle.get(key));
						} catch(JSONException e) {
							//Handle exception here
						}
					}
					return new Gson().fromJson(json.toString(),NotificationPayload.class );
				}
			}
	
			public static enum NotificationType{

				DONT_KNOW(-1);

				
				public int value;

				private NotificationType(int val) {
					this.value = val;
				}
			}

			public static NotificationType getNotificationTypeFromInt(int x){
				NotificationType[] values = NotificationType.values();
				if(x >0 && x < values.length)
					for(NotificationType type:values){
						if(type.value == x)
							return type;
					}
				return NotificationType.DONT_KNOW;
			}
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle extras = intent.getExtras();
				int temp = 0;
				try{
					String temp2 = extras.getString(Config.NOTIFICATION_KEY_MESSAGE_TYPE);
					temp = temp2==null ? -1 : Integer.parseInt(temp2);
				}
				catch(Exception e){
					temp =0;
				}
				NotificationType type = getNotificationTypeFromInt(Math.max(temp, extras.getInt(Config.NOTIFICATION_KEY_MESSAGE_TYPE, -1)));
				
				String titleText = null;
				String messageToDisplay = UiText.NEW_TEXT_AVAILABLE.getValue();
				boolean generateNotification = true;
				boolean payloadConsumed = false;
				NotificationPayload payload = null;
				if(extras!=null){
					payload = NotificationPayload.getNotificationPayloadFromBundle(extras);
					switch(type){

						case DONT_KNOW:
							messageToDisplay = payload.toString();
							break;

						default:
							break;
					}
				}
				if(generateNotification) // inside app all notifications should be handled without notifications
					UiUtils.generateNotification(context, titleText, messageToDisplay, extras);
				if(!payloadConsumed){//queue it
					TeluguBeatsApp.pendingNotifications.put(type, new ArrayList<NotificationPayload>());
					TeluguBeatsApp.pendingNotifications.get(type).add(payload);
				}
		 	}
			
			static HashMap<NotificationType, GenericListener<NotificationPayload>> listeners = new HashMap<NotificationType, GenericListener<NotificationPayload>>();
			
			public void setListener(NotificationType type , GenericListener<NotificationPayload> listener){
				listeners.put(type, listener);
			}
			
			public static void destroyAllListeners(){
				listeners.clear();
			}


			public void removeListener(
					NotificationType notificationGcmInboxMessage) {
				listeners.remove(notificationGcmInboxMessage);
			}
			
			public boolean checkAndCallListener(NotificationType type, NotificationPayload notificationPayload){
				if(TeluguBeatsApp.nState== NotifificationProcessingState.CONTINUE){
					if(listeners.containsKey(type)){
							listeners.get(type).onData(notificationPayload);
							return true;
					}
				}
				return false; // we wont call listener until state is continue , setting to continue is very important 
			}
			
			public GenericListener<NotificationPayload> getListener( NotificationType type) {
				if(listeners.containsKey(type)){
						return listeners.get(type);
				}
				return null;
			}
	}