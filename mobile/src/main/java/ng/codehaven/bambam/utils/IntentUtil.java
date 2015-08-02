package ng.codehaven.bambam.utils;

import android.content.Context;
import android.content.Intent;

public class IntentUtil {

    private Context mContext;
    private Intent mIntent;

    public IntentUtil(Context c) {
        mContext = c;
    }

    public IntentUtil(Context c, Intent i) {
        mContext = c;
        mIntent = i;
    }

    public void goToActivity(Intent intent) throws NullPointerException{
        Intent i = intent;
        if (i == null){
            i = mIntent;
        }
        mContext.startActivity(i);
    }
}
