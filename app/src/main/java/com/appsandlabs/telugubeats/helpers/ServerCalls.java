package com.appsandlabs.telugubeats.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.UserDeviceManager;
import com.appsandlabs.telugubeats.config.Config;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.models.Event;
import com.appsandlabs.telugubeats.models.InitData;
import com.appsandlabs.telugubeats.models.PollItem;
import com.appsandlabs.telugubeats.models.User;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


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
        return items.get(   i==0?0:i-1);
    }
}

public class ServerCalls {
    public static final String CDN_PATH = "https://storage.googleapis.com/quizapp-tollywood/";
    public static final String SERVER_ADDR = "http://192.168.0.100:8888";
    public static String streamId = "telugu";


    static AsyncHttpClient client = new AsyncHttpClient();
    static {
        client.setMaxRetriesAndTimeout(1, 1000);
        client.setTimeout(4000);
        if(UserDeviceManager.getAuthKey()!=null)
            client.addHeader("auth_key", UserDeviceManager.getAuthKey());
    }
    static Gson gson = new Gson();

    public static void setUserGCMKey(final Context context, String registrationId, final GenericListener<Boolean> dataInputListener) {
//		String url = SERVER_ADDR+"/func?task=setGCMRegistrationId";
//		url+="&encodedKey="+UserDeviceManager.getAuthKey(context)+"&regId="+registrationId;
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
        client.get(SERVER_ADDR + "/stream/"+streamId+"/init_data", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String temp = new String(responseBody);
                InitData initData = gson.fromJson(temp, InitData.class);
                initData.setCurrentPoll();
                listener.onData(initData);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(Config.ERR_LOG_TAG , error.toString());
            }
        });
    }

    public static void sendPoll(PollItem pollItem , final GenericListener<Boolean> listener) {
        String authKey = UserDeviceManager.getAuthKey();
        if(authKey==null){
            //TODO: login dialog
            return;
        }
        client.addHeader("user_auth", authKey);

        client.get(SERVER_ADDR + "/poll/"+streamId+"/"+pollItem.poll+"/"+pollItem.id, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                listener.onData(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(Config.ERR_LOG_TAG, error.toString());
                listener.onData(false);
            }
        });
    }

    public static void registerUser(User user , final GenericListener<User> listener) {
        RequestParams params = new RequestParams();
        params.put("user_data", TeluguBeatsApp.gson.toJson(user));
        client.post(SERVER_ADDR + "/fromUser/login", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                User user = gson.fromJson(new String(responseBody), User.class);
                client.addHeader("auth_key", user.auth_key);
                listener.onData(user);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }
    static int count = 10;
    public static void readEvents() {
        new AsyncTask<Void, String, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                URL url = null;
                try {
                    url = new URL(ServerCalls.SERVER_ADDR + "/stream/" + ServerCalls.streamId + "/events");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    Scanner inputStream = new Scanner(new InputStreamReader((con.getInputStream())));
                    inputStream.useDelimiter("\r\n");
                    //noinspection InfiniteLoopStatement
                    while(true){
                        StringBuilder str = new StringBuilder();
                        String bytes;
                        while(inputStream.hasNext()){
                            bytes = inputStream.next();
                            if(bytes==null) return null; //reinitialize
                            if(bytes.equalsIgnoreCase("")){
                                break; // stop word reached
                            }
                            str.append(bytes);
                            str.append("\n");
                        }
                        publishProgress(str.toString());
                    }
                } catch (IOException e) {
                    if(count-- > 0)
                        readEvents();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {

                Event event = TeluguBeatsApp.gson.fromJson(values[0] , Event.class);
                TeluguBeatsApp.onEvent(event);


            }
        }.execute();
    }

    public static void sendDedicateEvent(String userName, final GenericListener<Boolean> listener) {
        String authKey = UserDeviceManager.getAuthKey();
        if(authKey==null){
            return;
        }
        RequestParams params = new RequestParams();
        params.put("user_name", userName);

        client.post(SERVER_ADDR + "/dedicate/", params ,  new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                listener.onData(true);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.d(Config.ERR_LOG_TAG, error.toString());
                listener.onData(false);
            }
        });

    }
}

