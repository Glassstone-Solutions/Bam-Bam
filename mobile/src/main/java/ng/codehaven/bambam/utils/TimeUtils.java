package ng.codehaven.bambam.utils;

import com.ocpsoft.pretty.time.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Thompson on 8/26/2015.
 */
public class TimeUtils {
    public static String timeAgo(String time) {
        PrettyTime mPtime = new PrettyTime();

        long timeAgo = timeStringtoMilis(time);

        return mPtime.format(new Date(timeAgo));
    }

    public static long timeStringtoMilis(String time) {
        long milis = 0;

        try {
            SimpleDateFormat sd = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
            Date date = sd.parse(time);
            milis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return milis;
    }
}
