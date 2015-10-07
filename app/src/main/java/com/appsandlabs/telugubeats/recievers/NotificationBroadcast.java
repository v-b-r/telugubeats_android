package com.appsandlabs.telugubeats.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.config.Config;
import com.appsandlabs.telugubeats.services.MusicService;

public class NotificationBroadcast extends BroadcastReceiver {

	public String ComponentName() {
		return this.getClass().getName();
	}


	@Override
	public void onReceive(Context context, Intent intent) {

		Log.e(Config.ERR_LOG_TAG, "recieved intent "+intent);
		if (intent.getAction().equals(MusicService.NOTIFY_PLAY)) {
			TeluguBeatsApp.onSongPlayPaused.sendMessage(TeluguBeatsApp.onSongPlayPaused.obtainMessage(0,0));
		} else if (intent.getAction().equals(MusicService.NOTIFY_PAUSE)) {
			TeluguBeatsApp.onSongPlayPaused.sendMessage(TeluguBeatsApp.onSongPlayPaused.obtainMessage(0, 1));
		}
		else if (intent.getAction().equals(MusicService.NOTIFY_DELETE)) {
			TeluguBeatsApp.onSongPlayPaused.sendMessage(TeluguBeatsApp.onSongPlayPaused.obtainMessage(0, 2));
		}
	}
}
