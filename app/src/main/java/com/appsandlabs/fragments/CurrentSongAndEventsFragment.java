package com.appsandlabs.fragments;

import android.content.Context;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.appsandlabs.TeluguBeatsApp;
import com.appsandlabs.datalisteners.GenericListener;
import com.appsandlabs.helpers.ABTemplating;
import com.appsandlabs.helpers.UiUtils;
import com.appsandlabs.telugubeats.MusicService;
import com.appsandlabs.telugubeats.R;
import com.appsandlabs.telugubeats.VisualizerConfig;
import com.appsandlabs.widgets.PollsListView;
import com.squareup.picasso.Picasso;

/**
 * Created by abhinav on 10/2/15.
 */
public class CurrentSongAndEventsFragment extends Fragment {

    MusicService musicService;
    private boolean mBound;
    private Paint hLinesPaint;
    private Paint barPaint;

    private float[] fftDataLeft = new float[1024/2];
    private float[] fftDataRight = new float[1024/2];

    private float[] barHeightsLeft = new float[VisualizerConfig.nBars];
    private float[] barHeightsRight = new float[VisualizerConfig.nBars];
    private View visualizerView;
    public ServiceConnection serviceConnection;
    private ABTemplating.ABView layout;


    public static class UiHandle{
        ImageView playingImage;
        TextView actorsList;
        TextView directors;
        TextView singersList;
        TextView scrollingDedicationsList;
        TextView currentUsersCount;
        TextView pollsHeading;
        PollsListView polls;
    }

    UiHandle uiHandle = new UiHandle();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        FrameLayout mainLayout = (FrameLayout) inflater.inflate(R.layout.music_and_polls_fragment, null);
        Context ctx = getActivity();
        hLinesPaint = new Paint();
        hLinesPaint.setColor(getResources().getColor(R.color.default_bg));
        hLinesPaint.setStrokeWidth(3);

        barPaint = new Paint();
        barPaint.setStrokeWidth(1);
        barPaint.setShader(new LinearGradient(0, 0, 0, VisualizerConfig.barHeight, Color.BLUE, Color.argb(255, 200, 200, 200), Shader.TileMode.MIRROR));
        barPaint.setStyle(Paint.Style.FILL);

        ((LinearLayout)mainLayout.findViewById(R.id.fg)).addView(layout = TeluguBeatsApp.abTemplating.getPlayerAndPollsView());
        layout.getCell("playingImage").addView(uiHandle.playingImage = new ImageView(ctx));
        layout.getCell("singers").addView(uiHandle.singersList = new TextView(ctx));
        layout.getCell("directors").addView(uiHandle.directors = new TextView(ctx));
        layout.getCell("actors").addView(uiHandle.actorsList = new TextView(ctx));
        uiHandle.scrollingDedicationsList  = layout.getCell("scrolling_dedications").getLabel();
        uiHandle.currentUsersCount  = layout.getCell("live_users").getLabel();
        uiHandle.pollsHeading  = layout.getCell("live_polls_heading").getLabel();
        layout.getCell("live_polls_list").addView(uiHandle.polls = new PollsListView(ctx));


        if(TeluguBeatsApp.currentPoll!=null)
            uiHandle.polls.resetPolls(TeluguBeatsApp.currentPoll);
        if(TeluguBeatsApp.currentSong!=null)
            Picasso.with(getActivity()).load(TeluguBeatsApp.currentSong.album.imageUrl).into(uiHandle.playingImage);

        UiUtils.setBg(layout, TeluguBeatsApp.currentSong.album.imageUrl);

        layout.getCell("visualizer").addView(visualizerView = new View(ctx) {

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

            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                VisualizerConfig.barWidth = (right - left) / VisualizerConfig.nBars - VisualizerConfig.barSpacing;

                super.onLayout(changed, left, top, right, bottom);
            }
        }, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, VisualizerConfig.barHeight));

        // load current polls and poll data
        // get current playing currentSong
        // get current
        return mainLayout;
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
        super.onResume();
    }

    @Override
    public void onPause() {
        TeluguBeatsApp.onFFTData = new GenericListener<>();
        super.onPause();
    }
}
