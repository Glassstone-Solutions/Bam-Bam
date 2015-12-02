package ng.codehaven.bambam.tasks;

import android.media.MediaPlayer;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Thompson on 9/28/2015.
 */
public abstract class FadeVolumeTask extends TimerTask {
    public static final int FADE_IN = 0;
    public static final int FADE_OUT = 1;

    private int mCurrentStep = 0;
    private int mSteps;
    private int mMode;

    private MediaPlayer mp;

    /**
     * Constructor, launches timer immediately
     * @param mediaPlayer
     *            MediaPlayer being affected
     * @param mode
     *            Volume fade mode <code>FADE_IN</code> or
     *            <code>FADE_OUT</code>
     * @param millis
     *            Time the fade process should take
     */
    public FadeVolumeTask(MediaPlayer mediaPlayer, int mode, int millis) {
        this.mp = mediaPlayer;
        this.mMode = mode;
        this.mSteps = millis / 20; // 20 times per second
        this.onPreExecute();
        new Timer().scheduleAtFixedRate(this, 0, millis / mSteps);
    }

    @Override
    public void run() {
        float volumeValue = 1.0f;

        if (mMode == FADE_OUT) {
            volumeValue *= (float) (mSteps - mCurrentStep) / (float) mSteps;
        } else {
            volumeValue *= (float) (mCurrentStep) / (float) mSteps;
        }

        try {
            mp.setVolume(volumeValue, volumeValue);
        } catch (Exception e) {
            return;
        }

        if (mCurrentStep >= mSteps) {
            this.onPostExecute();
            this.cancel();
        }

        mCurrentStep++;
    }

    /**
     * Task executed before launching timer
     */
    public abstract void onPreExecute();

    /**
     * Task executer after timer finished working
     */
    public abstract void onPostExecute();
}
