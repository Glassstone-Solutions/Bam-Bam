package ng.codehaven.bambam.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ng.codehaven.bambam.models.Comment;
import ng.codehaven.bambam.models.Track;

public class CommentsService extends Service {

    public CommentsService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Realm realm = Realm.getInstance(this);
        RealmResults<Track> r = realm.where(Track.class).findAll();

        for (final Track t : r){
            ParseQuery<ParseObject> obj = ParseQuery.getQuery("Activities");
            obj.whereEqualTo("tuneId", t.getParseId());
            obj.whereEqualTo("type", "comment");
            obj.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> list, ParseException e) {
                    if (e == null){
                        for (final ParseObject p : list){
                            p.getParseUser("from").fetchIfNeededInBackground(new GetCallback<ParseUser>() {
                                @Override
                                public void done(ParseUser user, ParseException e) {
                                    if (e == null) {
                                        realm.beginTransaction();
                                        Comment c = new Comment();
                                        c.setmUsername(user.getString("f_username"));
                                        c.setmComment(p.getString("content"));
                                        c.setmAvatar("https://graph.facebook.com/" +user.getString("fb_id")+ "/picture?type=large");
                                        c.setmCreatedAt(p.getCreatedAt());
                                        t.getmComments().add(c);
                                        realm.commitTransaction();
                                    }
                                }
                            });
                        }
                    }
                }
            });
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
