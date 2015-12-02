package ng.codehaven.bambam.utils;

import android.content.Context;
import android.support.annotation.NonNull;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import ng.codehaven.bambam.models.Track;
import ng.codehaven.bambam.models.TuneMeta;


public class GetTunesHelper {

    public static List<Track> GetTunes(List<ParseObject> mTracks) throws ParseException {

        List<Track> tracks = new ArrayList<>();

        for (ParseObject i : mTracks) {
            tracks.add(new Track(
                            i.getString("title"),
                            i.getObjectId(),
                            i.getString("desc"),
                            i.getParseFile("track").getUrl(),
                            i.getParseFile("art").getUrl(),
                            i.getParseUser("owner").fetchIfNeeded().getString("f_user"),
                            i.getParseUser("owner").fetchIfNeeded().getObjectId(),
                            i.getBoolean("forSale"),
                            false,
                            i.getCreatedAt())
            );
        }
        return tracks;
    }

    public static List<Track> getTunes(Context c){
        List<Track> tracks = new ArrayList<>();

        Realm realm = Realm.getInstance(c);
        RealmResults<Track> r = realm.where(Track.class)
                .equalTo("artistObjId", ParseUser.getCurrentUser().getObjectId())
                .findAllSorted("createdAt", RealmResults.SORT_ORDER_DESCENDING);

        for (Track t : r){
            tracks.add(t);
        }

        return tracks;
    }

    public static Track getTrack(ParseObject user, ParseObject i, boolean isLiked) {
        return new Track(
                i.getString("title"),
                i.getObjectId(),
                i.getString("desc"),
                i.getParseFile("track").getUrl(),
                i.getParseFile("art").getUrl(),
                user.getString("f_username"),
                user.getObjectId(),
                i.getBoolean("forSale"),
                isLiked,
                i.getCreatedAt()
        );
    }

    public static boolean getTuneLike(String t) throws ParseException {

        ParseObject tune = getTuneParseObject(t);

        ParseQuery<ParseObject> like = ParseTuneQuery(tune, "like", ParseUser.getCurrentUser());

        return like.getFirst() != null;

    }

    public static int getTuneLikeCount(String t) throws ParseException {
        ParseObject tune = getTuneParseObject(t);

        ParseQuery<ParseObject> like = ParseTuneQuery(tune, "like", null);

        return like.count();
    }

    public static void setTuneLike(String t) throws ParseException {
        ParseObject tune = getTuneParseObject(t);
        ParseObject like = new ParseObject("Activities");
        like.put("from", ParseUser.getCurrentUser());
        like.put("tune", tune);

        like.save();

    }

    public static List<HashMap<String,String[]>> comments(String t) throws ParseException {
        ParseObject tune = getTuneParseObject(t);

        ParseQuery<ParseObject> like = ParseTuneQuery(tune, "comments", null);

        like.orderByDescending("createdAt");
        like.setLimit(3);

        List<ParseObject> p = like.find();

        List<HashMap<String,String[]>> mComment = new ArrayList<>();

        if (p.size() > 0) {
            for (int i = 0; i<p.size(); i++){
                String[] cA = {p.get(i).getParseUser("from").fetchIfNeeded().getString("f_name"), p.get(i).getString("comment")};
                HashMap<String, String[]> c = new HashMap<>();
                c.put("comment", cA);
                mComment.add(c);

            }
        }

        return mComment;
    }

    private static ParseObject getTuneParseObject(String t) throws ParseException {
        ParseQuery<ParseObject> getTune = new ParseQuery<>("Tunes");
        getTune.whereEqualTo("objectId", t);

        return getTune.getFirst();
    }

    public static TuneMeta getTuneMeta(String t) throws ParseException{
        return new TuneMeta(getTuneLikeCount(t), getTuneLike(t),comments(t));
    }

    @NonNull
    private static ParseQuery<ParseObject> ParseTuneQuery(ParseObject tune, String type, ParseUser user) {
        ParseQuery<ParseObject> like = new ParseQuery<>("Activities");
        like.whereEqualTo("tune", tune);
        like.whereEqualTo("type", type);
        if (user != null) {
            like.whereEqualTo("from", user);
        }
        return like;
    }


}
