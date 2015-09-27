package com.appsandlabs.com.appsandlabs.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.http.Header;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.appsandlabs.datalisteners.GenericListener;


class Item<T> {
    int reletiveProb;
    T name;
    
    Item(int prob, T n){
    	reletiveProb = prob;
    	name = n;
    }
}
class RandomSelector <T>{
    List<Item<T>> items = new ArrayList<Item<T>>();
    Random rand = new Random();
    int totalSum = 0;

    RandomSelector(List<Item<T>> items) {
    	this.items = items;
        for(Item<T> item : items) {
            totalSum = totalSum + item.reletiveProb;
        }
    }

    public Item<T> getRandom() {

        int index = rand.nextInt(totalSum);
        int sum = 0;
        int i=0;
        while(sum < index ) {
             sum = sum + items.get(i++).reletiveProb;
        }
        return items.get(i==0?0:i-1);
    }
}

public class ServerCalls {
    public static final String CDN_PATH = "https://storage.googleapis.com/quizapp-tollywood/";

    public static void setUserGCMKey(final Context context, String registrationId, final GenericListener<Boolean> dataInputListener) {
//		String url = SERVER_ADDR+"/func?task=setGCMRegistrationId";
//		url+="&encodedKey="+UserDeviceManager.getEncodedKey(context)+"&regId="+registrationId;
//
//		AsyncHttpClient client  = new AsyncHttpClient();
//		client.setMaxRetriesAndTimeout(3, 10);
//
//		final ServerNotifier serverNotifier = new ServerNotifier() {
//			@Override
//			public void onServerResponse(MessageType messageType, ServerResponse response) {
//				switch(messageType){
//					case REG_SAVED:
//						if(dataInputListener!=null){
//							dataInputListener.onData(true);
//						}
//						break;
//					case FAILED:
//						if(dataInputListener!=null){
//							dataInputListener.onData(false);
//						}
//						break;
//					default:
//						break;
//				}
//			}
//		};
//		client.get(url, new AsyncHttpResponseHandler() {
//			@Override
//			public void onSuccess(int arg0, Header[] arg1, byte[] responseBytes) {
//				String response = new String(responseBytes);
//				ServerResponse serverResponse= (new Gson()).fromJson(response, ServerResponse.class);
//				MessageType messageType = serverResponse.getStatusCode();
//				serverNotifier.onServerResponse(messageType , serverResponse);
//			}
//			public void  onFailure(int messageType, org.apache.http.Header[] headers, byte[] responseBody, Throwable error){
//
//				serverNotifier.onServerResponse(MessageType.FAILED , null);
//			}
//		});

	}
}

