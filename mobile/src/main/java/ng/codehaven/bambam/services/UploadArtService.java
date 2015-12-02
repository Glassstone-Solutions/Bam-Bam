package ng.codehaven.bambam.services;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;

import ng.codehaven.bambam.Constants;
import ng.codehaven.bambam.utils.FileHelper;

public class UploadArtService extends Service implements ProgressCallback, SaveCallback {

    public static final String DONE_BROADCAST = UploadArtService.class.getSimpleName()+".donebroadcast";
    public static final String SAVE_BROADCAST = UploadArtService.class.getSimpleName()+".savebroadcast";

    int donePercent;

    int saveState;

    Intent mProgressIntent, mDoneIntent;

    ParseFile mParseFile;

    public UploadArtService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mProgressIntent = new Intent(DONE_BROADCAST);
        mDoneIntent = new Intent(SAVE_BROADCAST);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.w("TAG", "Started art service");
        Uri mArtUri = intent.getData();
        byte[] mArtFileBytes = FileHelper.getByteArrayFromFile(this, mArtUri);

        mParseFile = new ParseFile(FileHelper.getFileName(this, mArtUri, Constants.TYPE_IMAGE), mArtFileBytes);

        mParseFile.saveInBackground(this, this);

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void done(Integer i) {
        donePercent = i;
        Log.w("TAG", "Progress "+String.valueOf(i));
        mProgressIntent.putExtra("progress", i);
        sendBroadcast(mProgressIntent);
    }

    @Override
    public void done(ParseException e) {
        if (e == null){
            saveState = 0;
            String fileUrl = mParseFile.getUrl();
            mDoneIntent.putExtra("saveState", saveState);
            mDoneIntent.putExtra("artUrl", fileUrl);
            sendBroadcast(mDoneIntent);
            stopSelf();
        } else {
            saveState = e.getCode();
            Log.e("ERROR", e.getMessage()+". Code = "+String.valueOf(e.getCode()));
        }
    }
}
