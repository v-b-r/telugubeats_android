package com.appsandlabs.telugubeats;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.appsandlabs.audiotools.FFT;
import com.appsandlabs.audiotools.TByteArrayOutputStream;
import com.appsandlabs.datalisteners.TeluguBeatsConfig;

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

    public static final int FFT_N_SAMPLES = 2*1024;
    private FFT leftFft = new FFT(FFT_N_SAMPLES , 44100);
    private FFT rightFft = new FFT(FFT_N_SAMPLES , 44100);


    //this is a dummy binder , will just use methods from the original service class only
    public static class MusicServiceBinder extends Binder {
        private final MusicService musicService;

        public MusicServiceBinder(MusicService service){
            this.musicService = service;
        }
        public MusicService getService() {
            // Return this instance of LocalService so clients can call public methods
            return musicService;
        }

    }


    public IBinder onBind(Intent arg0) {
        Log.d("telugubeats_log", "bind");
        return serviceBinder==null? (serviceBinder = new MusicServiceBinder(this)) : serviceBinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("telugubeats_log", "create");

        playingThread  = new MusicPlayThread(this);
        //downloads stream and starts playing mp3 music and keep updating polls
        playingThread.start();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("telugubeats_log", "onstartCommad");
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d("telugubeats_log", "destroy");
        done = true;
        TeluguBeatsConfig.sfd_ser = null;
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    //music running thread , it just runs with a pause and stop methods
    public static class MusicPlayThread extends Thread{
        private final MusicService musicService;

        MusicPlayThread(MusicService service){
            musicService = service;
        }


        @Override
        public void run() {
                try {
                    if(musicService.pause) return;
                     URL url = new URL("http://192.168.0.102:8888/stream/telugu");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    musicService.decode(con.getInputStream());
                } catch (IOException | DecoderException e) {
                    Log.d("telugubeats_log", "some error in thread");
                    e.printStackTrace();
                }
        }
    }





    public boolean done;
    public boolean pause;
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
        AudioTrack track=new AudioTrack(AudioManager.STREAM_MUSIC,44100,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT, bufferSize,AudioTrack.MODE_STREAM);

        track.play();
        InputStream inputStream = new BufferedInputStream(stream, 2*FFT_N_SAMPLES);

        fftArrayLeft = new float[leftFft.specSize()];
        fftArrayRight = new float[rightFft.specSize()];

        try {
            Bitstream bitstream = new Bitstream(inputStream);
            Decoder decoder = new Decoder();
            done = false;
            while (! done) {
                if(pause) continue;
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
                        if(frames>2){
                            frames = 0;
                            audioLeft.reset();
                            audioRight.reset();
                            if(TeluguBeatsConfig.onFFTData!=null) {
                                leftFft.forward(audioLeft.getBuffer());
                                for (int i = 0; i < leftFft.specSize(); i++) {
                                    fftArrayLeft[i] = leftFft.getBand(i);
                                }

                                rightFft.forward(audioRight.getBuffer());
                                for (int i = 0; i < rightFft.specSize(); i++) {
                                    fftArrayRight[i] = rightFft.getBand(i);
                                }
                                TeluguBeatsConfig.onFFTData.onData( fftArrayLeft, fftArrayRight);
                            }
                        }
                        for (short s : pcm) {
                            audioLeft.write(s & 0xff);
                            audioRight.write((s >> 8 ) & 0xff);
                        }
                        frames+=1;
                        track.write(pcm, 0, pcm.length);

                }
                bitstream.closeFrame();
            }

            return null;
        } catch (BitstreamException e) {
            throw new IOException("Bitstream error: " + e);
        } catch (DecoderException e) {
            Log.w("telugubeats", "Decoder error", e);
            throw new DecoderException("Decoder error", e);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}