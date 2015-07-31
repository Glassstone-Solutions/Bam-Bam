package ng.codehaven.bambam.ui.views;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.Window;

public class WindowCompatUtils {
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void setStatusBarcolor(Window window, int color){
        window.setStatusBarColor(color);
    }
}
