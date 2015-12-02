package ng.codehaven.bambam.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.parse.ParseObject;

import butterknife.InjectView;
import ng.codehaven.bambam.R;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.services.UploadArtService;
import ng.codehaven.bambam.services.UploadTuneService;
import ng.codehaven.bambam.ui.BaseActivity;
import ng.codehaven.bambam.utils.FileHelper;
import ng.codehaven.bambam.utils.ParseProxyObject;

public class SendTuneActivity extends BaseActivity implements View.OnClickListener {

    public static final int PICK_ART_REQUEST = 0;
    public static final int PICK_MP3_REQUEST = 1;
    public static final int PIC_DONE = 0;
    protected Uri mArtMediaUri;
    protected Uri mMp3MediaUri;
    @InjectView(R.id.toolbar)
    protected Toolbar mToolBar;
    boolean mUploadArtBroadcastIsRegistered, mSaveArtBroadcastIsRegistered, mDoneTrackS3SaveBroadcastReceiver, mProgressTuneBroadcastReceiver;
    int artProgress = 1;
    int tuneProgress = 1;
    int saveArtState = -1;
    int tp = 1;
    boolean artSet, progStart;
    boolean bothReady;
    String mTitle, mDesc, mArtFileUrl;
    @InjectView(R.id.art)
    ImageView mArt;
    @InjectView(R.id.chooseMp3)
    Button mChooseMp3Btn;
    @InjectView(R.id.title)
    EditText tEV;
    @InjectView(R.id.desc)
    EditText dEV;
    @InjectView(R.id.progressLayout)
    LinearLayout mPLayout;
    @InjectView(R.id.progress)
    ProgressBar mSaveProgressBar;
    Handler h = new Handler();
    int p = 1;
    private ParseObject tune;
    private BroadcastReceiver doneTrackS3SaveBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tune.put(Track.TRACK_URL, intent.getStringExtra("s3_key"));

            tune.saveEventually();



            finish();
        }
    }, doneArtProgressBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent i) {
            updateArtProgress(i.getIntExtra("progress", 1));
        }
    }, doneArtSaveBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            saveArtState = intent.getIntExtra("saveArtState", -1);
            Log.w("URL", intent.getStringExtra("artUrl"));

            createNewTune(intent.getStringExtra("artUrl"));

            Intent i = new Intent(SendTuneActivity.this, ChooseMp3Activity.class);
            i.putExtra("tune", new ParseProxyObject(tune));

            startActivity(i);

        }
    }, progressTuneBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateTuneProgress(intent.getIntExtra("s3_progress", 1));
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (!mUploadArtBroadcastIsRegistered) {
            this.registerReceiver(doneArtProgressBroadcastReceiver, new IntentFilter(UploadArtService.DONE_BROADCAST));
            mUploadArtBroadcastIsRegistered = true;
        }
        if (!mSaveArtBroadcastIsRegistered) {
            this.registerReceiver(doneArtSaveBroadcastReceiver, new IntentFilter(UploadArtService.SAVE_BROADCAST));
            mSaveArtBroadcastIsRegistered = true;
        }

        if (!mDoneTrackS3SaveBroadcastReceiver) {
            this.registerReceiver(doneTrackS3SaveBroadcastReceiver, new IntentFilter(UploadTuneService.S3_KEY_DONE_ACTION));
            mDoneTrackS3SaveBroadcastReceiver = true;
        }

        if (!mProgressTuneBroadcastReceiver){
            this.registerReceiver(progressTuneBroadcastReceiver, new IntentFilter(UploadTuneService.S3_KEY_PROGRESS_ACTION));
            mProgressTuneBroadcastReceiver = true;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mUploadArtBroadcastIsRegistered) {
            unregisterReceiver(doneArtProgressBroadcastReceiver);
            mUploadArtBroadcastIsRegistered = false;
        }
        if (mSaveArtBroadcastIsRegistered) {
            unregisterReceiver(doneArtSaveBroadcastReceiver);
            mSaveArtBroadcastIsRegistered = false;
        }
        if (mDoneTrackS3SaveBroadcastReceiver) {
            unregisterReceiver(doneTrackS3SaveBroadcastReceiver);
            mDoneTrackS3SaveBroadcastReceiver = false;
        }
        if (mProgressTuneBroadcastReceiver){
            unregisterReceiver(progressTuneBroadcastReceiver);
            mProgressTuneBroadcastReceiver = false;
        }
    }

    @Override
    public int getActivityResourceId() {
        return R.layout.activity_send_tune;
    }


    @Override
    public boolean hasToolBar() {
        return true;
    }

    @Override
    public Toolbar getToolBar() {
        return mToolBar;
    }

    @Override
    public int getMenuResourceId() {
        return R.menu.menu_send_tune;
    }

    @Override
    public boolean enableBack() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mArt.setOnClickListener(this);
        mChooseMp3Btn.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {


            bothReady = true;
            mTitle = tEV.getText().toString().trim();
            mDesc = dEV.getText().toString().trim();
            if (saveArtState == -1 && !artSet) {
                Log.w("TAG", "Start art service");
                Intent i = new Intent(this, UploadArtService.class);
                i.setData(mArtMediaUri);
                startService(i);
                artSet = true;
            }
            while (saveArtState == 0) {
                if (mMp3MediaUri != null && !mTitle.isEmpty()) {
                    Intent i = new Intent(this, UploadTuneService.class);
                    i.setData(mMp3MediaUri);

                    startService(i);

                    Log.w("TAG", "Start Tune service");

                    saveArtState = -2;
                }
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.art:
                if (saveArtState != 0 && !artSet) {
                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                    i.setType("image/*");
                    startActivityForResult(i, PICK_ART_REQUEST);
                }
                break;
            case R.id.chooseMp3:
                Intent chooseMp3Intent = new Intent(Intent.ACTION_GET_CONTENT);
                chooseMp3Intent.setType("audio/mpeg");
                startActivityForResult(chooseMp3Intent, PICK_MP3_REQUEST);
                break;
            case R.id.action_send:


                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            switch (requestCode) {
                case PICK_ART_REQUEST:
                    mArtMediaUri = data.getData();
                    byte[] mArtFileBytes = FileHelper.getByteArrayFromFile(this, mArtMediaUri);
                    mArt.setImageBitmap(
                            FileHelper.resizeImageMaintainAspectRatio(
                                    mArtFileBytes, FileHelper.SHORT_SIDE_TARGET
                            )
                    );
                    mPLayout.setVisibility(View.VISIBLE);
                    break;
                case PICK_MP3_REQUEST:
                    mMp3MediaUri = data.getData();
//                    byte[] mMp3FileBytes = FileHelper.getByteArrayFromFile(this, mMp3MediaUri);
                    break;
            }
        }
    }

    private void updateArtProgress(int progress) {
        artProgress = progress;


        Log.w("PRO", String.valueOf(progress));
        updateProgressBar();
    }

    private void updateProgressBar() {

        tp = artProgress + tuneProgress;

        Log.w("PRO", String.valueOf(p));

        if (!progStart) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (tp < 200) {
                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                mSaveProgressBar.setProgress(tp);
                            }
                        });

                    }
                }
            }).start();
            progStart = true;
        }

    }

    private void updateTuneProgress(int s3_progress) {
        tuneProgress = s3_progress;
        updateProgressBar();
    }

    private void createNewTune(String artUrl) {
        tune = new ParseObject("Tunes");
        tune.put(Track.ART_URL, artUrl);
        tune.put(Track.TRACK_TITLE, mTitle);
        tune.put(Track.TRACK_DESC, mDesc);
        tune.saveEventually();
    }
}
