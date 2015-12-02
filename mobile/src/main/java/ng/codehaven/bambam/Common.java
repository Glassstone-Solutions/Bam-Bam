package ng.codehaven.bambam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseRole;
import com.squareup.picasso.Picasso;

import ng.codehaven.bambam.interfaces.PrepareServiceListener;
import ng.codehaven.bambam.services.MusicService;
import ng.codehaven.bambam.services.PlaybackService;

public class Common extends MultiDexApplication {

    private static Common sInstance;

    private RequestQueue mRequestQueue;

    private PlaybackService mService;

    //Playback kickstarter object.
    private PlaybackKickstarter mPlaybackKickstarter;

    //Broadcast elements.
    private LocalBroadcastManager mLocalBroadcastManager;
    public static final String UPDATE_UI_BROADCAST = "ng.codehaven.bambam.NEW_SONG_UPDATE_UI";

    //Update UI broadcast flags.
    public static final String SHOW_AUDIOBOOK_TOAST = "AudiobookToast";
    public static final String UPDATE_SEEKBAR_DURATION = "UpdateSeekbarDuration";
    public static final String UPDATE_PAGER_POSTIION = "UpdatePagerPosition";
    public static final String UPDATE_PLAYBACK_CONTROLS = "UpdatePlabackControls";
    public static final String SERVICE_STOPPING = "ServiceStopping";
    public static final String SHOW_STREAMING_BAR = "ShowStreamingBar";
    public static final String HIDE_STREAMING_BAR = "HideStreamingBar";
    public static final String UPDATE_BUFFERING_PROGRESS = "UpdateBufferingProgress";
    public static final String INIT_PAGER = "InitPager";
    public static final String NEW_QUEUE_ORDER = "NewQueueOrder";
    public static final String UPDATE_EQ_FRAGMENT = "UpdateEQFragment11";

    //Picasso instance.
    private Picasso mPicasso;
    private Context mContext;

    //Repeat mode constants.
    public static final int REPEAT_OFF = 0;
    public static final int REPEAT_PLAYLIST = 1;
    public static final int REPEAT_SONG = 2;
    public static final int A_B_REPEAT = 3;

    //SharedPreferences.
    private static SharedPreferences mSharedPreferences;

    public static SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    //SharedPreferences keys.
    public static final String CROSSFADE_ENABLED = "CrossfadeEnabled";
    public static final String CROSSFADE_DURATION = "CrossfadeDuration";
    public static final String REPEAT_MODE = "RepeatMode";
    public static final String MUSIC_PLAYING = "MusicPlaying";
    public static final String SERVICE_RUNNING = "ServiceRunning";
    public static final String CURRENT_LIBRARY = "CurrentLibrary";
    public static final String CURRENT_LIBRARY_POSITION = "CurrentLibraryPosition";
    public static final String SHUFFLE_ON = "ShuffleOn";
    public static final String FIRST_RUN = "FirstRun";
    public static final String STARTUP_BROWSER = "StartupBrowser";
    public static final String SHOW_LOCKSCREEN_CONTROLS = "ShowLockscreenControls";
    public static final String ARTISTS_LAYOUT = "ArtistsLayout";
    public static final String ALBUM_ARTISTS_LAYOUT = "AlbumArtistsLayout";
    public static final String ALBUMS_LAYOUT = "AlbumsLayout";
    public static final String PLAYLISTS_LAYOUT = "PlaylistsLayout";
    public static final String GENRES_LAYOUT = "GenresLayout";
    public static final String FOLDERS_LAYOUT = "FoldersLayout";

    private boolean mIsServiceRunning = false;

    public synchronized static Common getsInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Application context.
        mContext = getApplicationContext();

        //Picasso.
        mPicasso = new Picasso.Builder(mContext).build();

        Parse.initialize(this, Constants.PARSE_APP_KEY, Constants.PARSE_CLIENT_KEY);
        ParseFacebookUtils.initialize(this);
        ParseRole user = new ParseRole("userRole");
        user.saveInBackground();
        ParseInstallation.getCurrentInstallation().saveInBackground();

        mRequestQueue = Volley.newRequestQueue(this);

        sInstance = this;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public PlaybackService getService() {
        return mService;
    }

    public void setService(PlaybackService mService) {
        this.mService = mService;
    }

    public boolean isServiceRunning() {
        return mIsServiceRunning;
    }
    public void setIsServiceRunning(boolean running){
        mIsServiceRunning = running;
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public PrepareServiceListener getPlaybackKickstarter() {
        return mPlaybackKickstarter;
    }

}
