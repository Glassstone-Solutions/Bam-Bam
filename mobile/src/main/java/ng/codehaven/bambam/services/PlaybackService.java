package ng.codehaven.bambam.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ng.codehaven.bambam.Common;
import ng.codehaven.bambam.helpers.AudioManagerHelper;
import ng.codehaven.bambam.helpers.SongHelper;
import ng.codehaven.bambam.models.Playlist;
import ng.codehaven.bambam.models.Track;

public class PlaybackService extends Service {

    private Common mApp;

    private Context mContext;
    private NotificationCompat.Builder mBuilder;
    private Notification mNotification;
    private NotificationManager mNotifyManager;
    public static int mNotificationId = 92713;

    //MediaPlayer objects and flags.
    private MediaPlayer mMediaPlayer;
    private MediaPlayer mMediaPlayer2;
    private int mCurrentMediaPlayer = 1;
    private boolean mFirstRun = true;

    //AudioManager.
    private AudioManager mAudioManager;
    private AudioManagerHelper mAudioManagerHelper;

    //Playlist implementation
    private List<Track> mQueue;
    private Playlist mPlaylist;

    //Flags that indicate whether the mediaPlayers have been initialized.
    private boolean mMediaPlayerPrepared = false;
    private boolean mMediaPlayer2Prepared = false;

    //Pointer variable.
    private int mCurrentSongIndex;

    //Handler object.
    private Handler mHandler;

    private IBinder myBinder = new MyBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        mApp = (Common) getApplicationContext();
        mContext = this.getApplicationContext();
        mHandler = new Handler();
        mQueue = new ArrayList<>();

        initMediaPlayers();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }

    public void updateNotification(SongHelper mSongHelper) {

    }

    public void updateWidgets() {

    }

    public Track getTrack(int index) {
        if (mQueue != null){
            return mQueue.get(index);
        }
        return null;
    }

    public class MyBinder extends Binder {

        public PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    public OnPreparedListener mediaPlayerPrepared = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            //Update the prepared flag.
            setIsMediaPlayerPrepared(true);
            //Set the completion listener for mMediaPlayer.
            getMediaPlayer().setOnCompletionListener(onMediaPlayerCompleted);

            //TODO: Check for audio focus before starting media player

            startMediaPlayer();
        }
    };

    public OnCompletionListener onMediaPlayerCompleted = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            mCurrentSongIndex++;
            //Reset the volumes for both mediaPlayers.
            getMediaPlayer().setVolume(1.0f, 1.0f);
            getMediaPlayer2().setVolume(1.0f, 1.0f);

            try {
                if (isAtEndOfQueue() && getRepeatMode() != Common.REPEAT_PLAYLIST) {
                    stopSelf();
                } else if (isMediaPlayer2Prepared()) {
                    startMediaPlayer2();
                } else {
                    //Check every 100ms if mMediaPlayer2 is prepared.
                    mHandler.post(startMediaPlayer2IfPrepared);
                }
            } catch (IllegalStateException e) {
                //mMediaPlayer2 isn't prepared yet.
                mHandler.post(startMediaPlayer2IfPrepared);
            }

        }
    };

    public OnPreparedListener mediaPlayer2Prepared = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            setIsMediaPlayer2Prepared(true);

            //Set the completion listener for mMediaPlayer2.
            getMediaPlayer2().setOnCompletionListener(onMediaPlayer2Completed);

            startMediaPlayer2();
        }
    };

    public OnCompletionListener onMediaPlayer2Completed = new OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            mCurrentSongIndex++;
            //Reset the volumes for both mediaPlayers.
            getMediaPlayer().setVolume(1.0f, 1.0f);
            getMediaPlayer2().setVolume(1.0f, 1.0f);

            try {
                if (isAtEndOfQueue() && getRepeatMode() != Common.REPEAT_PLAYLIST) {
                    stopSelf();
                } else if (isMediaPlayerPrepared()) {
                    startMediaPlayer();
                } else {
                    //Check every 100ms if mMediaPlayer2 is prepared.
                    mHandler.post(startMediaPlayerIfPrepared);
                }
            } catch (IllegalStateException e) {
                //mMediaPlayer2 isn't prepared yet.
                mHandler.post(startMediaPlayerIfPrepared);
            }
        }
    };

    /**
     * Error listener for mMediaPlayer.
     */
    public OnErrorListener onErrorListener = new OnErrorListener() {

        @Override
        public boolean onError(MediaPlayer mMediaPlayer, int what, int extra) {
			/* This error listener might seem like it's not doing anything.
			 * However, removing this will cause the mMediaPlayer object to go crazy
			 * and skip around. The key here is to make this method return true. This
			 * notifies the mMediaPlayer object that we've handled all errors and that
			 * it shouldn't do anything else to try and remedy the situation.
			 *
			 * TL;DR: Don't touch this interface. Ever.
			 */
            return true;
        }

    };

    private void setIsMediaPlayerPrepared(boolean b) {
        mMediaPlayerPrepared = b;
    }

    private void setIsMediaPlayer2Prepared(boolean b) {
        mMediaPlayer2Prepared = b;
    }

    private void setCurrentMediaPlayer(int i) {
        mCurrentMediaPlayer = i;
    }

    public void addTrackToPlaylist(Track t, Playlist p) {

    }

    public void playTrack(Track t) throws IOException {
        mQueue.add(t);
        mCurrentSongIndex = mQueue.size();
        MediaPlayer mediaPlayer = getCurrentMediaPlayer();

        if (mFirstRun) {
            mediaPlayer.setDataSource(mContext, Uri.parse(t.getTrackUrl()));
            mediaPlayer.prepareAsync();
            play();
        }
    }

    public void play() {
        if (getCurrentMediaPlayer() == mMediaPlayer) {
            mHandler.post(startMediaPlayerIfPrepared);
        } else {
            mHandler.post(startMediaPlayer2IfPrepared);
        }
    }

    private void pause() {
        if (getCurrentMediaPlayer().isPlaying())
            getCurrentMediaPlayer().pause();
    }

    private void skipBack() {
        setCurrentSongIndex(getCurrentSongIndex() - 1);
        switch (mCurrentMediaPlayer){
            case 1:
                mHandler.post(startMediaPlayer2IfPrepared);
                break;
            default:
                mHandler.post(startMediaPlayerIfPrepared);
                break;
        }
    }

    private void setCurrentSongIndex(int i) {
        if (i < 0 && mQueue.size() == 1)
            mCurrentSongIndex = 1;
        else
            mCurrentSongIndex = i;
    }

    private void skipForward() {

    }

    /**
     * Returns the current active MediaPlayer object.
     */
    public MediaPlayer getCurrentMediaPlayer() {
        if (mCurrentMediaPlayer == 1)
            return mMediaPlayer;
        else
            return mMediaPlayer2;
    }

    private Notification buildNotification(int SmallIcon) {
        return null;
    }

    /**
     * Starts mMediaPlayer if it is prepared and ready for playback.
     * Otherwise, continues checking every 100ms if mMediaPlayer is prepared.
     */
    private Runnable startMediaPlayerIfPrepared = new Runnable() {

        @Override
        public void run() {
            if (isMediaPlayerPrepared())
                startMediaPlayer();
            else
                mHandler.postDelayed(this, 100);

        }

    };

    /**
     * Starts mMediaPlayer if it is prepared and ready for playback.
     * Otherwise, continues checking every 100ms if mMediaPlayer2 is prepared.
     */
    private Runnable startMediaPlayer2IfPrepared = new Runnable() {

        @Override
        public void run() {
            if (isMediaPlayer2Prepared())
                startMediaPlayer2();
            else
                mHandler.postDelayed(this, 100);

        }

    };

    private void startMediaPlayer() throws IllegalStateException {
        //Aaaaand let the show begin!
        setCurrentMediaPlayer(1);
        getMediaPlayer().start();
        //Start preparing the next song.
        prepareMediaPlayer2(determineNextSongIndex());
    }

    private void startMediaPlayer2() throws IllegalStateException {
        //Aaaaand let the show begin!
        setCurrentMediaPlayer(2);
        getMediaPlayer2().start();
        //Start preparing the next song.
        prepareMediaPlayer(determineNextSongIndex());
    }

    /**
     * Grabs the song parameters at the specified index, retrieves its
     * data source, and beings to asynchronously prepare mMediaPlayer.
     * Once mMediaPlayer is prepared, mediaPlayerPrepared is called.
     *
     * @return True if the method completed with no exceptions. False, otherwise.
     */
    private boolean prepareMediaPlayer(int songIndex) {
        try {
            //Stop here if we're at the end of the queue.
            if (songIndex==-1)
                return true;
            //Reset mMediaPlayer2 to its uninitialized state.
            getMediaPlayer().reset();

            //Loop the player if the repeat mode is set to repeat the current song.
            if (getRepeatMode()==Common.REPEAT_SONG) {
                getMediaPlayer().setLooping(true);
            }

            //Set mMediaPlayer2's song data.
            Track t = mQueue.get(songIndex);
        /*
    		 * Set the data source for mMediaPlayer and start preparing it
    		 * asynchronously.
    		 */
            getMediaPlayer().setDataSource(mContext, Uri.parse(t.getTrackUrl()));
            getMediaPlayer().setOnPreparedListener(mediaPlayerPrepared);
            getMediaPlayer().setOnErrorListener(onErrorListener);
            getMediaPlayer().prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();

            //Start preparing the next song.
            if (!isAtEndOfQueue())
                prepareMediaPlayer(songIndex+1);
            else
                return false;

            return false;
        }

        return true;
    }

    /**
     * Grabs the song parameters at the specified index, retrieves its
     * data source, and beings to asynchronously prepare mMediaPlayer2.
     * Once mMediaPlayer2 is prepared, mediaPlayer2Prepared is called.
     *
     * @return True if the method completed with no exceptions. False, otherwise.
     */
    private boolean prepareMediaPlayer2(int songIndex) {
        try {
            //Stop here if we're at the end of the queue.
            if (songIndex==-1)
                return true;
            //Reset mMediaPlayer2 to its uninitialized state.
            getMediaPlayer2().reset();

            //Loop the player if the repeat mode is set to repeat the current song.
            if (getRepeatMode()==Common.REPEAT_SONG) {
                getMediaPlayer2().setLooping(true);
            }

            //Set mMediaPlayer2's song data.
            Track t = mQueue.get(songIndex);
        /*
    		 * Set the data source for mMediaPlayer and start preparing it
    		 * asynchronously.
    		 */
            getMediaPlayer2().setDataSource(mContext, Uri.parse(t.getTrackUrl()));
            getMediaPlayer2().setOnPreparedListener(mediaPlayer2Prepared);
            getMediaPlayer2().setOnErrorListener(onErrorListener);
            getMediaPlayer2().prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();

            //Start preparing the next song.
            if (!isAtEndOfQueue())
                prepareMediaPlayer2(songIndex+1);
            else
                return false;

            return false;
        }

        return true;
    }

    /**
     * Starts/resumes the current media player. Returns true if
     * the operation succeeded. False, otherwise.
     */
    public boolean startPlayback() {

        try {
            getCurrentMediaPlayer().start();
            //Check to make sure we have audio focus.
//            if (checkAndRequestAudioFocus()) {
//                getCurrentMediaPlayer().start();
//
//                //Update the UI and scrobbler.
//                String[] updateFlags = new String[] { Common.UPDATE_PLAYBACK_CONTROLS };
//                String[] flagValues = new String[] { "" };
//
//                mApp.broadcastUpdateUICommand(updateFlags, flagValues);
//                updateNotification(mApp.getService().getCurrentSong());
//                updateWidgets();
//                scrobbleTrack(SimpleLastFMHelper.START);
//
//            } else {
//                return false;
//            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Indicates if mMediaPlayer is prepared and
     * ready for playback.
     */
    public boolean isMediaPlayerPrepared() {
        return mMediaPlayerPrepared;
    }

    /**
     * Indicates if mMediaPlayer2 is prepared and
     * ready for playback.
     */
    public boolean isMediaPlayer2Prepared() {
        return mMediaPlayer2Prepared;
    }

    /**
     * Returns the primary MediaPlayer object. Don't
     * use this method directly unless you have a good
     * reason to explicitly call mMediaPlayer. Use
     * getCurrentMediaPlayer() whenever possible.
     */
    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }

    /**
     * Returns the secondary MediaPlayer object. Don't
     * use this method directly unless you have a good
     * reason to explicitly call mMediaPlayer2. Use
     * getCurrentMediaPlayer() whenever possible.
     */
    public MediaPlayer getMediaPlayer2() {
        return mMediaPlayer2;
    }

    /**
     * Returns the current repeat mode. The repeat mode
     * is determined based on the value that is saved in
     * SharedPreferences.
     */
    public int getRepeatMode() {
        return mApp.getSharedPreferences().getInt(Common.REPEAT_MODE, Common.REPEAT_OFF);
    }

    private boolean isAtEndOfQueue() {
        if (mCurrentSongIndex > mQueue.size()) {
            mCurrentSongIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    public Playlist getPlaylist() {
        return this.mPlaylist;
    }

    public void setPlaylist(Playlist p) {
        this.mPlaylist = p;
    }

    private void setCurrentSong(Track t){

    }

    private int getCurrentSongIndex() {
        return mCurrentSongIndex;
    }

    private int determineNextSongIndex() {
        if (isAtEndOfQueue() && getRepeatMode()==Common.REPEAT_PLAYLIST)
            return 0;
        else if (!isAtEndOfQueue() && getRepeatMode()==Common.REPEAT_SONG)
            return getCurrentSongIndex();
        else if (isAtEndOfQueue())
            return -1;
        else
            return getCurrentSongIndex() + 1;
    }

    private void initMediaPlayers() {
        /*
         * Release the MediaPlayer objects if they are still valid.
		 */
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }

        if (mMediaPlayer2 != null) {
            getMediaPlayer2().release();
            mMediaPlayer2 = null;
        }

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer2 = new MediaPlayer();
        setCurrentMediaPlayer(1);

        getMediaPlayer().reset();
        getMediaPlayer2().reset();

        //Loop the players if the repeat mode is set to repeat the current song.
        if (getRepeatMode() == Common.REPEAT_SONG) {
            getMediaPlayer().setLooping(true);
            getMediaPlayer2().setLooping(true);
        }

        try {
            mMediaPlayer.setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
            getMediaPlayer2().setWakeMode(mContext, PowerManager.PARTIAL_WAKE_LOCK);
        } catch (Exception e) {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer2 = new MediaPlayer();
            setCurrentMediaPlayer(1);
        }

        //Set the mediaPlayers' stream sources.
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        getMediaPlayer2().setAudioStreamType(AudioManager.STREAM_MUSIC);

        mMediaPlayer.setOnPreparedListener(mediaPlayerPrepared);
        mMediaPlayer2.setOnPreparedListener(mediaPlayer2Prepared);

    }

}
