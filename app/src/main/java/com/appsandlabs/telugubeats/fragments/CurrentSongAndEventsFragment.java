package com.appsandlabs.telugubeats.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.appsandlabs.telugubeats.R;
import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.UiText;
import com.appsandlabs.telugubeats.config.VisualizerConfig;
import com.appsandlabs.telugubeats.datalisteners.GenericListener;
import com.appsandlabs.telugubeats.helpers.UiUtils;
import com.appsandlabs.telugubeats.interfaces.AppEventListener;

import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

import static com.appsandlabs.telugubeats.TeluguBeatsApp.getContext;
import static com.appsandlabs.telugubeats.TeluguBeatsApp.getServerCalls;
import static com.appsandlabs.telugubeats.helpers.UiUtils.getColorFromResource;

/**
 * Created by abhinav on 10/2/15.
 */
public class CurrentSongAndEventsFragment extends Fragment {

    private Paint hLinesPaint;
    private Paint barPaint;

    private float[] fftDataLeft = new float[1024/2];
    private float[] fftDataRight = new float[1024/2];

    private float[] barHeightsLeft = new float[VisualizerConfig.nBars];
    private float[] barHeightsRight = new float[VisualizerConfig.nBars];
    private View visualizerView;
    public ServiceConnection serviceConnection;
    private Bitmap mBitmap;
    private ViewGroup layout;
    private AppEventListener blurredBgListener;
    private AppEventListener feedChangeListener;
    private AppEventListener songChangeListener;

    public static class UiHandle{

        TextView songAndTitle;
        ListView scrollingDedications;
        TextView musicDirectors;
        TextView actors;
        TextView directors;
        TextView singers;
        TextView liveUsers;
        LinearLayout whatsAppDedicate;
        LinearLayout visualizer;
        EditText saySomethingText;
        Button sayButton;
        ScrollView scrollView;

    }

    UiHandle uiHandle = new UiHandle();

    public UiHandle initUiHandle(ViewGroup layout){

        uiHandle.songAndTitle = (TextView)layout.findViewById(R.id.song_and_title);
        uiHandle.scrollingDedications = (ListView)layout.findViewById(R.id.scrolling_dedications);
        uiHandle.musicDirectors = (TextView)layout.findViewById(R.id.music_directors);
        uiHandle.actors = (TextView)layout.findViewById(R.id.actors);
        uiHandle.directors = (TextView)layout.findViewById(R.id.directors);
        uiHandle.singers = (TextView)layout.findViewById(R.id.singers);
        uiHandle.liveUsers = (TextView)layout.findViewById(R.id.live_users);
        uiHandle.whatsAppDedicate = (LinearLayout)layout.findViewById(R.id.whats_app_dedicate);
        uiHandle.visualizer = (LinearLayout)layout.findViewById(R.id.visualizer);
        uiHandle.saySomethingText = (EditText)layout.findViewById(R.id.say_something_text);
        uiHandle.sayButton  = (Button)layout.findViewById(R.id.say_button);
        uiHandle.scrollView = (ScrollView)layout.findViewById(R.id.scrolling_view);
        return uiHandle;
    }


    Bitmap visualizerBitmap = null;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        Context ctx = getActivity();
        hLinesPaint = new Paint();
        hLinesPaint.setColor(getResources().getColor(android.R.color.transparent));
        hLinesPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
        hLinesPaint.setStrokeWidth(3);

        barPaint = new Paint();
        barPaint.setStrokeWidth(1);
        barPaint.setShader(new LinearGradient(0, 0, 0, VisualizerConfig.barHeight, getColorFromResource(R.color.malachite), Color.argb(255, 200, 200, 200), Shader.TileMode.MIRROR));
        barPaint.setStyle(Paint.Style.FILL);
        layout = (ViewGroup) inflater.inflate(R.layout.events_and_song_fragment_layout, null);

        uiHandle = initUiHandle(layout);



        uiHandle.visualizer.addView(visualizerView = new View(ctx) {


            public Canvas canvas;

            @Override
            protected void onDraw(Canvas mCanvas) {

                canvas.setBitmap(mBitmap);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                float[] leftFft = fftDataLeft;
                float[] rightFft = fftDataRight;
                int nBars = VisualizerConfig.nBars;
                for (int i = 0; i < nBars; i++) {
                    float max = 0;
                    int bandSize = leftFft.length / nBars;
                    for (int j = 0; j < bandSize; j++) {
                        //if (max < leftFft[bandSize * i + j]) {
                        max += leftFft[bandSize * i + j];
                        //}
                    }
                    barHeightsLeft[i] = max / bandSize;
                }

                for (int i = 0; i < nBars; i++) {
                    float max = 0;
                    int bandSize = rightFft.length / nBars;
                    for (int j = 0; j < bandSize; j++) {
                        // if (max < rightFft[bandSize * i + j]) {
                        max += rightFft[bandSize * i + j];
                        // }
                    }
                    barHeightsRight[i] = max / bandSize;
                }


//                for (int i = 0; i < VisualizerConfig.nBars; i++) {//horizontal lines
//                    int x = i * (VisualizerConfig.barWidth + VisualizerConfig.barSpacing);
//                    canvas.drawRect(
//                            x,
//                            VisualizerConfig.barHeight - (barHeightsLeft[i]) / 16,
//                            x + VisualizerConfig.barWidth,
//                            VisualizerConfig.barHeight,
//                            barPaint);
//                }
                int xOffset = 0;//  (VisualizerConfig.barWidth + VisualizerConfig.barSpacing)*VisualizerConfig.nBars
                for (int i = 0; i < VisualizerConfig.nBars / 2; i++) {//horizontal lines
                    int x = i * (VisualizerConfig.barWidth + VisualizerConfig.barSpacing) + xOffset;
                    int max = VisualizerConfig.nBars / 2;

                    //draw max/2-i , max/2+i

                    canvas.drawRect(
                            x,
                            VisualizerConfig.barHeight - (barHeightsRight[max - i]) / 8,//2*VisualizerConfig.nBars-1-i
                            x + VisualizerConfig.barWidth,
                            VisualizerConfig.barHeight,
                            barPaint);

                }

                for (int i = 0; i < VisualizerConfig.nBars / 2; i++) {//horizontal lines
                    int x = (i + VisualizerConfig.nBars / 2) * (VisualizerConfig.barWidth + VisualizerConfig.barSpacing) + xOffset;
                    int max = VisualizerConfig.nBars;

                    canvas.drawRect(
                            x,
                            VisualizerConfig.barHeight - (barHeightsRight[max - 1 - i]) / 8,//2*VisualizerConfig.nBars-1-i
                            x + VisualizerConfig.barWidth,
                            VisualizerConfig.barHeight,
                            barPaint);
                }

                for (
                        int i = 0;
                        i < VisualizerConfig.hLines; i++)

                {//horizontal lines
                    int y = i * VisualizerConfig.barHeight / VisualizerConfig.hLines;
                    canvas.drawLine(0, y, (VisualizerConfig.barWidth + VisualizerConfig.barSpacing) * VisualizerConfig.nBars * 2, y, hLinesPaint);
                }
                mCanvas.drawBitmap(mBitmap, 0, 0, null);
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                VisualizerConfig.barWidth = (right - left) / VisualizerConfig.nBars - VisualizerConfig.barSpacing;
                super.onLayout(changed, left, top, right, bottom);
            }

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(mBitmap);
            }

        }, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, VisualizerConfig.barHeight));


        visualizerView.post(new Runnable() {
            @Override
            public void run() {
                visualizerView.getHeight();
                visualizerView.getWidth();


            }
        });
        // load current polls and poll data
        // get current playing currentSong
        // get current



        uiHandle.whatsAppDedicate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // popup to write a name
                TeluguBeatsApp.getUiUtils().promptInput("Enter name of user", 0, "", "dedicate", new GenericListener<String>() {
                    @Override
                    public void onData(String a) {
                        if (a.trim().isEmpty()) return;


                        getServerCalls().sendDedicateEvent(a, new GenericListener<Boolean>() {
                            @Override
                            public void onData(Boolean s) {
                                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, TeluguBeatsApp.currentUser.name + " has dedicated " + TeluguBeatsApp.currentSong.title + " song to you on TeluguBeats");
                                String link = "https://play.google.com/store/apps/details?id=com.appsandlabs.telugubeats";
                                sharingIntent.putExtra(Intent.EXTRA_TEXT, link);
                                sharingIntent.setPackage("com.whatsapp");

                                if(sharingIntent.resolveActivity(getActivity().getPackageManager()) != null)
                                    startActivityForResult(sharingIntent, 0);
                                Toast.makeText(getContext() , UiText.UNABLE_TO_OPEN_INTENT.getValue() ,  Toast.LENGTH_SHORT );
                            }
                        });
                    }
                });
            }
        });


//        final ArrayList<String> eventsAdapter = new ArrayList<String>(TeluguBeatsApp.lastFewEvents);
//        Collections.reverse(eventsAdapter);
          uiHandle.scrollingDedications.setAdapter(new ArrayAdapter<String>(TeluguBeatsApp.getContext(),
                  R.layout.simple_list_item_1, R.id.text_view, TeluguBeatsApp.getLastFewFeedEvents()));



        uiHandle.saySomethingText.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    UiUtils.scrollToBottom(uiHandle.scrollingDedications);
                    UiUtils.scrollToBottom(uiHandle.scrollView);
                }
            }
        });

        uiHandle.sayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = uiHandle.saySomethingText.getText().toString();
                uiHandle.saySomethingText.setText("");
                getServerCalls().sendChat(text, new GenericListener<Boolean>());
                uiHandle.saySomethingText.requestFocus();
            }
        });


        UiUtils.scrollToBottom(uiHandle.scrollingDedications);
        return layout;
    }

    private void resetCurrentSong() {
        Context ctx = TeluguBeatsApp.getCurrentActivity();

        uiHandle.songAndTitle.setText(TeluguBeatsApp.currentSong.title + " - " + TeluguBeatsApp.currentSong.album.name);
        uiHandle.singers.setText(TextUtils.join(", ", TeluguBeatsApp.currentSong.singers));


        if (TeluguBeatsApp.currentSong.album.directors!=null && TeluguBeatsApp.currentSong.album.directors.size()>0) {
            ((ViewGroup)uiHandle.directors.getParent()).setVisibility(View.VISIBLE);
            uiHandle.directors.setText(TextUtils.join(", ", TeluguBeatsApp.currentSong.album.directors));
        }
        else{
            ((ViewGroup)uiHandle.directors.getParent()).setVisibility(View.GONE);
        }

        if (TeluguBeatsApp.currentSong.album.actors!=null && TeluguBeatsApp.currentSong.album.actors.size()>0) {
            ((ViewGroup)uiHandle.actors.getParent()).setVisibility(View.VISIBLE);
            uiHandle.actors.setText(TextUtils.join(", ", TeluguBeatsApp.currentSong.album.actors));
        }
        else{
            ((ViewGroup)uiHandle.actors.getParent()).setVisibility(View.GONE);
        }

        if (TeluguBeatsApp.currentSong.album.musicDirectors!=null && TeluguBeatsApp.currentSong.album.musicDirectors.size()>0) {
            ((ViewGroup)uiHandle.musicDirectors.getParent()).setVisibility(View.VISIBLE);
            uiHandle.musicDirectors.setText(TextUtils.join(", ", TeluguBeatsApp.currentSong.album.musicDirectors));
        }
        else{
            ((ViewGroup)uiHandle.musicDirectors.getParent()).setVisibility(View.GONE);
        }


        if(TeluguBeatsApp.currentSong!=null) {
            if(TeluguBeatsApp.blurredCurrentSongBg!=null){
                UiUtils.setBg(layout, new BitmapDrawable(TeluguBeatsApp.blurredCurrentSongBg));
            }
            else {
                Task.callInBackground(new Callable<Bitmap>() {
                    @Override
                    public Bitmap call() throws Exception {
                        Bitmap blurBitmap = TeluguBeatsApp.getUiUtils().fastblur(UiUtils.getBitmapFromURL(TeluguBeatsApp.currentSong.album.imageUrl), 5, 40);
                        return blurBitmap;
                    }
                }).onSuccess(new Continuation<Bitmap, Object>() {
                    @Override
                    public Object then(Task<Bitmap> task) throws Exception {
                        TeluguBeatsApp.blurredCurrentSongBg = task.getResult();
                        TeluguBeatsApp.broadcastEvent(TeluguBeatsApp.NotifierEvent.BLURRED_BG_AVAILABLE, null);
                        return null;
                    }
                }, Task.UI_THREAD_EXECUTOR);

                blurredBgListener = new AppEventListener() {
                    @Override
                    public void onEvent(TeluguBeatsApp.NotifierEvent type, Object data) {
                        UiUtils.setBg(layout, new BitmapDrawable(TeluguBeatsApp.blurredCurrentSongBg));
                    }
                };

                TeluguBeatsApp.addListener(TeluguBeatsApp.NotifierEvent.BLURRED_BG_AVAILABLE, blurredBgListener);
            }
        }

    }


    @Override
    public void onResume() {
        TeluguBeatsApp.onFFTData = new GenericListener<float[]>(){
            @Override
            public void onData(float[] l , float[] r) {
                fftDataLeft = new float[l.length];
                System.arraycopy(l, 0, fftDataLeft, 0, fftDataLeft.length);;
                fftDataRight = new float[l.length];
                System.arraycopy(r, 0, fftDataRight, 0, fftDataRight.length);;

                if(visualizerView!=null)
                    visualizerView.postInvalidate();
                return;
            }
        };

        resetCurrentSong();

        TeluguBeatsApp.addListener(TeluguBeatsApp.NotifierEvent.GENERIC_FEED, feedChangeListener = new AppEventListener() {
            @Override
            public void onEvent(TeluguBeatsApp.NotifierEvent type, Object data) {
                ((ArrayAdapter) uiHandle.scrollingDedications.getAdapter()).notifyDataSetChanged();
                UiUtils.scrollToBottom(uiHandle.scrollingDedications);
                UiUtils.scrollToBottom(uiHandle.scrollView);
            }
        });

        TeluguBeatsApp.addListener(TeluguBeatsApp.NotifierEvent.SONG_CHANGED, songChangeListener= new AppEventListener() {
            @Override
            public void onEvent(TeluguBeatsApp.NotifierEvent type, Object data) {
                resetCurrentSong();
            }
        });
        //add current song change listener
        super.onResume();
    }

    @Override
    public void onPause() {
        TeluguBeatsApp.onFFTData = new GenericListener<>();
        TeluguBeatsApp.removeListener(TeluguBeatsApp.NotifierEvent.BLURRED_BG_AVAILABLE, blurredBgListener);
        TeluguBeatsApp.removeListener(TeluguBeatsApp.NotifierEvent.GENERIC_FEED, feedChangeListener);
        TeluguBeatsApp.removeListener(TeluguBeatsApp.NotifierEvent.SONG_CHANGED, songChangeListener);
        //TODO : remove event listener

        super.onPause();
    }
}
