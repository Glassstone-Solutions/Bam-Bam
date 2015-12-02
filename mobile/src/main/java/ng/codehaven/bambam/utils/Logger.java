package ng.codehaven.bambam.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Logger {
    private String mTag;
    private Context mContext;
    private boolean mIsToast;
    public static final String TAG = "BAM-BAM";

    public Logger(String tag) {
        mTag = tag;
        mIsToast = false;
    }

    public Logger(Context context, boolean isToast) {
        mContext = context;
        mIsToast = isToast;
    }

    public void log(String message){
        if (mTag.isEmpty()){
            mTag = TAG;
        }
        Log.d(mTag, message);
    }

    public void toast(String message){
        if (!mIsToast){
            log(message);
        } else {
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        }
    }
}
