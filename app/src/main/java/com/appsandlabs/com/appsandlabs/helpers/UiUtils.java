package com.appsandlabs.com.appsandlabs.helpers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.appsandlabs.NotificationReciever;
import com.appsandlabs.TeluguBeatsApp;
import com.appsandlabs.app.Config;
import com.appsandlabs.datalisteners.GenericListener;

import com.appsandlabs.telugubeats.MainActivity;
import com.appsandlabs.telugubeats.R;
import com.appsandlabs.widgets.CustomLoadingDialog;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;


public class UiUtils {


	public enum Images {
		MAP_MARKER_ICON("icons/marker.png"),
		PLUS_ICON("icons/plus_icon.png"),
		SMILEY_ICON("icons/smiley_icon.png");


		String value = null;
		int resourceId = -1;

		Images(String value) {
			this.value = value;
		}

		Images(int resourceId) {
			this.resourceId = resourceId;
		}

		public String getValue() {
			return value;
		}

		public int getResourceId() {
			return resourceId;
		}

		public Drawable getDrawable(Context ctx) {
			return ctx.getResources().getDrawable(getResourceId());
		}

		public String getValue(Object... args) {
			return String.format(value, args);
		}
	}

	private TeluguBeatsApp app;
	private Animation animationSlideInLeft;
	private Animation animationSlideOutRight;
	private Animation animationSlideInRight;
	private Animation animationSlideOutLeft;
	
	public UiUtils(TeluguBeatsApp app){
		this.app = app;
		
//       animationSlideInLeft = AnimationUtils.loadAnimation(app.getContext(),
//			   R.anim.slide_in_left);
//       animationSlideInRight = AnimationUtils.loadAnimation(app.getContext(),
//			   R.anim.slide_in_right);
//  	   animationSlideOutLeft = AnimationUtils.loadAnimation(app.getContext(),
//			   R.anim.slide_out_left);
//       animationSlideOutRight = AnimationUtils.loadAnimation(app.getContext(),
//			   R.anim.slide_out_right);
//       animationSlideOutLeft.setAnimationListener(app);
//       animationSlideOutRight.setAnimationListener(app);
//       animationSlideInLeft.setAnimationListener(app);
//       animationSlideInRight.setAnimationListener(app);
	}
	
	public boolean isOutAnimation(Animation animation){
		if(animation==animationSlideOutLeft || animation==animationSlideOutRight){
			return true;
		}
		return false;
	}

	public static enum UiText{
		NO_PREVIOUS_MESSAGES("No Previous Messages"), 
		TEXT_LOADING("loading.."), 
		INVITE_DIALOG_TITLE("Invite your Friends");

		String value = null;
		UiText(String value){
			this.value = value;
		}
		public String getValue(){
			return value;
		}
		public String getValue(Object...args){
			return String.format(value, args);
		}


	}

	
	private static int uiBlockCount  =0;
	private static CustomLoadingDialog preloader = null;
	private static CharSequence preloaderText;
	public  synchronized void addUiBlock(){
		try{
			if(uiBlockCount==0){
				preloaderText = UiText.TEXT_LOADING.getValue();
				preloader = new CustomLoadingDialog(app.getContext(), preloaderText);
				preloader.show();
			}
			uiBlockCount++;
		}
		catch(Exception e){
			uiBlockCount =0 ;
			//older view error
		}
			
	}
	public synchronized void addUiBlock(String text){
		try{
		if(uiBlockCount==0){
			preloaderText = text;
			preloader = new CustomLoadingDialog(app.getContext(), preloaderText);
			preloader.show();
		}
		else{
			if(!preloaderText.toString().endsWith(text)){
				preloaderText = preloaderText+ ("\n"+text);
				preloader.setMessage(preloaderText);
			}
		}
		uiBlockCount++;
	}
	catch(Exception e){
		uiBlockCount =0 ;
		//older view error
	}

	}
	
	public synchronized boolean removeUiBlock(){
		try{
			uiBlockCount--;
			if(uiBlockCount==0){
				
				preloader.dismiss();
				return true;
			}
			return false;
		}
		catch(Exception e){
			uiBlockCount =0 ;
			//older view error
			return false;
		}

	}
	@SuppressLint("NewApi")
	public static void setBg(View view , Drawable drawable){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
	    	view.setBackground(drawable);
	    } else {
	    	view.setBackgroundDrawable(drawable);
	    }
	}
	
	
	public Timer setInterval(int millis , final GenericListener<Integer> listener) {
		// TODO Auto-generated constructor stub
		Timer timer = (new Timer());
		timer.schedule(new TimerTask() {
					int count =0;
					@Override
					public void run() {
						// TODO: NullPointerException after when pressing back button to exit quiz
							FragmentActivity activity = app.getCurrentActivity();
							if(activity!=null)
						      (activity).runOnUiThread(new Runnable(){
		
						       @Override
						       public void run() {
						    	   listener.onData(++count);
						       }}
						       );
							else{
								Log.d("ERR", "changes");
								this.cancel();
							}
					}
		}, 0, millis);
		return timer;
	}
	public static void generateNotification(Context pContext, String titleText, String message,Bundle b) {
		int notificationId = Config.NOTIFICATION_ID;
    	int type = b!=null ? b.getInt(Config.NOTIFICATION_KEY_MESSAGE_TYPE, -1):-1;
    	if(titleText==null){
    		titleText = pContext.getResources().getString(R.string.app_name);
    	}
    	switch(NotificationReciever.getNotificationTypeFromInt(type)){
				case DONT_KNOW:
					break;
    	}
    	
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(pContext)
        		.setSmallIcon(R.drawable.ic_launcher).setContentTitle(titleText)
                        .setContentText(message);
        notificationBuilder.setWhen(System.currentTimeMillis()).setAutoCancel(true);
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND);
        Intent resultIntent = new Intent(pContext, MainActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(b!=null)
        	resultIntent.putExtras(b);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(pContext);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(CalendarView.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(pContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) pContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, notificationBuilder.build()); //will show a notification and when clicked will open the app.	    
	}
	public static void generateNotification(Context pContext, String message) {
		generateNotification(pContext, null, message, null);
	}
    
    public static void sendSMS(Context context , String phoneNumber , String text) {
    	Uri smsUri = Uri.parse("tel:+" + phoneNumber);
    	Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
    	intent.putExtra("sms_body", text);
    	intent.setType("vnd.android-dir/mms-sms");
    	context.startActivity(intent);
    }  
    
    
    public static void shareText(Activity A,String message,String phoneNumber){
    	Intent sendIntent = new Intent();
    	sendIntent.setAction(Intent.ACTION_SEND);
    	sendIntent.putExtra(Intent.EXTRA_TEXT, message);
    	if(phoneNumber!=null){
    		sendIntent.putExtra(Intent.EXTRA_PHONE_NUMBER, phoneNumber);
    		sendIntent.putExtra("address", phoneNumber);
    	}
    	sendIntent.setType("text/plain");
    	A.startActivity(Intent.createChooser(sendIntent, UiUtils.UiText.INVITE_DIALOG_TITLE.getValue()));
    }

	public static String formatRemainingTime(double timeRemainingInMillis){
		long secondsInMilli = 1000;
		long minutesInMilli = secondsInMilli * 60;
		long hoursInMilli = minutesInMilli * 60;
		long daysInMilli = hoursInMilli * 24;

		String ret = "";
		long elapsedDays = (long) (timeRemainingInMillis / daysInMilli);
		timeRemainingInMillis = timeRemainingInMillis % daysInMilli;
		if(elapsedDays>0) ret+=elapsedDays+"days ";

		long elapsedHours = (long) (timeRemainingInMillis / hoursInMilli);
		timeRemainingInMillis = timeRemainingInMillis % hoursInMilli;
		if(elapsedDays>0 ||elapsedHours>0) ret+=elapsedHours+"hours ";

		long elapsedMinutes = (long) (timeRemainingInMillis / minutesInMilli);
		timeRemainingInMillis = timeRemainingInMillis % minutesInMilli;
		if(elapsedDays>0 ||elapsedHours>0 || elapsedMinutes>0) ret+=elapsedMinutes+"min ";

		long elapsedSeconds = (long) (timeRemainingInMillis / secondsInMilli);
		if(elapsedDays>0 ||elapsedHours>0 || elapsedMinutes>0 ||elapsedSeconds>0) ret+=elapsedSeconds+"sec";


		return 	ret;
	}


	public Animation getAnimationSlideOutRight() {
		return animationSlideOutRight;
	}

	public Animation getAnimationSlideOutLeft() {
		return animationSlideOutLeft;
	}

	public Animation getAnimationSlideInLeft() {
		return animationSlideInLeft;
	}
	public Animation getAnimationSlideInRight() {
		return animationSlideInRight;
	}


	public static Task<RequestCreator> getRequestCreatorTask(final String assetPath, final boolean downloadToAssets){
		return Task.call(new Callable<RequestCreator>() {
			@Override
			public RequestCreator call() throws Exception {

				if(assetPath.startsWith("http://") || assetPath.startsWith("https://")){
					return Picasso.with(TeluguBeatsApp.getContext()).load(assetPath);//.error(R.drawable.error_image);
				}
//				try {
//					InputStream ims = app.getContext().getAssets().open("images/" + assetPath); //assets folder
//					ims.close();
//					return Picasso.with(app.getContext()).load("file:///android_asset/images/" + assetPath).error(R.drawable.error_image);
//				} catch (IOException e) {
//					Log.d(Config.QUIZAPP_ERR_LOG_TAG, "failed to load from assets");
//					e.printStackTrace();
//				}
//				File file = new File(app.getContext().getFilesDir().getParentFile().getPath()+"/images/"+assetPath);
//				if(file.exists()){
//					return Picasso.with(app.getContext()).load(file).error(R.drawable.error_image);
//				}

//				Log.d(Config.QUIZAPP_ERR_LOG_TAG, "loading from CDN");

				RequestCreator requestCreator =  Picasso.with(TeluguBeatsApp.getContext()).load(ServerCalls.CDN_PATH + assetPath);//.error(R.drawable.error_image);

//				if(downloadToAssets) {
//					try {
//						Bitmap bitmap = requestCreator.get();
//						File saveImageFile = new File(app.getContext().getFilesDir().getParentFile().getPath() + "/images/" + assetPath);
//						saveImageFile.createNewFile();
//						FileOutputStream ostream = new FileOutputStream(saveImageFile);
//						bitmap.compress(assetPath.endsWith(".png") ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 75, ostream);
//						ostream.close();
//					}
//					catch (Exception e){
//					}
//				}
				return requestCreator;
			}
		});
	}


	public int  generateRandomColor(int mix) {
		Random random = new Random();
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		// mix the color
		red = (red + Color.red(mix)) / 2;
		green = (green + Color.green(mix)) / 2;
		blue = (blue + Color.blue(mix)) / 2;

		int color =  Color.argb(255,red, green, blue);
		return color;
	}


	public static void into(RequestCreator requestCreator, ImageView imageView, Callback callback) {
		  boolean mainThread = Looper.myLooper() == Looper.getMainLooper();
		  if (mainThread) {
		    requestCreator.into(imageView, callback);
		  } else {
		    try {
		      Bitmap bitmap = requestCreator.get();
		      imageView.setImageBitmap(bitmap);
		      if (callback != null) {
		        callback.onSuccess();
		      }
		    } catch (IOException e) {
		      if (callback != null) {
		        callback.onError();
		      }
		    }
		  }
	}


	public static boolean loadImageIntoViewDoInBackground(Context ctx, final ImageView imgView, final String assetPath, final boolean downloadToAssets){
			return loadImageIntoViewDoInBackground(ctx, imgView, assetPath, downloadToAssets, -1, -1, null);
	}

	public static boolean loadImageIntoViewDoInBackground(Context ctx, final ImageView imgView, final String assetPath, final boolean downloadToAssets, int width, int height, GenericListener<Boolean> completedLoadingImage){
		if(assetPath==null || assetPath.isEmpty())
			return false;
//		try{
			if(assetPath.startsWith("http://") || assetPath.startsWith("https://")){
				if(width> 0 && height>0)
					into(Picasso.with(ctx).load(assetPath).resize(width , height), imgView, null);
				else
					into(Picasso.with(ctx).load(assetPath), imgView, null);
			    return true;
			}

//		    InputStream ims = ctx.getAssets().open("images/"+assetPath); //assets folder
//			if(width>0 && height>0)
//				into(Picasso.with(ctx).load("file:///android_asset/images/"+assetPath).resize(width, height),imgView, null);
//			else
//				into(Picasso.with(ctx).load("file:///android_asset/images/" + assetPath), imgView, null);
//			return true;
//		}
//		catch(IOException ex) {//files in SD card
//			File file = new File(ctx.getFilesDir().getParentFile().getPath()+"/images/"+assetPath);
//			if(file.exists()){
//				into(Picasso.with(ctx).load(file).fit().centerCrop() , imgView, null);
//			}
//			else{
//				if(downloadToAssets){//from cdn //TODO: convert this for synchronous use
//					imgView.setTag(new LoadAndSave(imgView, file, assetPath, downloadToAssets, completedLoadingImage));
//					if(width>0 && height>0)
//						Picasso.with(ctx).load(ServerCalls.CDN_PATH+assetPath).error(R.drawable.error_image).resize(width , height).into((LoadAndSave)imgView.getTag());
//					else{
//						Picasso.with(ctx).load(ServerCalls.CDN_PATH+assetPath).error(R.drawable.error_image).into((LoadAndSave)imgView.getTag());
//					}
//				}
//				else{
			into(Picasso.with(ctx).load(ServerCalls.CDN_PATH + assetPath), imgView,null);//directly
//				}
//			}
//		}
//		catch (Exception e) {
//			return false;
//		}
		return true;
	}
	public Task<RequestCreator> loadImageIntoView(Context ctx, final ImageView imgView, final String assetPath, final boolean downloadToAssets){
		return loadImageIntoView(ctx , imgView,  assetPath , downloadToAssets , null);
	}


	public Task<RequestCreator> loadImageIntoView(Context ctx, final ImageView imgView, final String assetPath, final boolean downloadToAssets, Transformation t){
		return loadImageIntoView(ctx , imgView,  assetPath , downloadToAssets , -1 , -1 , t);
	}

	public static Task<RequestCreator> loadImageIntoView(Context ctx, final ImageView imgView, final String assetPath, final boolean downloadToAssets, final int width, final int height, final Transformation transformation){
		return getRequestCreatorTask(assetPath , downloadToAssets).onSuccess(new Continuation<RequestCreator, RequestCreator>() {

			@Override
			public RequestCreator then(Task<RequestCreator> task) throws Exception {
				RequestCreator requestCreator = task.getResult();
				if(transformation!=null)
					requestCreator.transform(transformation);

				if(width> 0 && height >0)
					requestCreator.resize(width, height);

				requestCreator.into(imgView);
				return requestCreator;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}


	public static  void loadImageAsBg(final View view , final String assetPath , boolean downloadToAssets){
		if(assetPath==null || assetPath.isEmpty())
			return;
		Task.callInBackground(new Callable<Bitmap>() {
			@Override
			public Bitmap call() throws Exception {
				RequestCreator requestCreator = null;
				if (assetPath.startsWith("http://") || assetPath.startsWith("https://")) {
					requestCreator = Picasso.with(TeluguBeatsApp.getContext()).load(assetPath);//.error(R.drawable.error_image);
				}
//				try {
//					InputStream ims = app.getContext().getAssets().open("images/" + assetPath); //assets folder
//					ims.close();
//					requestCreator = Picasso.with(app.getContext()).load("file:///android_asset/images/" + assetPath).error(R.drawable.error_image);
//				} catch (IOException e) {
//					Log.d(Config.QUIZAPP_ERR_LOG_TAG, "failed to load from assets");
//					e.printStackTrace();
//				}
//				File file = new File(app.getContext().getFilesDir().getParentFile().getPath()+"/images/"+assetPath);
//				if(file.exists()){
//					return Picasso.with(app.getContext()).load(file).error(R.drawable.error_image);
//				}
				if(requestCreator==null)
					requestCreator = Picasso.with(TeluguBeatsApp.getContext()).load(ServerCalls.CDN_PATH + assetPath);//.error(R.drawable.error_image);
				return requestCreator.get();
			}
		}).onSuccess(new Continuation<Bitmap, Void>() {
			@Override
			public Void then(Task<Bitmap> task) throws Exception {
				if(task.getResult()!=null)
					UiUtils.setBg(view , new BitmapDrawable(view.getResources(), task.getResult()));;
				return null;
			}
		}, Task.UI_THREAD_EXECUTOR);
	}

//	public double getLevelFromPoints(double points){
//		return points;
////		2+n/3
////		increment: 3 3 4 4 4 5 5 5 6 6 6 7 7 7
////		sigma(3+k/3)
////		3 + k
////		3 6 10 14 18 23 28 33 39 45 51 58 65 72
////		3*n+(n/3)0 0 1 2 3 5 7 9 12 15 18 22 26)   (3*n+(n/3)+ N-1 shit)
////		2+(0 0 1 1 1 3 3 3 6 6 6 ) = 3+9*(1+2+3 ..) 3+9*(n*(n-1))/2 ;; 400+3*(1 2 3) (level-2)*(level-3)/2+(level-2)
////		200 400 700 1000 1300 1800 2300 2800 3600 4400 5200 ..
////		2 4 7 10 13 18 23 18 36 44 52
//	}

//	public double getPointsFromLevel(double level){
//		return 100*(2*level + (level*level - level)/6);
//	}

	float oneDp = -1;
	public float getInDp(int i) {
		if(oneDp==-1){
			oneDp = app.getResources().getDimension(R.dimen.one_dp);
		}
		return i*oneDp;
	}

	float oneSp = -1;
	public float getInSp(int i) {
		if(oneSp==-1){
			oneSp = app.getResources().getDimension(R.dimen.one_sp);
		}
		return i*oneSp;
	}
	public int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				app.getResources().getDisplayMetrics());
	}

	public static ListView setListViewHeightBasedOnChildren2(ListView myListView) {
	      ListAdapter myListAdapter = myListView.getAdapter();
	        if (myListAdapter == null || myListAdapter.getCount()==0) {
	            //do nothing return null
	            return myListView;
	        }
	        //set listAdapter in loop for getting final size
	        int totalHeight = 0;
	        for (int size = 0; size < myListAdapter.getCount(); size++) {
	            View listItem = myListAdapter.getView(size, null, myListView);
	            listItem.measure(0, 0);
	            totalHeight += listItem.getMeasuredHeight();
	        }
	      //setting listview item in chatListAdapter
	        ViewGroup.LayoutParams params = myListView.getLayoutParams();
	        params.height = totalHeight + (myListView.getDividerHeight() * (myListAdapter.getCount() - 1));
	        myListView.setLayoutParams(params);
	        return myListView;
	}

	public void blickAnimation(View view){
		 final Animation animation = new AlphaAnimation(1, 0); // Change alpha from fully visible to invisible
		    animation.setDuration(500); // duration - half a second
		    animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
		    animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
		    animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in
		    view.startAnimation(animation);
	}

	protected void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span , final GenericListener<String> clickListener){
	    int start = strBuilder.getSpanStart(span);
	    int end = strBuilder.getSpanEnd(span);
	    int flags = strBuilder.getSpanFlags(span);
	    ClickableSpan clickable = new ClickableSpan() {
	          public void onClick(View view) {
	        	  if(clickListener!=null)
	        		  clickListener.onData(span.getURL());
	        	  else{
	        		  genericLinkClickListener(span.getURL());
	        	  }
	          }
			@Override
			public void updateDrawState(TextPaint ds) {
				ds.setColor(Color.BLACK);

			}
	    };
	    strBuilder.setSpan(clickable, start, end, flags);
	    strBuilder.removeSpan(span);
	}

	protected void genericLinkClickListener(String url) {
		// TODO Auto-generated method stub

	}

	public void setTextViewHTML(TextView text, String html , GenericListener<String> clickListener){
	    CharSequence sequence = Html.fromHtml(html);
	    	text.setMovementMethod(LinkMovementMethod.getInstance());
	        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
	        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
	        for(URLSpan span : urls) {
	            makeLinkClickable(strBuilder, span, clickListener);
	        }
	    text.setText(strBuilder);
	}


	public static Point getScreenDimetions(TeluguBeatsApp app){
		WindowManager w = app.getCurrentActivity().getWindowManager();
		Point point = new Point();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
		    Point size = new Point();
		    w.getDefaultDisplay().getSize(size);
		} else {
		    Display d = w.getDefaultDisplay();
		    point.x = d.getWidth();
		    point.y = d.getHeight();
		}
	    return point;
	}

	public void populateViews(LinearLayout linearLayout, View[] views, Context context, View extraView){
	    extraView.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

	    // kv : May need to replace 'getSherlockActivity()' with 'this' or 'getActivity()'
	    Display display = app.getCurrentActivity().getWindowManager().getDefaultDisplay();
	    linearLayout.removeAllViews();
	    int maxWidth = display.getWidth() - extraView.getMeasuredWidth() - 20;

	    linearLayout.setOrientation(LinearLayout.VERTICAL);

	    LinearLayout.LayoutParams params;
	    LinearLayout newLL = new LinearLayout(context);
	    newLL.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	    newLL.setGravity(Gravity.LEFT);
	    newLL.setOrientation(LinearLayout.HORIZONTAL);

	    int widthSoFar = 0;

	    for (int i = 0; i < views.length; i++)
	    {
	        LinearLayout LL = new LinearLayout(context);
	        LL.setOrientation(LinearLayout.HORIZONTAL);
	        LL.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
	        LL.setLayoutParams(new ListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	        views[i].measure(0, 0);
	        params = new LinearLayout.LayoutParams(views[i].getMeasuredWidth(), LayoutParams.WRAP_CONTENT);
	        params.setMargins(5, 0, 5, 0);

	        LL.addView(views[i], params);
	        LL.measure(0, 0);
	        widthSoFar += views[i].getMeasuredWidth();
	        if (widthSoFar >= maxWidth)
	        {
	            linearLayout.addView(newLL);

	            newLL = new LinearLayout(context);
	            newLL.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	            newLL.setOrientation(LinearLayout.HORIZONTAL);
	            newLL.setGravity(Gravity.LEFT);
	            params = new LinearLayout.LayoutParams(LL.getMeasuredWidth(), LL.getMeasuredHeight());
	            newLL.addView(LL, params);
	            widthSoFar = LL.getMeasuredWidth();
	        }
	        else
	        {
	            newLL.addView(LL);
	        }
	    }
	    linearLayout.addView(newLL);
	}
	
}
