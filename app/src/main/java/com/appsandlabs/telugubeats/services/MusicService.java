package com.appsandlabs.telugubeats.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.appsandlabs.telugubeats.R;
import com.appsandlabs.telugubeats.TeluguBeatsApp;
import com.appsandlabs.telugubeats.audiotools.FFT;
import com.appsandlabs.telugubeats.audiotools.TByteArrayOutputStream;
import com.appsandlabs.telugubeats.config.Config;
import com.appsandlabs.telugubeats.helpers.ServerCalls;
import com.appsandlabs.telugubeats.helpers.UiUtils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * Created by abhinav on 9/21/15.
 */
public class MusicService extends Service {


    private MusicServiceBinder serviceBinder;
    private MusicPlayThread playingThread;

    public static final int FFT_N_SAMPLES = 2 * 1024;
    private FFT leftFft = new FFT(FFT_N_SAMPLES, 44100);
    private FFT rightFft = new FFT(FFT_N_SAMPLES, 44100);


    public static final String NOTIFY_DELETE = "com.appsandlabs.telugubeats.delete";
    public static final String NOTIFY_PAUSE = "com.appsandlabs.telugubeats.pause";
    public static final String NOTIFY_PLAY = "com.appsandlabs.telugubeats.play";

    //this is a dummy binder , will just use methods from the original service class only
    public static class MusicServiceBinder extends Binder {
        private final MusicService musicService;

        public MusicServiceBinder(MusicService service) {
            this.musicService = service;
        }

        public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return musicService;
        }

    }


    public IBinder onBind(Intent arg0) {
        Log.d("telugubeats_log", "bind");
        return serviceBinder == null ? (serviceBinder = new MusicServiceBinder(this)) : serviceBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("telugubeats_log", "create");
        done = false;
        playStream();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("telugubeats_log", "onstartCommad");

        addMessageHandlers();


        return START_STICKY;
    }

    private void addMessageHandlers() {
        TeluguBeatsApp.onSongChanged = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
//                String message = (String)msg.obj;
                newNotification();
                return false;
            }
        });
        TeluguBeatsApp.showDeletenotification = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                newNotification(true);
                return false;
            }
        });
        TeluguBeatsApp.onSongPlayPaused = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
//                String message = (String)msg.obj;
                Log.e(Config.ERR_LOG_TAG, "recieved msg to handler " + msg);
                Integer shouldPlay = (Integer) msg.obj;
                if(shouldPlay==0){
                    playStream();
                    newNotification();
                }
                else if (shouldPlay==1){
                    stopStream();
                    newNotification();
                }
                else{
                    stopStream();
                    stopSelf();
                    stopForeground(true);
                }
                return false;
            }
        });
    }


    @Override
    public void onDestroy() {
        Log.d("telugubeats_log", "destroy");
        done = true;
        TeluguBeatsApp.sfd_ser = null;
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    //music running thread , it just runs with a pause and stop methods
    public static class MusicPlayThread extends Thread {
        private MusicService musicService;
        private String streamId = "telugu";


        MusicPlayThread(MusicService service) {
            this(service, "telugu");
        }

        MusicPlayThread(MusicService service, String streamId) {
            musicService = service;
            this.streamId = streamId;
        }


        @Override
        public void run() {
            try {
                URL url = new URL(ServerCalls.SERVER_ADDR + "/stream/" + streamId + "/audio");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                musicService.decode(con.getInputStream());
            } catch (IOException | DecoderException e) {
                Log.d("telugubeats_log", "some error in thread");
                e.printStackTrace();
            }
        }

        public void restartStream(String streamId) {
            musicService.stopStream();
            start();
        }
    }


    public static boolean done;
    private float[] fftArrayLeft;
    private float[] fftArrayRight;



    public byte[] decode(InputStream stream)
            throws IOException, DecoderException {
        TByteArrayOutputStream audioLeft = new TByteArrayOutputStream(FFT_N_SAMPLES);
        TByteArrayOutputStream audioRight = new TByteArrayOutputStream(FFT_N_SAMPLES);

        float totalMs = 0;
        boolean seeking = true;
        int frames = 0;

        int bufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize, AudioTrack.MODE_STREAM);

        track.play();
        InputStream inputStream = new BufferedInputStream(stream, 2 * FFT_N_SAMPLES);

        fftArrayLeft = new float[leftFft.specSize()];
        fftArrayRight = new float[rightFft.specSize()];

        try {
            Bitstream bitstream = new Bitstream(inputStream);
            Decoder decoder = new Decoder();
            done = false;
            while (!done) {
                Header frameHeader = bitstream.readFrame();
                if (frameHeader == null) {
                    done = true;
                } else {
                    totalMs += frameHeader.ms_per_frame();

                    SampleBuffer output = (SampleBuffer) decoder.decodeFrame(frameHeader, bitstream);

                    if (output.getSampleFrequency() != 44100
                            || output.getChannelCount() != 2) {
                        throw new DecoderException("mono or non-44100 MP3 not supported", null);
                    }

                    short[] pcm = output.getBuffer();
                    if (frames > 2) {
                        frames = 0;
                        audioLeft.reset();
                        audioRight.reset();
                        if (TeluguBeatsApp.onFFTData != null) {
                            leftFft.forward(audioLeft.getBuffer());
                            for (int i = 0; i < leftFft.specSize(); i++) {
                                fftArrayLeft[i] = leftFft.getBand(i);
                            }

                            rightFft.forward(audioRight.getBuffer());
                            for (int i = 0; i < rightFft.specSize(); i++) {
                                fftArrayRight[i] = rightFft.getBand(i);
                            }
                            TeluguBeatsApp.onFFTData.onData(fftArrayLeft, fftArrayRight);
                        }
                    }
                    for (short s : pcm) {
                        audioLeft.write(s & 0xff);
                        audioRight.write((s >> 8) & 0xff);
                    }
                    frames += 1;
                    track.write(pcm, 0, pcm.length);

                }
                bitstream.closeFrame();
            }

            return null;
        } catch (BitstreamException e) {
            throw new IOException("Bitstream error: " + e);
        } catch (DecoderException e) {
            Log.w(Config.ERR_LOG_TAG, "Decoder error", e);
            throw new DecoderException("Decoder error", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }


    boolean currentVersionSupportBigNotification = Config.currentVersionSupportBigNotification();
    boolean currentVersionSupportLockScreenControls = Config.currentVersionSupportLockScreenControls();

    @SuppressLint("NewApi")
    private void newNotification() {
        newNotification(false);
    }


        /**
         * Notification
         * Custom Bignotification is available from API 16
         */
    @SuppressLint("NewApi")
    private void newNotification(boolean showDeleteButton) {
        RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_notification);
        RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

        final Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_music)
                .setContentTitle(TeluguBeatsApp.currentSong.title + " - " + TeluguBeatsApp.currentSong.album.name)
                .build();

        setListeners(simpleContentView);
        setListeners(expandedView);

        notification.contentView = simpleContentView;
        if (currentVersionSupportBigNotification) {
            notification.bigContentView = expandedView;
        }


        Bitmap albumArt =  UiUtils.getBitmapFromURL(TeluguBeatsApp.currentSong.album.imageUrl);

        if (albumArt != null) {
            notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
            }
        } else {
            notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
            }
        }


        if (MusicService.done) {
            notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
                notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
            }
        } else {
            notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

            if (currentVersionSupportBigNotification) {
                notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
                notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
            }
        }
        if(!showDeleteButton) {
            notification.contentView.setViewVisibility(R.id.btnDelete, View.GONE);
            if (currentVersionSupportBigNotification) {
                notification.contentView.setViewVisibility(R.id.btnDelete, View.GONE);

            }
        }

        notification.contentView.setTextViewText(R.id.textSongName, TeluguBeatsApp.currentSong.title);
        notification.contentView.setTextViewText(R.id.textAlbumName, TeluguBeatsApp.currentSong.album.name);
        if (currentVersionSupportBigNotification) {
            notification.bigContentView.setTextViewText(R.id.textSongName, TeluguBeatsApp.currentSong.title);
            notification.bigContentView.setTextViewText(R.id.textAlbumName, TeluguBeatsApp.currentSong.album.name);
        }
        notification.flags |= Notification.FLAG_ONGOING_EVENT;


        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        manager.updateAppWidget(Config.NOTIFICATION_ID, notification.contentView);

        startForeground(Config.NOTIFICATION_ID, notification);
    }

    /**
     * Notification click listeners
     *
     * @param view
     */
    public void setListeners(RemoteViews view) {
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent play = new Intent(NOTIFY_PLAY);


        PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

    }

    final Object sync = new Object();
    private void playStream() {
        synchronized (sync) {
            done = false;
            playingThread = new MusicPlayThread(this);
            //downloads stream and starts playing mp3 music and keep updating polls
            playingThread.start();
        }
    }

    private void stopStream(){
        synchronized (sync) {
            done = true;
            try {
                playingThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //stopped here
        }
    }

}