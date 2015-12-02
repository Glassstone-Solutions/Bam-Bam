package ng.codehaven.bambam.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.realm.Realm;
import ng.codehaven.bambam.helpers.SongHelper;
import ng.codehaven.bambam.models.Track;

public class MusicService extends Service implements
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnPreparedListener {

    public static final String ACTION_ADD = "ng.codehaven.bambam.services.ACTION_CMD";
    public static final String ACTION_START = "ng.codehaven.bambam.services.ACTION_START";
    public static final String ACTION_END = "ng.codehaven.bambam.services.ACTION_END";
    public static final String ACTION_NEXT = "ng.codehaven.bambam.services.ACTION_NEXT";

    public static final int TRACK_LOADING = 0;
    public static final int TRACK_PREPARED = 1;
    public static final int TRACK_PLAYING = 2;
    public static final int TRACK_PAUSED = 3;
    public static final int TRACK_DONE = 4;
    public static final int TRACK_BUFFERING = 5;

    private int mPlayIndex, mPlayhead;

    private ConcurrentLinkedQueue<Track> mTracks;
    private List<Track>mPlayingQueue;
    private Track nTU;
    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private MediaPlayer mMediaPlayer2 = new MediaPlayer();

    private LocalBinder mLocalBinder = new LocalBinder();
    private Handler handler;

    private boolean isPlayStarted, isPlayPaused, isPrepared;
    private Intent i2;
    private String nowPlaying;

    private Realm realm;

    private Callbacks mCallback;

    public void setCallbacks(Callbacks callbacks){
        this.mCallback = callbacks;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mPlayingQueue = new ArrayList<>();
        mTracks = new ConcurrentLinkedQueue<>();

        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.reset();

        mMediaPlayer2.setOnCompletionListener(this);
        mMediaPlayer2.setOnBufferingUpdateListener(this);
        mMediaPlayer2.setOnSeekCompleteListener(this);
        mMediaPlayer2.setOnPreparedListener(this);
        mMediaPlayer2.reset();

        realm = Realm.getInstance(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Intent i1 = new Intent(ACTION_START);
        i2 = new Intent(ACTION_NEXT);
        if (ACTION_ADD.equals(action)) {
            // If Broadcast not sent, send it

            Track t = realm.where(Track.class).equalTo("parseId", intent.getStringExtra("trackId")).findFirst();
            i1.putExtra("trackId", t.getParseId());
            if (!isPlayStarted){
                sendBroadcast(i1);
                isPlayStarted = true;
            }
            addToQueue(t);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private synchronized void addToQueue(Track track) {
        if (mMediaPlayer != null && track != null) {
            mPlayingQueue.add(track);
            if (!mMediaPlayer.isPlaying()) {
                try {
                    mCallback.playState(track, TRACK_LOADING, 0,0);
                    i2.putExtra("trackId", track.getParseId());
                    sendBroadcast(i2);
                    mMediaPlayer.setDataSource(track.getTrackUrl());
                    mMediaPlayer.prepareAsync();
                } catch (IOException | IllegalStateException | IllegalArgumentException e) {
                    e.printStackTrace();
                }
            } else {
                mTracks.offer(track);
            }
        } else {
            Intent i = new Intent(ACTION_END);
            mCallback.playState(null, TRACK_DONE, 0,0);
            sendBroadcast(i);
            stopSelf();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mLocalBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int i) {
        mCallback.playState(null, TRACK_BUFFERING, mediaPlayer.getCurrentPosition(),mediaPlayer.getDuration());
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mMediaPlayer.reset();
        nTU = mTracks.poll();
        mPlayIndex++;
        if (mPlayingQueue != null &&!mPlayingQueue.isEmpty()){
            mPlayIndex++;
            if (mPlayIndex >= mPlayingQueue.size()){
                mPlayIndex = 0;
            }

            try {
                mCallback.playState(mPlayingQueue.get(mPlayIndex), TRACK_LOADING, 0,0);
                mMediaPlayer.setDataSource(this, Uri.parse(mPlayingQueue.get(mPlayIndex).getTrackUrl()));
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
                Intent i = new Intent(ACTION_END);
                mCallback.playState(null, TRACK_DONE, 0,0);
                sendBroadcast(i);
                stopSelf();
            }

        } else {
            mCallback.playState(null, TRACK_DONE, 0, 0);
            Intent i = new Intent(ACTION_END);
            sendBroadcast(i);
            stopSelf();
        }

    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        isPlayStarted = true;
        mCallback.playState(null, TRACK_PREPARED, 0, mediaPlayer.getDuration());
        handler = new Handler();
        handler.removeCallbacks(progressCallback);
        handler.postDelayed(progressCallback, 1000);
    }

    private Runnable progressCallback = new Runnable() {
        @Override
        public void run() {
            LogMediaPosition();
            handler.postDelayed(this, 1000);
        }
    };

    private void LogMediaPosition() {
        if (mMediaPlayer.isPlaying()){
            mCallback.playState(null, TRACK_PLAYING, mMediaPlayer.getCurrentPosition(), mMediaPlayer.getDuration());
        }
    }

    public boolean pausePlayback() {
        boolean isPlaying = true;
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            isPlaying = false;
        }
        return isPlaying;
    }

    public boolean startPlayback() {
        boolean isPlaying = false;
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
            isPlaying = true;
        }
        return isPlaying;
    }

    public Track getTrack(int index) {
        return mPlayingQueue.get(index);
    }



    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public interface Callbacks{
        void isPrepared(Boolean p);
        void getTrack(Track t);
        void getProgress(int progress);
        void playState(Track t, int state, int progress, int max);
    }
}
