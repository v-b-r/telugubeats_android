package com.appsandlabs.com.appsandlabs.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import com.appsandlabs.datalisteners.GenericListener;
import com.appsandlabs.datalisteners.GenericListener2;
import com.appsandlabs.models.InitData;
import com.appsandlabs.models.PollItem;
import com.appsandlabs.models.Song;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.google.gson.Gson;

import org.apache.http.Header;


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
    public static final String SERVER_ADDR = "http://192.168.0.103:8888";
    static AsyncHttpClient client = new AsyncHttpClient();
    static Gson gson = new Gson();

    public static void setUserGCMKey(final Context context, String registrationId, final GenericListener<Boolean> dataInputListener) {
//		String url = SERVER_ADDR+"/func?task=setGCMRegistrationId";
//		url+="&encodedKey="+UserDeviceManager.getEncodedKey(context)+"&regId="+registrationId;
//

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

    public static void loadInitData(final GenericListener<InitData> listener) {
        client.get(SERVER_ADDR + "/init_data/get", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                InitData initData = gson.fromJson(responseBody.toString(), InitData.class);
                listener.onData(initData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
}

