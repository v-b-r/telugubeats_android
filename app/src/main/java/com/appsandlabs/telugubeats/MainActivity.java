package com.appsandlabs.telugubeats;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.appsandlabs.datalisteners.GenericListener;
import com.appsandlabs.datalisteners.TeluguBeatsConfig;

public class MainActivity extends AppCompatActivity {


    MusicService musicService;
    private boolean mBound;
    private Paint blackPaint;
    private Paint greenPaint;

    private float[] fftDataLeft = new float[1024/2];
    private float[] fftDataRight = new float[1024/2];

    private float[] barHeightsLeft = new float[VisualizerConfig.nBars];
    private float[] barHeightsRight = new float[VisualizerConfig.nBars];
    private View visualizerView;
    public ServiceConnection serviceConnection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(3);

        greenPaint  = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStrokeWidth(3);
        greenPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        ((LinearLayout)findViewById(R.id.content)).addView(visualizerView = new View(this) {
            @Override
            protected void onDraw(Canvas canvas) {
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
//                            greenPaint);
//                }
                int xOffset = 0;//  (VisualizerConfig.barWidth + VisualizerConfig.barSpacing)*VisualizerConfig.nBars
                for (int i = 0; i < VisualizerConfig.nBars / 2; i ++) {//horizontal lines
                    int x = i * (VisualizerConfig.barWidth + VisualizerConfig.barSpacing) + xOffset;
                    int max = VisualizerConfig.nBars/2;

                    //draw max/2-i , max/2+i

                    canvas.drawRect(
                            x,
                            VisualizerConfig.barHeight - (barHeightsRight[max - i]) / 8,//2*VisualizerConfig.nBars-1-i
                            x + VisualizerConfig.barWidth,
                            VisualizerConfig.barHeight,
                            greenPaint);

                }

                for (int i = 0; i < VisualizerConfig.nBars/2; i++) {//horizontal lines
                    int x =  (i+VisualizerConfig.nBars/2)* (VisualizerConfig.barWidth + VisualizerConfig.barSpacing) + xOffset;
                    int max = VisualizerConfig.nBars;

                    canvas.drawRect(
                            x,
                            VisualizerConfig.barHeight - (barHeightsRight[max-1-i]) / 8,//2*VisualizerConfig.nBars-1-i
                            x + VisualizerConfig.barWidth,
                            VisualizerConfig.barHeight,
                            greenPaint);
                }

                for (
                        int i = 0;
                        i < VisualizerConfig.hLines; i++)

                {//horizontal lines
                    int y = i * VisualizerConfig.barHeight / VisualizerConfig.hLines;
                    canvas.drawLine(0, y, (VisualizerConfig.barWidth + VisualizerConfig.barSpacing) * VisualizerConfig.nBars * 2, y, blackPaint);
                }

            }
        }, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        visualizerView.setBackground(new ColorDrawable(Color.BLACK));
    }


    @Override
    protected void onResume() {
        TeluguBeatsConfig.onFFTData = new GenericListener<float[]>(){
            @Override
            public String onData(float[] l , float[] r) {
                fftDataLeft = new float[l.length];
                System.arraycopy(l, 0, fftDataLeft, 0, fftDataLeft.length);;
                fftDataRight = new float[l.length];
                System.arraycopy(r, 0, fftDataRight, 0, fftDataRight.length);;

                if(visualizerView!=null)
                    visualizerView.postInvalidate();
                return null;
            }
        };
        super.onResume();
    }

    @Override
    protected void onPause() {
        TeluguBeatsConfig.onFFTData = new GenericListener<>();
        if(mBound) {
            unbindService(serviceConnection);
            mBound = false;
        }
        super.onPause();
    }

    protected void drawFfftData(){
        // draw with pure canvas or surface view
    }




    @Override
    protected void onStart() {
        super.onStart();
        if(mBound) return;
        Intent svc=new Intent(this, MusicService.class);
        startService(svc);
        //connect to background service
        bindService(svc, serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicServiceBinder binder = (MusicService.MusicServiceBinder) service;
                musicService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBound = false;
            }
        }, Context.BIND_AUTO_CREATE);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
