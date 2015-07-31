package ng.codehaven.bambam.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class Logger {

    private String mMessage;
    private String mTag;
    private Context mContext;
    private boolean mIsToast;
    public static final String TAG = "BAM-BAM";

    public Logger(String message, String tag) {
        mMessage = message;
        mTag = tag;
        mIsToast = false;
    }

    public Logger(String message, Context context, boolean isToast) {
        mMessage = message;
        mContext = context;
        mIsToast = isToast;
    }

    public void log(){
        if (mTag.isEmpty()){
            mTag = TAG;
        }
        Log.d(mTag, mMessage);
    }

    public void toast(){
        if (!mIsToast){
            log();
        } else {
            Toast.makeText(mContext, mMessage, Toast.LENGTH_SHORT).show();
        }
    }
}
