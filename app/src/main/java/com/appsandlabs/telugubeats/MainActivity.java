package com.appsandlabs.telugubeats;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.appsandlabs.datalisteners.GenericListener;
import com.appsandlabs.datalisteners.TeluguBeatsConfig;

public class MainActivity extends AppCompatActivity {


    MusicService musicService;
    private boolean mBound;
    private Paint blackPaint;
    private Paint greenPaint;

    private float[] fftDataLeft = new float[1024*4];
    private float[] fftDataRight = new float[1024*4];

    private float[] barHeightsLeft = new float[16];
    private float[] barHeightsRight = new float[16];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        blackPaint = new Paint();
        blackPaint.setColor(Color.BLACK);
        blackPaint.setStrokeWidth(3);

        greenPaint  = new Paint();
        greenPaint.setColor(Color.GREEN);
        greenPaint.setStyle(Paint.Style.FILL);


        ((LinearLayout)findViewById(R.id.content)).addView(new View(this){
            @Override
            protected void onDraw(Canvas canvas) {
                for(int i=0;i<32;i++){
                    float max = 0 ;
                    for(int j=0;j<fftDataLeft.length/32;j++){
                        if(max > fftDataLeft[fftDataLeft.length/32*i+j]){
                            max = fftDataLeft[fftDataLeft.length/32*i+j];
                        }
                    }
                    barHeightsLeft[i]  = max;
                }

                for(int i=0;i<32;i++){
                    float max = 0 ;
                    for(int j=0;j<fftDataRight.length/32;j++){
                        if(max > fftDataRight[fftDataRight.length/32*i+j]){
                            max = fftDataRight[fftDataRight.length/32*i+j];
                        }
                    }
                    barHeightsRight[i]  = max;
                }

                for(int i=0;i<16;i++){//horizontal lines
                    int y = i*VisualizerConfig.barHeight/16;
                    canvas.drawLine(0 , y,  (VisualizerConfig.barWidth+VisualizerConfig.barSpacing)*VisualizerConfig.nBars , y , blackPaint);
                }

                for(int i=0;i<VisualizerConfig.nBars;i++){//horizontal lines
                    int x = i*(VisualizerConfig.barWidth+VisualizerConfig.barSpacing);
                    canvas.drawRect(
                            x ,
                            barHeightsLeft[i],
                            0 ,
                            (VisualizerConfig.barWidth+VisualizerConfig.barSpacing)* i +  VisualizerConfig.barWidth ,
                            greenPaint);
                }

                super.onDraw(canvas);
            }
        });
    }


    @Override
    protected void onResume() {
        TeluguBeatsConfig.onFFTData = new GenericListener<float[]>(){
            @Override
            public String onData(float[] l , float[] r) {
                fftDataLeft = l;
                fftDataRight = r;
                return null;
            }
        };
        super.onResume();
    }

    @Override
    protected void onPause() {
        TeluguBeatsConfig.onFFTData = null;
        super.onPause();
    }

    protected void drawFfftData(){
        // draw with pure canvas or surface view
    }




    @Override
    protected void onStart() {
        super.onStart();
        if(mBound) return;

        //connect to background service
        Intent svc=new Intent(this, MusicService.class);
        bindService(svc, new ServiceConnection() {
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
