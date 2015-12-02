package ng.codehaven.bambam;

import android.content.Context;
import android.content.Intent;

import ng.codehaven.bambam.interfaces.NowPlayingActivityListener;
import ng.codehaven.bambam.interfaces.PrepareServiceListener;
import ng.codehaven.bambam.ui.activities.NowPlayingActivity;


public class PlaybackKickstarter implements NowPlayingActivityListener, PrepareServiceListener {

    private Context mContext;
    private Common mApp;

    private String mQuerySelection;
    private int mPlaybackRouteId;
    private int mCurrentSongIndex;
    private boolean mPlayAll;

    public PlaybackKickstarter(Context context) {
        mContext = context;
    }

    /**
     * Helper method that calls all the required method(s)
     * that initialize music playback. This method should
     * always be called when the cursor for the service
     * needs to be changed.
     */

    public void initPlayback(Context context,
                             String querySelection,
                             int playbackRouteId,
                             int currentSongIndex,
                             boolean showNowPlayingActivity,
                             boolean playAll){
        mApp = (Common) mContext.getApplicationContext();
        mQuerySelection = querySelection;
        mPlaybackRouteId = playbackRouteId;
        mCurrentSongIndex = currentSongIndex;
        mPlayAll = playAll;

        if (showNowPlayingActivity) {
            //Launch NowPlayingActivity.
            Intent intent = new Intent(mContext, NowPlayingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(NowPlayingActivity.START_SERVICE, true);
            mContext.startActivity(intent);

        } else {
            //Start the playback service if it isn't running.
            if (!mApp.isServiceRunning()) {
                startService();
            } else {
                //Call the callback method that will start building the new cursor.
                mApp.getService()
                        .getPrepareServiceListener()
                        .onServiceRunning(mApp.getService());
            }

        }

    }

    /**
     * Starts AudioPlaybackService. Once the service is running, we get a
     * callback to onServiceRunning() (see below). That's where the method to
     * build the cursor is called.
     */
    private void startService() {
        Intent intent = new Intent(mContext, AudioPlaybackService.class);
        mContext.startService(intent);
    }

    @Override
    public void onServiceRunning(AudioPlaybackService service) {

    }

    @Override
    public void onServiceFailed(Exception exception) {

    }

    @Override
    public void onNowPlayingActivityReady() {

    }
}
