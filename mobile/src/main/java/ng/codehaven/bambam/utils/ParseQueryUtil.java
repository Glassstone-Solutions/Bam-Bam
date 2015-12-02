package ng.codehaven.bambam.utils;

import android.support.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class ParseQueryUtil {

    public static List<ParseObject> getMyTunes(int limit, int skip) throws ParseException {
        return getTuneObjs(limit, skip, ParseUser.getCurrentUser()).find();
    }

    public static List<ParseObject> getUserTunes(int limit, int skip, ParseUser user) throws ParseException {
        return getTuneObjs(limit, skip, user).find();
    }

    public static List<ParseObject> timeline(int limit, int skip) throws ParseException {


        return null;

    }

    @NonNull
    private static ParseQuery<ParseObject> getTuneObjs(int limit, int skip, ParseUser user) {
        ParseQuery<ParseObject>mObjs = new ParseQuery<>("Tunes");
        mObjs.whereEqualTo("owner", user);
        mObjs.setLimit(limit);
        mObjs.setSkip(skip);
        mObjs.addDescendingOrder("createdAt");
        return mObjs;
    }
}
